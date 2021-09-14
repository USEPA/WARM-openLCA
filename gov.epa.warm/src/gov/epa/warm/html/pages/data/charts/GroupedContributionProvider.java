package gov.epa.warm.html.pages.data.charts;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.backend.app.App;
import gov.epa.warm.backend.data.GroupDefinitions;
import gov.epa.warm.backend.data.GroupDefinitions.Group;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.results.FullResult;

public class GroupedContributionProvider implements IChartDataProvider {

	@Override
	public ChartData getChartData(IntermediateResult results, List<ObjectMap> materials, ReportType type) {
		List<Group> groups = GroupDefinitions.getGroups();
		ChartData data = new ChartData();
		data.setIdentifier("groupedContributions");
		data.setLabels(getLabels(results, materials, groups));
		data.setLegend(getLegend(results, materials, groups));
		data.setSeries(getSeries(results, materials, groups));
		return ChartUtil.sort(data);
	}

	private List<String> getLabels(IntermediateResult results, List<ObjectMap> materials, List<Group> groups) {
		List<String> labels = new ArrayList<>();
		for (Group group : groups)
			labels.add(group.getName());
		return labels;
	}

	private List<String> getLegend(IntermediateResult results, List<ObjectMap> materials, List<Group> groups) {
		List<String> legend = new ArrayList<String>();
		legend.add("Baseline scenario");
		legend.add("Alternative scenario");
		return legend;
	}

	private ChartDataEntry[][] getSeries(IntermediateResult results, List<ObjectMap> materials, List<Group> groups) {
		long impactId = results.getBaselineResult().getImpactIndex().getKeyAt(0);
		ChartDataEntry[][] series = new ChartDataEntry[][] {
				new ChartDataEntry[groups.size()],
				new ChartDataEntry[groups.size()]
		};
		for (int i = 0; i < groups.size(); i++) {
			Group group = groups.get(i);
			ChartDataEntry baseline = new ChartDataEntry();
			double baselineValue = calculateGroupValue(results.getBaselineResult(), impactId, group);
			baseline.setValue(ChartUtil.round(baselineValue));
			baseline.setMeta(getMetaLabel("Baseline scenario", group));
			baseline.setIdentifier(group.getName());
			series[0][i] = baseline;
			ChartDataEntry alternative = new ChartDataEntry();
			double alternativeValue = calculateGroupValue(results.getAlternativeResult(), impactId, group);
			alternative.setValue(ChartUtil.round(alternativeValue));
			alternative.setMeta(getMetaLabel("Alternative scenario", group));
			alternative.setIdentifier(group.getName());
			series[1][i] = alternative;
		}
		return series;
	}

	private double calculateGroupValue(FullResult result, long impactId, Group group) {
		double total = 0d;
		for (String processRefId : group.getProcessRefIds()) {
			long processId = App.getProcessIdMap().get(processRefId);
			total += result.getSingleImpactResult(processId, impactId);
		}
		return total;
	}

	private String getMetaLabel(String scenario, Group group) {
		return "<strong>" + scenario + "</strong><br>Flow: " + group.getName();
	}
}
