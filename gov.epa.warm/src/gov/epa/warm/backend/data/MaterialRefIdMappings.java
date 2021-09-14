package gov.epa.warm.backend.data;

import gov.epa.warm.backend.data.mapping.ConditionalMapping;
import gov.epa.warm.backend.data.mapping.MaterialRefIdMapping;
import gov.epa.warm.backend.data.parser.MaterialRefIdMappingParser;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;
import gov.epa.warm.rcp.utils.Rcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialRefIdMappings {

	private static List<ConditionalMapping<MaterialRefIdMapping>> mappingsCo2;
	private static List<ConditionalMapping<MaterialRefIdMapping>> mappingsEnergy;

	public static Map<String, String> forBaseline(ObjectMap choices, ReportType type) {
		Map<String, String> map = new HashMap<>();
		for (ConditionalMapping<MaterialRefIdMapping> mapping : getMappings(type))
			if (mapping.matches(choices))
				map.put(mapping.getMapped().getMaterial(), mapping.getMapped().getBaselineRefId());
		return map;
	}

	public static Map<String, String> forAlternative(ObjectMap choices, ReportType type) {
		Map<String, String> map = new HashMap<>();
		for (ConditionalMapping<MaterialRefIdMapping> mapping : getMappings(type))
			if (mapping.matches(choices))
				map.put(mapping.getMapped().getMaterial(), mapping.getMapped().getAlternativeRefId());
		return map;
	}

	private static List<ConditionalMapping<MaterialRefIdMapping>> getMappings(ReportType reportType) {
		try {
			if (reportType == ReportType.ENERGY) {
				File file = new File(Rcp.getWorkspace(), "mappings/material_to_ref_ids_energy.txt");
				if (mappingsEnergy == null)
					mappingsEnergy = MaterialRefIdMappingParser.parse(new FileInputStream(file));
				return mappingsEnergy;
			}
			File file = new File(Rcp.getWorkspace(), "mappings/material_to_ref_ids_co2.txt");
			if (mappingsCo2 == null)
				mappingsCo2 = MaterialRefIdMappingParser.parse(new FileInputStream(file));
			return mappingsCo2;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
