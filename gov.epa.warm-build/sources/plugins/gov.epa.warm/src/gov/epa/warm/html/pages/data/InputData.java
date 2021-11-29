package gov.epa.warm.html.pages.data;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class InputData {

	public static void putDefaultInputs(Map<String, String> data) {
		// some values must be always set, so fill new input with those defaults
		Map<String, Object> defaults = new HashMap<>();
		defaults.put("landfill_type", toValue("landfill_type_national_average", "radio"));
		defaults.put("landfill_gas_recovery_collection", toValue("landfill_gas_recovery_typical_operation", "radio"));
		defaults.put("landfill_moisture", toValue("landfill_moisture_national_average", "radio"));
		defaults.put("location", toValue("location_national_average", "text"));
		defaults.put("anaerobic_digestion_type", toValue("anaerobic_digestion_wet", "radio"));
		defaults.put("anaerobic_digestion_curing", toValue("anaerobic_digestion_cured", "radio"));
		data.put("userInputs", new Gson().toJson(defaults));
	}

	private static Map<String, Object> toValue(String value, String type) {
		Map<String, Object> result = new HashMap<>();
		result.put("type", type);
		result.put("value", value);
		return result;
	}

}
