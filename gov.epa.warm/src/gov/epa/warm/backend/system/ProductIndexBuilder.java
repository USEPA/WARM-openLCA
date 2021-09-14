package gov.epa.warm.backend.system;

import gov.epa.warm.backend.app.App;
import gov.epa.warm.backend.data.mapping.ProviderMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProductIndexBuilder {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final MatrixCache cache;
	private final ProcessTable processTable;
	private final ProviderMapping[] providerMappings;
	private ProcessType preferredType = ProcessType.LCI_RESULT;

	public ProductIndexBuilder(MatrixCache cache, ProviderMapping[] providerMappings) {
		this.cache = cache;
		this.processTable = cache.getProcessTable();
		this.providerMappings = providerMappings != null ? providerMappings : new ProviderMapping[0];
	}

	public void setPreferredType(ProcessType preferredType) {
		this.preferredType = preferredType;
	}

	public ProductIndex build(LongPair refProduct) {
		return build(refProduct, 1.0);
	}

	public ProductIndex build(LongPair refProduct, double demand) {
		log.trace("build product index for {}", refProduct);
		ProductIndex index = new ProductIndex(refProduct);
		index.setDemand(demand);
		List<LongPair> block = new ArrayList<>();
		block.add(refProduct);
		HashSet<LongPair> handled = new HashSet<>();
		while (!block.isEmpty()) {
			List<LongPair> nextBlock = new ArrayList<>();
			log.trace("fetch next block with {} entries", block.size());
			Map<Long, List<CalcExchange>> exchanges = fetchExchanges(block);
			for (LongPair recipient : block) {
				handled.add(recipient);
				List<CalcExchange> processExchanges = exchanges.get(recipient.getFirst());
				List<CalcExchange> productInputs = getProductInputs(processExchanges);
				for (CalcExchange productInput : productInputs) {
					LongPair provider = findProvider(productInput);
					if (provider == null)
						continue;
					LongPair recipientInput = new LongPair(recipient.getFirst(), productInput.flowId);
					index.putLink(recipientInput, provider);
					if (!handled.contains(provider) && !nextBlock.contains(provider))
						nextBlock.add(provider);
				}
			}
			block = nextBlock;
		}
		return index;
	}

	private List<CalcExchange> getProductInputs(List<CalcExchange> processExchanges) {
		if (processExchanges == null || processExchanges.isEmpty())
			return Collections.emptyList();
		List<CalcExchange> productInputs = new ArrayList<>();
		for (CalcExchange exchange : processExchanges) {
			if (!exchange.input)
				continue;
			if (exchange.flowType == FlowType.ELEMENTARY_FLOW)
				continue;
			productInputs.add(exchange);
		}
		return productInputs;
	}

	private Map<Long, List<CalcExchange>> fetchExchanges(List<LongPair> block) {
		if (block.isEmpty())
			return Collections.emptyMap();
		Set<Long> processIds = new HashSet<>();
		for (LongPair pair : block)
			processIds.add(pair.getFirst());
		try {
			return cache.getExchangeCache().getAll(processIds);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to load exchanges from cache", e);
			return Collections.emptyMap();
		}
	}

	private LongPair findProvider(CalcExchange productInput) {
		if (productInput == null)
			return null;
		long productId = productInput.flowId;
		LongPair candidate = getMappedProvider(productInput);
		if (candidate != null)
			return candidate;
		long[] processIds = processTable.getProductProviders(productId);
		if (processIds == null)
			return null;
		for (long processId : processIds) {
			LongPair newOption = LongPair.of(processId, productId);
			if (isBetter(productInput, candidate, newOption))
				candidate = newOption;
		}
		return candidate;
	}

	private LongPair getMappedProvider(CalcExchange productInput) {
		for (ProviderMapping mapping : providerMappings) {
			if (!matches(productInput, mapping))
				continue;
			long providerId = App.getProcessIdMap().get(mapping.getProviderId());
			return new LongPair(providerId, productInput.flowId);
		}
		return null;
	}

	private boolean matches(CalcExchange productInput, ProviderMapping mapping) {
		String processId = App.getProcessIdMap().get(productInput.processId);
		String flowId = App.getFlowIdMap().get(productInput.flowId);
		return mapping.matches(processId, flowId);
	}

	private boolean isBetter(CalcExchange inputLink, LongPair candidate,
			LongPair newOption) {
		if (candidate == null)
			return true;
		if (newOption == null)
			return false;
		if (candidate.getFirst() == inputLink.defaultProviderId)
			return false;
		if (newOption.getFirst() == inputLink.defaultProviderId)
			return true;
		ProcessType candidateType = processTable.getType(candidate.getFirst());
		ProcessType newOptionType = processTable.getType(newOption.getFirst());
		if (candidateType == preferredType && newOptionType != preferredType)
			return false;
		return candidateType != preferredType && newOptionType == preferredType;
	}
}
