package gov.epa.warm.backend.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.matrix.CalcExchange;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.cache.ProcessTable;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.linking.LinkingConfig;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.epa.warm.backend.app.App;
import gov.epa.warm.backend.data.mapping.ProviderMapping;

class ProductIndexBuilder {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final MatrixCache cache;
	private final ProcessTable processTable;
	private final ProviderMapping[] providerMappings;
	private final LinkingConfig config;
	
	public ProductIndexBuilder(MatrixCache cache, ProviderMapping[] providerMappings) {
		this.cache = cache;
		this.processTable = cache.getProcessTable();
		this.providerMappings = providerMappings != null ? providerMappings : new ProviderMapping[0];
		this.config = new LinkingConfig();
	}

	public void setPreferredType(ProcessType preferredType) {
		this.config.preferredType = preferredType;
	}

	public TechIndex build(TechFlow techFlow) {
		return build(techFlow, 1.0);
	}

	public TechIndex build(TechFlow techFlow, double demand) {
		LongPair refProduct = new LongPair(techFlow.processId(), techFlow.flowId());
		log.trace("build product index for {}", refProduct);
		TechIndex index = new TechIndex(techFlow);
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
				List<CalcExchange> processExchanges = exchanges.get(recipient.first);
				List<CalcExchange> productInputs = getProductInputs(processExchanges);
				for (CalcExchange productInput : productInputs) {
					LongPair provider = findProvider(productInput);
					if (provider == null)
						continue;
					LongPair recipientInput = new LongPair(recipient.first, productInput.exchangeId);
					TechFlow techFlowProvider = processTable.getProvider(provider.first, productInput.flowId);
					index.putLink(recipientInput, techFlowProvider);
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
			if (!exchange.isInput)
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
			processIds.add(pair.first);
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
		List<TechFlow> processes = processTable.getProviders(productId);
		if (processes == null)
			return null;
		for (TechFlow process : processes) {
			LongPair newOption = LongPair.of(process.processId(), productId);
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

	private boolean isBetter(CalcExchange inputLink, LongPair candidate, LongPair newOption) {
		if (candidate == null)
			return true;
		if (newOption == null)
			return false;
		if (candidate.first == inputLink.defaultProviderId)
			return false;
		if (newOption.first == inputLink.defaultProviderId)
			return true;
		ProcessType candidateType = processTable.getType(candidate.first);
		ProcessType newOptionType = processTable.getType(newOption.first);
		if (candidateType == config.preferredType && newOptionType != config.preferredType)
			return false;
		return candidateType != config.preferredType && newOptionType == config.preferredType;
	}
}
