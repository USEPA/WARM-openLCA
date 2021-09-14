package gov.epa.warm.backend.data.in;

import gov.epa.warm.backend.data.MapUtil;
import gov.epa.warm.rcp.utils.ObjectMap;

public class InputMapper {

	public static final String[] SCENARIO_TYPES = {
			"baseline", "alternative"
	};
	public static final String[] FIELDS = {
			"source_reduction", "recycling", "landfilling", "composting", "combustion"
	};
	private static final String[] TRANSPORT_DISTANCE_KEYS = {
			"transport_distance_combustion", "transport_distance_composting",
			"transport_distance_landfilling", "transport_distance_recycling"
	};

	/*
	 * Map contains keys 0, 1, ...., n - 1 where n = no. of materials
	 */
	public static ObjectMap mapInputValues(ObjectMap inputData) {
		ObjectMap inputValues = new ObjectMap();
		ObjectMap materialData = ObjectMap.fromMap(inputData.get("materials"));
		for (String index : materialData.keySet()) {
			ObjectMap material = ObjectMap.fromMap(materialData.get(index));
			for (String scenarioType : SCENARIO_TYPES)
				for (String field : FIELDS) {
					String sourceKey = getSourceKey(scenarioType, field);
					String targetKey = getTargetKey(scenarioType, field, material.getString("name"));
					String value = material.get(sourceKey);
					if (value != null && !value.isEmpty())
						inputValues.put(targetKey, value);
				}
		}
		mapTransportDistance(inputData, inputValues);
		mapSourceReduction(inputData, inputValues);
		return ObjectMap.fromMap(inputValues);
	}

	private static void mapTransportDistance(ObjectMap input, ObjectMap data) {
		String value = input.getString("transport_distance.value");
		if (value == null)
			value = "transport_distance_default";
		switch (value) {
		case "transport_distance_default":
			for (String key : TRANSPORT_DISTANCE_KEYS)
				data.remove(key);
			break;
		case "transport_distance_define":
			for (String key : TRANSPORT_DISTANCE_KEYS)
				MapUtil.move(input, key, data, key, "0");
			break;
		}
	}

	private static void mapSourceReduction(ObjectMap input, ObjectMap data) {
		String value = input.getString("source_reduction.value");
		if (value == null)
			return;
		if (value.equals("source_reduction_virgin"))
			data.put("source_reduction", "0");
		else
			data.put("source_reduction", "1");
	}

	private static String getSourceKey(String scenarioType, String field) {
		StringBuilder key = new StringBuilder();
		key.append(scenarioType);
		key.append("_");
		key.append(field);
		return key.toString();
	}

	private static String getTargetKey(String scenarioType, String field, String materialName) {
		materialName = MapUtil.convertMaterial(materialName);
		StringBuilder key = new StringBuilder();
		key.append(scenarioType);
		key.append("_");
		key.append(materialName);
		key.append("_");
		key.append(field);
		return key.toString();
	}

}
