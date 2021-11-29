package gov.epa.warm.html.pages.data.charts;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.util.ArrayList;
import java.util.List;

class MaterialContributionProvider implements IChartDataProvider {

	@Override
	public ChartData getChartData(IntermediateResult results, List<ObjectMap> materials, ReportType type) {
		ChartData data = new ChartData();
		data.setIdentifier("materialContributions");
		data.setLabels(getLabels(results, materials));
		data.setLegend(getLegend(results, materials));
		data.setSeries(getSeries(results, materials));
		return ChartUtil.sort(data);
	}

	private List<String> getLabels(IntermediateResult results, List<ObjectMap> materials) {
		List<String> labels = new ArrayList<>();
		for (ObjectMap materialResult : materials) {
			String name = materialResult.getString("name");
			labels.add(name);
		}
		return labels;
	}

	private List<String> getLegend(IntermediateResult results, List<ObjectMap> materials) {
		List<String> legend = new ArrayList<>();
		for (String scenario : new String[] { "Baseline", "Alternative" })
			for (String subType : SUB_TYPES)
				legend.add(scenario + " - " + subType);
		return legend;
	}

	private ChartDataEntry[][] getSeries(IntermediateResult results, List<ObjectMap> materials) {
		ChartDataEntry[][] values = new ChartDataEntry[2 * SUB_TYPES.length][];
		for (int i = 0; i < TYPES.length; i++)
			for (int j = 0; j < SUB_TYPES.length; j++) {
				values[j + SUB_TYPES.length * i] = new ChartDataEntry[materials.size()];
				int k = 0;
				for (ObjectMap material : materials) {
					String key = getKeyForSubType(TYPES[i], SUB_TYPES[j]);
					ChartDataEntry entry = new ChartDataEntry();
					entry.setValue(ChartUtil.round(material.getDouble(key)));
					entry.setMeta(getMetaLabel(TYPES[i], SUB_TYPES[j], material.getString("name")));
					entry.setIdentifier(SUB_TYPES[j]);
					values[j + SUB_TYPES.length * i][k] = entry;
					k++;
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
