package gov.epa.warm.backend.system;

import java.sql.Connection;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.ProductSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.impl.Constants;
import gnu.trove.set.hash.TLongHashSet;
import gov.epa.warm.backend.data.mapping.ProviderMapping;

public class ProductSystemBuilder {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final MatrixCache matrixCache;
	private final IDatabase database;
	private boolean preferSystemProcesses;

	public ProductSystemBuilder(MatrixCache matrixCache) {
		this(matrixCache, false);
	}

	public ProductSystemBuilder(MatrixCache matrixCache,
			boolean preferSystemProcesses) {
		this.matrixCache = matrixCache;
		this.database = matrixCache.getDatabase();
		this.preferSystemProcesses = preferSystemProcesses;
	}

	public ProductSystem autoComplete(ProductSystem system,
			ProviderMapping[] mappings) {
		if (system == null || system.referenceExchange == null || system.referenceProcess == null)
			return system;
		Process refProcess = system.referenceProcess;
		Flow refProduct = system.referenceExchange.flow;
		if (refProduct == null)
			return system;
		TechFlow ref = TechFlow.of(refProcess, refProduct);
		return autoComplete(system, ref, mappings);
	}

	private ProductSystem autoComplete(ProductSystem system,
			TechFlow processProduct, ProviderMapping[] mappings) {
		try (Connection con = database.createConnection()) {
			log.trace("auto complete product system {}", system);
			run(system, processProduct, mappings);
			log.trace("reload system");
			database.getEntityFactory().getCache().evict(ProductSystem.class);
			return new ProductSystemDao(database).getForId(system.id);
		} catch (Exception e) {
			log.error("Failed to auto complete product system " + system, e);
			return null;
		}
	}

	private void run(ProductSystem system, TechFlow processProduct, ProviderMapping[] mappings) {
		log.trace("build product index");
		ProductIndexBuilder builder = new ProductIndexBuilder(matrixCache, mappings);
		builder.setPreferredType(preferSystemProcesses ? ProcessType.LCI_RESULT : ProcessType.UNIT_PROCESS);
		TechIndex index = builder.build(processProduct);
		log.trace("built a product index with {} process products and {} links",
				index.size(), index.getLinkedExchanges().size());
		log.trace("create new process links");
		addLinksAndProcesses(system, index);
	}

	private void addLinksAndProcesses(ProductSystem system, TechIndex index) {
		ProcessLinkIndex links = new ProcessLinkIndex();
		TLongHashSet processes = new TLongHashSet(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, -1);
		addSystemLinksAndProcesses(system, links, processes);
		log.trace("add new processes and links");
		for (LongPair input : index.getLinkedExchanges()) {
			TechFlow output = index.getLinkedProvider(input);
			if (output == null)
				continue;
			long provider = output.processId();
			long recipient = input.first;
			long exchange = input.second;
			long flow = output.flowId();
			processes.add(provider);
			processes.add(recipient);
			links.put(provider, recipient, flow, exchange);
		}
		updateDatabase(system, links, processes);
	}

	private void addSystemLinksAndProcesses(ProductSystem system, ProcessLinkIndex linkIndex, TLongHashSet processes) {
		log.trace("the system already contains {} links and {} processes",
				system.processLinks.size(), system.processes.size());
		for (ProcessLink link : system.processLinks)
			linkIndex.put(link);
		for (long procId : system.processes)
			processes.add(procId);
	}

	private void updateDatabase(ProductSystem system, ProcessLinkIndex links, TLongHashSet processes) {
		try {
			log.trace("update product system tables");
			cleanTables(system.id);
			insertLinks(system.id, links.createLinks());
			insertProcesses(system.id, processes);
		} catch (Exception e) {
			log.error("faile to update database in process builder", e);
		}
	}

	private void cleanTables(long systemId) throws Exception {
		log.trace("clean system tables for {}", systemId);
		String sql = "DELETE FROM tbl_process_links WHERE f_product_system = " + systemId;
		NativeSql.on(database).runUpdate(sql);
		sql = "DELETE FROM tbl_product_system_processes WHERE f_product_system = " + systemId;
		NativeSql.on(database).runUpdate(sql);
	}

	
	private void insertLinks(final long systemId, final List<ProcessLink> links) throws Exception {
		log.trace("insert {} process links", links.size());
		String stmt = "INSERT INTO tbl_process_links(f_product_system, f_provider, f_process, f_flow, f_exchange) VALUES (?, ?, ?, ?, ?)";
		NativeSql.on(database).batchInsert(stmt, links.size(), (i, ps) -> {
			ProcessLink link = links.get(i);
			ps.setLong(1, systemId);
			ps.setLong(2, link.providerId);
			ps.setLong(3, link.processId);
			ps.setLong(4, link.flowId);
			ps.setLong(5, link.exchangeId);
			return true;
		});
	}

	private void insertProcesses(final long systemId, TLongHashSet processes) throws Exception {
		log.trace("insert {} system processes", processes.size());
		final long[] processIds = processes.toArray();
		String stmt = "INSERT INTO tbl_product_system_processes(f_product_system, f_process) values (?, ?)";
		NativeSql.on(database).batchInsert(stmt, processIds.length, (i, ps) -> {
			ps.setLong(1, systemId);
			ps.setLong(2, processIds[i]);
			return true;
		});
	}
}
