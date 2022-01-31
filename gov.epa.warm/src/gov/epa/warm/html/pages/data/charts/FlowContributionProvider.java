package gov.epa.warm.html.pages.data.charts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.backend.data.FlowRefIdMappings;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

class FlowContributionProvider implements IChartDataProvider {

	private final static String FLOW_ID_CARBON_FOREST = "9ed04856-b565-46c4-8163-eb0616409abc";
	private final static String FLOW_NAME_CARBON_FOREST = "Carbon (forest storage)";
	private final static String FLOW_ID_CARBON_LANDFILL = "80bb6903-93b7-4c04-ac53-37775125af8d";
	private final static String FLOW_NAME_CARBON_LANDFILL = "Carbon (landfill storage)";
	private final static String FLOW_ID_CARBON_SOIL = "8c2fe757-6866-4ed2-9f89-81012ad774a0";
	private final static String FLOW_NAME_CARBON_SOIL = "Carbon (soil storage)";

	@Override
	public ChartData getChartData(IntermediateResult results, List<ObjectMap> materials, ReportType type) {
		List<EnviFlow> flows = loadFlows(results, type);
		ChartData data = new ChartData();
		data.setIdentifier("flowContributions");
		data.setLabels(getLabels(results, materials, flows));
		data.setLegend(getLegend());
		data.setSeries(getSeries(results, flows));
		return ChartUtil.sort(data);
	}

	private List<String> getLabels(IntermediateResult results, List<ObjectMap> materials, List<EnviFlow> flows) {
		List<String> labels = new ArrayList<>();
		for (EnviFlow enviFlow : flows) {
			FlowDescriptor flow = enviFlow.flow();
			if (flow.refId.equals(FLOW_ID_CARBON_FOREST))
				labels.add(FLOW_NAME_CARBON_FOREST);
			else if (flow.refId.equals(FLOW_ID_CARBON_LANDFILL))
				labels.add(FLOW_NAME_CARBON_LANDFILL);
			else if (flow.refId.equals(FLOW_ID_CARBON_SOIL))
				labels.add(FLOW_NAME_CARBON_SOIL);
			else
				labels.add(flow.name);
		}
		return labels;
	}

	private List<String> getLegend() {
		List<String> legend = new ArrayList<String>();
		legend.add("Baseline scenario");
		legend.add("Alternative scenario");
		return legend;
	}

	private ChartDataEntry[][] getSeries(IntermediateResult results, List<EnviFlow> flows) {
		ImpactDescriptor impact = results.getBaselineResult().impactIndex().at(0);
		ChartDataEntry[][] series = new ChartDataEntry[][] { new ChartDataEntry[flows.size()],
				new ChartDataEntry[flows.size()] };
		for (int i = 0; i < flows.size(); i++) {
			EnviFlow enviFlow = flows.get(i);
			FlowDescriptor flow = enviFlow.flow();
			ChartDataEntry baseline = new ChartDataEntry();

			double baselineImpact = results.getBaselineResult().getDirectFlowImpact(enviFlow, impact);
			baseline.setValue(ChartUtil.round(baselineImpact));
			baseline.setMeta(getMetaLabel("Baseline scenario", flow));
			baseline.setIdentifier(flow.name);
			series[0][i] = baseline;
			ChartDataEntry alternative = new ChartDataEntry();
			double alternativeImpact = results.getAlternativeResult().getDirectFlowImpact(enviFlow, impact);
			alternative.setValue(ChartUtil.round(alternativeImpact));
			alternative.setMeta(getMetaLabel("Alternative scenario", flow));
			alternative.setIdentifier(flow.name);
			series[1][i] = alternative;
		}
		return series;
	}

	private String getMetaLabel(String scenario, FlowDescriptor flow) {
		return "<strong>" + scenario + "</strong><br>Flow: " + flow.name;
	}

	@SuppressWarnings("unchecked")
	private List<EnviFlow> loadFlows(IntermediateResult results, ReportType type) {
		List<EnviFlow> flows = new ArrayList<>();
		List<EnviFlow> baselineFlows = results.getBaselineResult().getFlows();
		List<EnviFlow> alternativeFlows = results.getAlternativeResult().getFlows();
		flows = new ArrayList<>();
		for (EnviFlow flow : toSet(baselineFlows, alternativeFlows))
			if (FlowRefIdMappings.getFlowContributionRefIds(type).contains(flow.flow().refId))
				flows.add(flow);
		return flows;
	}

	@SuppressWarnings("unchecked")
	private Set<EnviFlow> toSet(List<EnviFlow>... arrays) {
		Set<EnviFlow> set = new HashSet<>();
		for (List<EnviFlow> array : arrays)
			for (EnviFlow item : array) {
				set.add(item);
			}
		return set;
	}

}
