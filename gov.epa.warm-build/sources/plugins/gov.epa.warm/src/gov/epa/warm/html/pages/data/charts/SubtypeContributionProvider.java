package gov.epa.warm.html.pages.data.charts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

class SubtypeContributionProvider implements IChartDataProvider {
	
	private final static Logger log = LoggerFactory.getLogger(SubtypeContributionProvider.class);

	@Override
	public ChartData getChartData(IntermediateResult results, List<ObjectMap> materials, ReportType type) {
		ChartData data = new ChartData();
		log.debug(results.toString());
		data.setIdentifier("subtypeContributions");
		data.setLabels(getLabels(results, materials));
		data.setLegend(getLegend(results, materials));
		data.setSeries(getSeries(results, materials));
		log.debug(data.toString());
		return ChartUtil.sort(data);
	}

	private List<String> getLabels(IntermediateResult results, List<ObjectMap> materials) {
		return Arrays.asList(SUB_TYPES);
	}

	private List<String> getLegend(IntermediateResult results,
			List<ObjectMap> materials) {
		List<String> legend = new ArrayList<>();
		for (String scenario : new String[] { "Baseline", "Alternative" })
			for (ObjectMap material : materials)
				legend.add(scenario + " - " + material.getString("name"));
		return legend;
	}

	private ChartDataEntry[][] getSeries(IntermediateResult results, List<ObjectMap> materials) {
		ChartDataEntry[][] values = new ChartDataEntry[2 * materials.size()][];
		for (int i = 0; i < TYPES.length; i++) {
			int j = 0;
			for (ObjectMap material : materials) {
				values[j + materials.size() * i] = new ChartDataEntry[SUB_TYPES.length];
				for (int k = 0; k < SUB_TYPES.length; k++) {
					String key = getKeyForSubType(TYPES[i], SUB_TYPES[k]);
					ChartDataEntry entry = new ChartDataEntry();
					entry.setValue(ChartUtil.round(material.getDouble(key)));
					entry.setMeta(getMetaLabel(TYPES[i], SUB_TYPES[k], material.getString("name")));
					entry.setIdentifier(material.getString("name"));
					values[j + materials.size() * i][k] = entry;
				}
				j++;
			}
		}
		return values;
	}

	private String getMetaLabel(String scenario, String subtype, String material) {
		return "<strong>" + scenario + "</strong><br>Waste treatment: " + subtype + "<br>Material: " + material;
	}

	private String getKeyForSubType(String type, String subType) {
		type = type.substring(0, type.indexOf(' ')).toLowerCase();
		StringBuilder key = new StringBuilder(type);
		if (!subType.equals("All")) {
			key.append("_");
			key.append(subType.toLowerCase().replace(" ", "_"));
		}
		key.append("_result");
		return key.toString();
	}

}
