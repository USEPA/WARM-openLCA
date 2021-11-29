package gov.epa.warm.backend.app;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

public class RefIdMap {

	private final Map<String, Long> refIdToId = new HashMap<>();
	private final Map<Long, String> idToRefId = new HashMap<>();

	public RefIdMap(IDatabase database, String table) {
		NativeSql.on(database).query("SELECT id, ref_id FROM " + table, (resultSet) -> {
			long id = resultSet.getLong("id");
			String refId = resultSet.getString("ref_id");
			idToRefId.put(id, refId);
			refIdToId.put(refId, id);
			return true;
		});

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
