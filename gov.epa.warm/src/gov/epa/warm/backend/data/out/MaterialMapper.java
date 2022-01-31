package gov.epa.warm.backend.data.out;

import java.util.Map;

import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.FullResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.backend.app.App;
import gov.epa.warm.backend.data.MapUtil;
import gov.epa.warm.backend.data.MaterialRefIdMappings;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

public class MaterialMapper {

	private static final Logger log = LoggerFactory.getLogger(MaterialMapper.class);
	private static final String[] SUB_TYPES = { "", "recycling", "landfilling", "combustion", "composting",
			"anaerobic_digestion", "source_reduction" };
	private static final ProcessDao processDao = new ProcessDao(App.getMatrixCache().getDatabase());

	public static void putResults(IntermediateResult results, ObjectMap materials, ObjectMap choices, ReportType type) {
		Map<String, String> baselineMappings = MaterialRefIdMappings.forBaseline(choices, type);
		Map<String, String> alternativeMappings = MaterialRefIdMappings.forAlternative(choices, type);
		for (String index : materials.keySet()) {
			putValues("baseline", materials, index, results.getBaselineResult(), baselineMappings);
			putValues("alternative", materials, index, results.getAlternativeResult(), alternativeMappings);
			putValues("per_ton", materials, index, results.getPerTonResult(), alternativeMappings);
			double baseline = materials.getDouble(index + ".baseline_result");
			double alternative = materials.getDouble(index + ".alternative_result");
			double change = alternative - baseline;
			materials.put(index + ".change", Double.toString(change));
		}
	}

	private static void putValues(String type, ObjectMap materials, String index, FullResult result,
			Map<String, String> refIdMappings) {
		for (String subtype : SUB_TYPES) {
			if (type.equals("baseline") && subtype.equals("source_reduction"))
				continue;
			if (materials.getBoolean(index + ".disabled." + subtype))
				continue;
			String name = (String) materials.get(index + ".name");
			double value = getValue(name, type, subtype, result, refIdMappings);
			materials.put(getKey(index, type, subtype, "result"), Double.toString(value));
			calculateAndPutIncrements(materials, index);
		}
	}

	private static void calculateAndPutIncrements(ObjectMap materials, String index) {
		calculateAndPutIncrement(materials, index, "source_reduction");
		calculateAndPutIncrement(materials, index, "recycling");
		calculateAndPutIncrement(materials, index, "landfilling");
		calculateAndPutIncrement(materials, index, "combustion");
		calculateAndPutIncrement(materials, index, "composting");
		calculateAndPutIncrementDigested(materials, index, "anaerobic_digestion");
		calculateAndPutIncrement(materials, index, "");
	}

	private static void calculateAndPutIncrement(ObjectMap materials, String index, String type) {
		String key = getKey(index, "baseline", type, "");
		log.debug("key=" + key + ", index=" + index + ", type=" + type);
		double baselineInput = materials.getDouble(key);
		key = getKey(index, "alternative", type, "");
		double alternativeInput = materials.getDouble(key);
		key = getKey(index, "baseline", type, "result");
		double baselineOutput = materials.getDouble(key);
		key = getKey(index, "alternative", type, "result");
		double alternativeOutput = materials.getDouble(key);
		key = getKey(index, "", type, "input_change");
		materials.put(key, Double.toString(alternativeInput - baselineInput));
		key = getKey(index, "", type, "output_change");
		materials.put(key, Double.toString(alternativeOutput - baselineOutput));
	}

	private static void calculateAndPutIncrementDigested(ObjectMap materials, String index, String type) {
		String key = getKey(index, "baseline", type, "");
		log.debug("key=" + key + ", index=" + index + ", type=" + type);
		double baselineInput = materials.getDouble(key);
		key = getKey(index, "alternative", type, "");
		double alternativeInput = materials.getDouble(key);
		key = getKey(index, "baseline", type, "result");
		double baselineOutput = materials.getDouble(key);
		key = getKey(index, "alternative", type, "result");
		double alternativeOutput = materials.getDouble(key);
		key = getKey(index, "", type, "input_change");
		materials.put(key, Double.toString(alternativeInput - baselineInput));
		key = getKey(index, "", type, "output_change");
		materials.put(key, Double.toString(alternativeOutput - baselineOutput));
	}

	private static String getKey(String index, String type, String subtype, String suffix) {
		String key = index + ".";
		boolean first = true;
		if (type != null && !type.isEmpty()) {
			key += type;
			first = false;
		}
		if (subtype != null && !subtype.isEmpty()) {
			if (!first)
				key += "_";
			key += subtype;
			first = false;
		}
		if (suffix != null && !suffix.isEmpty()) {
			if (!first)
				key += "_";
			key += suffix;
			first = false;
		}
		return key;
	}

	private static double getValue(String material, String type, String subType, FullResult result,
			Map<String, String> refIdMappings) {
		material = MapUtil.convertMaterial(material);
		if (subType != null && !subType.isEmpty())
			material += "_" + subType;
		String refId = refIdMappings.get(material);
		if (refId == null) {
			log.warn("Could not find reference id for " + material);
			return 0;
		}
		long id = App.getProcessIdMap().get(refId);
		if (id == 0l) {
			log.warn("Could not find process for reference id " + refId + " for material " + type + ": " + material);
			return 0;
		}
		ProcessDescriptor process = processDao.getDescriptorForRefId(refId);
		// only one category in each method -> impact at index 0
		return result.getUpstreamImpactResult(process, result.getImpacts().get(0));

	}

}
