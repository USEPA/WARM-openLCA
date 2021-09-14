package gov.epa.warm.html.pages.data;

import gov.epa.warm.backend.MaterialCalculator;
import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.backend.data.MapUtil;
import gov.epa.warm.backend.data.out.MaterialMapper;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.openlca.core.results.FullResult;

import com.google.gson.Gson;

public class ReportData {

	public static void prepareResults(ReportType type, IntermediateResult results,
			ObjectMap inputData, ObjectMap choices, Map<String, String> data) {
		List<ObjectMap> materials = convertAndFillMaterials(results, inputData, choices, type);
		double baselineTotal = getTotalResult(results.getBaselineResult());
		double alternativeTotal = getTotalResult(results.getAlternativeResult());
		ObjectMap equivalents = calculateEquivalents(baselineTotal, alternativeTotal, type);
		data.put("type", type.name());
		data.put("userInputs", new Gson().toJson(inputData));
		data.put("results", new Gson().toJson(materials));
		data.put("equivalents", new Gson().toJson(equivalents));
		data.put("baseline_total", Double.toString(baselineTotal));
		data.put("alternative_total", Double.toString(alternativeTotal));
	}

	private static double getTotalResult(FullResult result) {
		return result.getTotalImpactResult(result.getImpactIndex().getKeyAt(0));
	}

	private static List<ObjectMap> convertAndFillMaterials(IntermediateResult results,
			ObjectMap inputData, ObjectMap choices, ReportType type) {
		List<ObjectMap> materials = new ArrayList<>();
		ObjectMap materialInputs = ObjectMap.fromMap(inputData.get("materials"));
		if (materialInputs != null) {
			MaterialMapper.putResults(results, materialInputs, choices, type);
			for (String index : materialInputs.keySet()) {
				ObjectMap material = ObjectMap.fromMap(materialInputs.get(index));
				material.put("index", index);
				boolean hasInputs = false;
				for (String key : material.keySet())
					if (MapUtil.isMaterialInput(key))
						if (material.getDouble(key) != 0d) {
							hasInputs = true;
							break;
						}
				if (hasInputs)
					materials.add(material);
			}
		}
		inputData.remove("materials");
		applyMaterialSpecificFormulas(materialInputs, materials, type);
		Collections.sort(materials, new MaterialComparator());
		return materials;
	}

	private static void applyMaterialSpecificFormulas(ObjectMap materialInputs, List<ObjectMap> materialResults,
			ReportType type) {
		new MaterialCalculator(materialInputs, materialResults).applyFormulas(type);
	}

	private interface FACTORS {
		interface MTCO2E {
			double VEHICLES = 4.75;
			double GASOLINE = 0.008887;
			double BARBEQUES = 0.024;
			double RAILWAYS = 186.5;
			double ANNUAL_TRANSPORTATION = 1739500000;
			double ANNUAL_ENERGY = 2022700000;
		}

		interface MTCE {
			double mtco_factor = 44d / 12d;
			double VEHICLES = MTCO2E.VEHICLES / mtco_factor;
			double GASOLINE = MTCO2E.GASOLINE / mtco_factor;
			double BARBEQUES = MTCO2E.BARBEQUES / mtco_factor;
			double RAILWAYS = MTCO2E.RAILWAYS / mtco_factor;
			double ANNUAL_TRANSPORTATION = MTCO2E.ANNUAL_TRANSPORTATION / mtco_factor;
			double ANNUAL_ENERGY = MTCO2E.ANNUAL_ENERGY / mtco_factor;
		}

		interface ENERGY {
			double HOUSEHOLDS = 110.02649;
			double OIL = 5.81;
			double GASOLINE = 0.124238095238095;
		}
	}

	private static ObjectMap calculateEquivalents(double baselineTotal, double alternativeTotal, ReportType type) {
		double change = alternativeTotal - baselineTotal;
		ObjectMap map = new ObjectMap();
		switch (type) {
		case MTCO2E:
			map.put("vehicles", divide(change, FACTORS.MTCO2E.VEHICLES));
			map.put("gasoline", divide(change, FACTORS.MTCO2E.GASOLINE));
			map.put("barbeques", divide(change, FACTORS.MTCO2E.BARBEQUES));
			map.put("railways", divide(change, FACTORS.MTCO2E.RAILWAYS));
			map.put("annual_transportation", divide(change, FACTORS.MTCO2E.ANNUAL_TRANSPORTATION));
			map.put("annual_energy", divide(change, FACTORS.MTCO2E.ANNUAL_ENERGY));
			break;
		case MTCE:
			map.put("vehicles", divide(change, FACTORS.MTCE.VEHICLES));
			map.put("gasoline", divide(change, FACTORS.MTCE.GASOLINE));
			map.put("barbeques", divide(change, FACTORS.MTCE.BARBEQUES));
			map.put("railways", divide(change, FACTORS.MTCE.RAILWAYS));
			map.put("annual_transportation", divide(change, FACTORS.MTCE.ANNUAL_TRANSPORTATION));
			map.put("annual_energy", divide(change, FACTORS.MTCE.ANNUAL_ENERGY));
			break;
		case ENERGY:
			map.put("households", divide(change, FACTORS.ENERGY.HOUSEHOLDS));
			map.put("oil", divide(change, FACTORS.ENERGY.OIL));
			map.put("gasoline", divide(change, FACTORS.ENERGY.GASOLINE));
			break;
		}
		return map;
	}

	private static String divide(double change, double factor) {
		return Double.toString(change / factor);
	}

	private static class MaterialComparator implements Comparator<ObjectMap> {

		@Override
		public int compare(ObjectMap o1, ObjectMap o2) {
			long index1 = o1.getLong("index");
			long index2 = o2.getLong("index");
			return Long.compare(index1, index2);
		}

	}

}
