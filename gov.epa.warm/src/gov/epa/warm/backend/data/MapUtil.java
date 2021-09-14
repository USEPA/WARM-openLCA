package gov.epa.warm.backend.data;

import gov.epa.warm.rcp.utils.ObjectMap;

public class MapUtil {

	private static String[] TYPES = { "baseline", "alternative" };

	public static void move(ObjectMap input, ObjectMap choices, String field) {
		move(input, field, choices, field, null);
	}

	public static void move(ObjectMap input, String inputField, ObjectMap choices, String choicesField,
			String defaultValue) {
		if (!input.containsKey(inputField))
			return;
		String value = input.get(inputField + ".value");
		if (value == null || value.isEmpty())
			value = defaultValue;
		choices.put(choicesField, value);
	}

	public static String convertMaterial(String materialName) {
		materialName = materialName.replace(' ', '_');
		materialName = materialName.replace('(', '_');
		materialName = materialName.replace(')', '_');
		materialName = materialName.replace('-', '_');
		materialName = materialName.replace('/', '_');
		while (materialName.contains("__"))
			materialName = materialName.replace("__", "_");
		if (materialName.startsWith("_"))
			materialName = materialName.substring(1);
		if (materialName.endsWith("_"))
			materialName = materialName.substring(0, materialName.length() - 1);
		return materialName.toLowerCase();
	}

	public static boolean isMaterialInput(String param) {
		for (String type : TYPES)
			if (param.startsWith(type))
				if (!param.endsWith("_result"))
					return true;
		return false;
	}

}
