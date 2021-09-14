package gov.epa.warm.html.pages.data.charts;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.backend.app.App;
import gov.epa.warm.backend.data.FlowRefIdMappings;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.FlowDao;
import org.openlca.core.model.Flow;

class FlowContributionProvider implements IChartDataProvider {

	private final static String FLOW_ID_CARBON_FOREST = "9ed04856-b565-46c4-8163-eb0616409abc";
	private final static String FLOW_NAME_CARBON_FOREST = "Carbon (forest storage)";
	private final static String FLOW_ID_CARBON_LANDFILL = "80bb6903-93b7-4c04-ac53-37775125af8d";
	private final static String FLOW_NAME_CARBON_LANDFILL = "Carbon (landfill storage)";
	private final static String FLOW_ID_CARBON_SOIL = "8c2fe757-6866-4ed2-9f89-81012ad774a0";
	private final static String FLOW_NAME_CARBON_SOIL = "Carbon (soil storage)";

	@Override
	public ChartData getChartData(IntermediateResult results, List<ObjectMap> materials, ReportType type) {
		List<Flow> flows = loadFlows(results, type);
		ChartData data = new ChartData();
		data.setIdentifier("flowContributions");
		data.setLabels(getLabels(results, materials, flows));
		data.setLegend(getLegend(results, materials, flows));
		data.setSeries(getSeries(results, materials, flows));
		return ChartUtil.sort(data);
	}

	private List<String> getLabels(IntermediateResult results, List<ObjectMap> materials, List<Flow> flows) {
		List<String> labels = new ArrayList<>();
		for (Flow flow : flows)
			if (flow.getRefId().equals(FLOW_ID_CARBON_FOREST))
				labels.add(FLOW_NAME_CARBON_FOREST);
			else if (flow.getRefId().equals(FLOW_ID_CARBON_LANDFILL))
				labels.add(FLOW_NAME_CARBON_LANDFILL);
			else if (flow.getRefId().equals(FLOW_ID_CARBON_SOIL))
				labels.add(FLOW_NAME_CARBON_SOIL);
			else
				labels.add(flow.getName());
		return labels;
	}

	private List<String> getLegend(IntermediateResult results, List<ObjectMap> materials, List<Flow> flows) {
		List<String> legend = new ArrayList<String>();
		legend.add("Baseline scenario");
		legend.add("Alternative scenario");
		return legend;
	}

	private ChartDataEntry[][] getSeries(IntermediateResult results, List<ObjectMap> materials, List<Flow> flows) {
		long impactId = results.getBaselineResult().getImpactIndex().getKeyAt(0);
		ChartDataEntry[][] series = new ChartDataEntry[][] {
				new ChartDataEntry[flows.size()],
				new ChartDataEntry[flows.size()]
		};
		for (int i = 0; i < flows.size(); i++) {
			Flow flow = flows.get(i);
			ChartDataEntry baseline = new ChartDataEntry();
			double baselineImpact = results.getBaselineResult().getSingleFlowImpact(flow.getId(), impactId);
			baseline.setValue(ChartUtil.round(baselineImpact));
			baseline.setMeta(getMetaLabel("Baseline scenario", flow));
			baseline.setIdentifier(flow.getName());
			series[0][i] = baseline;
			ChartDataEntry alternative = new ChartDataEntry();
			double alternativeImpact = results.getAlternativeResult().getSingleFlowImpact(flow.getId(), impactId);
			alternative.setValue(ChartUtil.round(alternativeImpact));
			alternative.setMeta(getMetaLabel("Alternative scenario", flow));
			alternative.setIdentifier(flow.getName());
			series[1][i] = alternative;
		}
		return series;
	}

	private String getMetaLabel(String scenario, Flow flow) {
		return "<strong>" + scenario + "</strong><br>Flow: " + flow.getName();
	}

	private List<Flow> loadFlows(IntermediateResult results, ReportType type) {
		List<Flow> flows = new ArrayList<>();
		long[] baselineFlowIds = results.getBaselineResult().getFlowIndex().getFlowIds();
		long[] alternativeFlowIds = results.getBaselineResult().getFlowIndex().getFlowIds();
		FlowDao flowDao = new FlowDao(App.getDatabase());
		flows = new ArrayList<>();
		for (Flow flow : flowDao.getForIds(toSet(baselineFlowIds, alternativeFlowIds)))
			if (FlowRefIdMappings.getFlowContributionRefIds(type).contains(flow.getRefId()))
				flows.add(flow);
		return flows;
	}

	private Set<Long> toSet(long[]... idArrays) {
		Set<Long> set = new HashSet<>();
		for (long[] ids : idArrays)
			for (long id : ids)
				set.add(id);
		return set;
	}

}
