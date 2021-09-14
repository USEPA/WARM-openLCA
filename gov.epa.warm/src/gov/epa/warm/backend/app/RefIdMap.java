package gov.epa.warm.backend.app;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefIdMap {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Map<String, Long> refIdToId = new HashMap<>();
	private final Map<Long, String> idToRefId = new HashMap<>();

	public RefIdMap(IDatabase database, String table) {
		try {
			NativeSql.on(database).query("SELECT id, ref_id FROM " + table,
					(resultSet) -> {
						long id = resultSet.getLong("id");
						String refId = resultSet.getString("ref_id");
						idToRefId.put(id, refId);
						refIdToId.put(refId, id);
						return true;
					});
		} catch (SQLException e) {
			log.error("Error loading ref id map", e);
		}
	}

	public String get(long id) {
		return idToRefId.get(id);
	}

	public long get(String refId) {
		if (!refIdToId.containsKey(refId))
			return 0;
		return refIdToId.get(refId);
	}

}
