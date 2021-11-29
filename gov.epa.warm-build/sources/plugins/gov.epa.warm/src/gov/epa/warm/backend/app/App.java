package gov.epa.warm.backend.app;

import java.io.File;
import java.io.IOException;

import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.matrix.solvers.JavaSolver;
import org.openlca.core.matrix.solvers.MatrixSolver;
import org.openlca.julia.Julia;
import org.openlca.julia.JuliaSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.epa.warm.rcp.utils.Rcp;

public class App {

	private static Logger log = LoggerFactory.getLogger(App.class);
	private static IDatabase database;
	private static String databaseName;
	private static MatrixSolver solver;
	private static MatrixCache matrixCache;
	private static RefIdMap flowIdMap;
	private static RefIdMap processIdMap;

	static {
		if (!Julia.isLoaded()) {
			log.warn("could not load a high-performance library for calculations");
			solver = new JavaSolver();
		} else
			solver = new JuliaSolver();
	}

	public static void activateDatabase(String databaseName) {
		if (App.databaseName != null)
			if (App.databaseName.equals(databaseName))
				return;
		deactiveDatabase();
		App.databaseName = databaseName;
		database = new Derby(getDatabase(databaseName));
		new ProductSystemDao(database).deleteAll();
		flowIdMap = new RefIdMap(database, "tbl_flows");
		processIdMap = new RefIdMap(database, "tbl_processes");
		matrixCache = MatrixCache.createLazy(database);
	}

	public static void deactiveDatabase() {
		try {
			if (database != null)
				database.close();
		} catch (IOException e) {
			log.error("Error closing database", e);
		} finally {
			flowIdMap = null;
			processIdMap = null;
			matrixCache = null;
		}
	}

	public static IDatabase getDatabase() {
		return database;
	}

	public static MatrixSolver getSolver() {
		return solver;
	}

	public static MatrixCache getMatrixCache() {
		return matrixCache;
	}

	public static RefIdMap getFlowIdMap() {
		return flowIdMap;
	}

	public static RefIdMap getProcessIdMap() {
		return processIdMap;
	}
	
	private static File getDatabase(String name) {
		File databaseDir = new File(Rcp.getWorkspace(), "databases");
		if (!databaseDir.exists())
			databaseDir.mkdir();
		return new File(databaseDir, databaseName);
	}

}
