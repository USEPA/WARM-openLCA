package gov.epa.warm.html.pages.data.charts;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.FullResult;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.backend.app.App;
import gov.epa.warm.backend.data.GroupDefinitions;
import gov.epa.warm.backend.data.GroupDefinitions.Group;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

public class GroupedContributionProvider implements IChartDataProvider {

	private final ProcessDao processDao;

	public GroupedContributionProvider() {
		processDao = new ProcessDao(App.getMatrixCache().getDatabase());
	}

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
		ImpactDescriptor impact = results.getBaselineResult().impactIndex().at(0);
		ChartDataEntry[][] series = new ChartDataEntry[][] { new ChartDataEntry[groups.size()],
				new ChartDataEntry[groups.size()] };
		for (int i = 0; i < groups.size(); i++) {
			Group group = groups.get(i);
			ChartDataEntry baseline = new ChartDataEntry();
			double baselineValue = calculateGroupValue(results.getBaselineResult(), impact, group);
			baseline.setValue(ChartUtil.round(baselineValue));
			baseline.setMeta(getMetaLabel("Baseline scenario", group));
			baseline.setIdentifier(group.getName());
			series[0][i] = baseline;
			ChartDataEntry alternative = new ChartDataEntry();
			double alternativeValue = calculateGroupValue(results.getAlternativeResult(), impact, group);
			alternative.setValue(ChartUtil.round(alternativeValue));
			alternative.setMeta(getMetaLabel("Alternative scenario", group));
			alternative.setIdentifier(group.getName());
			series[1][i] = alternative;
		}
		return series;
	}

	private double calculateGroupValue(FullResult result, ImpactDescriptor impact, Group group) {
		double total = 0d;
		for (String processRefId : group.getProcessRefIds()) {
			ProcessDescriptor process = processDao.getDescriptorForRefId(processRefId);
			total += result.getDirectImpactResult(process, impact);
		}
		return total;
	}

	private String getMetaLabel(String scenario, Group group) {
		return "<strong>" + scenario + "</strong><br>Flow: " + group.getName();
	}
}
