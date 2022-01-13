package gov.epa.warm.html.pages.data.charts;

import java.util.ArrayList;
import java.util.List;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

class MaterialWeightProvider implements IChartDataProvider {

	@Override
	public ChartData getChartData(IntermediateResult results, List<ObjectMap> materials, ReportType type) {
		ChartData data = new ChartData();
		data.setIdentifier("materialWeightContributions");
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
		legend.add("Baseline");
		return legend;
	}

	private ChartDataEntry[][] getSeries(IntermediateResult results, List<ObjectMap> materials) {
		ChartDataEntry[][] values = new ChartDataEntry[1][materials.size()];
		int i = 0;
		for (ObjectMap material : materials) {
			ChartDataEntry entry = new ChartDataEntry();
			entry.setValue(ChartUtil.round(material.getDouble("baseline")));
			entry.setMeta("Metric tons");
			entry.setIdentifier(material.getString("name"));
			values[0][i++] = entry;
		}
		return values;
	}

}
