package gov.epa.warm.backend.data.in;

import gov.epa.warm.backend.data.MapUtil;
import gov.epa.warm.rcp.utils.ObjectMap;

public class ChoiceMapper {

	public static ObjectMap mapChoices(ObjectMap input) {
		ObjectMap choices = new ObjectMap();
		mapLandfillType(input, choices);
		MapUtil.move(input, choices, "landfill_gas_recovery_collection");
		MapUtil.move(input, choices, "landfill_moisture");
		MapUtil.move(input, choices, "location");
		MapUtil.move(input, choices, "anaerobic_digestion_type");  
		MapUtil.move(input, choices, "anaerobic_digestion_curing");
		return choices;
	}

	private static void mapLandfillType(ObjectMap input, ObjectMap choices) {
		if ("landfill_type_lfg_recovery".equals(input.getString("landfill_type.value")))
			MapUtil.move(input, "landfill_subtype", choices, "landfill_type", null);
		else
			MapUtil.move(input, "landfill_type", choices, "landfill_type", null);
	}

}
