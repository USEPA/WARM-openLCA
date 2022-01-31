package gov.epa.warm.backend.system;

import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.ProcessLink;

/**
 * An index of process links that is used for creation of product systems.
 */
class ProcessLinkIndex {

	/** Maps provider-process-id -> recipient-process-id -> product-flow-id. -> recipient.exchange.id */
	private final TLongObjectHashMap<TLongObjectHashMap<TLongObjectHashMap<TLongHashSet>>> exchangeIndex;

	public ProcessLinkIndex() {
		exchangeIndex = new TLongObjectHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
	}

	/**
	 * Returns true if the link with the given provider, recipient, and flow is
	 * contained in this index.
	 */
	public boolean contains(ProcessLink link) {
		return contains(link.providerId, link.processId, link.flowId, link.exchangeId);
	}

	/**
	 * Returns true if the link with the given provider, recipient, flow and
	 * exchange is contained in this index.
	 */
	public boolean contains(long provider, long recipient, long flow, long exchange) {
		TLongObjectHashMap<TLongObjectHashMap<TLongHashSet>> recFlowExMap = exchangeIndex.get(provider);
		if (recFlowExMap == null)
			return false;
		TLongObjectHashMap<TLongHashSet> flowExMap = recFlowExMap.get(recipient);
		if (flowExMap == null)
			return false;
		TLongHashSet exchangeIds = flowExMap.get(flow);
		if (exchangeIds == null)
			return false;
		return exchangeIds.contains(exchange);
	}

	/**
	 * Adds the given link to this index. Multiple inserts of the same link result
	 * in a single entry.
	 */
	public void put(ProcessLink link) {
		if (link == null)
			return;
		put(link.providerId, link.processId, link.flowId, link.exchangeId);
	}

	/**
	 * Adds a new link with the given provider, recipient, flow and exchange to this
	 * index. Multiple inserts of the same triple result in a single entry.
	 */
	public void put(long provider, long recipient, long flow, long exchange) {
		TLongObjectHashMap<TLongObjectHashMap<TLongHashSet>> recFlowExMap = exchangeIndex.get(provider);
		if (recFlowExMap == null) {
			recFlowExMap = new TLongObjectHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
			exchangeIndex.put(provider, recFlowExMap);
		}
		TLongObjectHashMap<TLongHashSet> flowExMap = recFlowExMap.get(recipient);
		if (flowExMap == null) {
			flowExMap = new TLongObjectHashMap<>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
			recFlowExMap.put(recipient, flowExMap);
		}
		TLongHashSet exchangeIds = flowExMap.get(flow);
		if (exchangeIds == null) {
			exchangeIds = new TLongHashSet();
			flowExMap.put(flow, exchangeIds);
		}
		exchangeIds.add(exchange);
	}

	/**
	 * Creates a new list of process links from this index.
	 */
	public List<ProcessLink> createLinks() {
		List<ProcessLink> links = new ArrayList<>();
		for (long provider : exchangeIndex.keys()) {
			TLongObjectHashMap<TLongObjectHashMap<TLongHashSet>> recFlowExMap = exchangeIndex.get(provider);
			if (recFlowExMap == null)
				continue;
			for (long recipient : recFlowExMap.keys()) {
				TLongObjectHashMap<TLongHashSet> flowExMap = recFlowExMap.get(recipient);
				if (flowExMap == null)
					continue;
				for (long flow : flowExMap.keys()) {
					TLongHashSet exchangeIds = flowExMap.get(flow);
					for (long exchange : exchangeIds.toArray()) {
						ProcessLink link = new ProcessLink();
						link.providerId = provider;
						link.processId = recipient;
						link.flowId = flow;
						link.exchangeId = exchange;
						links.add(link);
					}
				}
			}
		}
		return links;
	}

}
