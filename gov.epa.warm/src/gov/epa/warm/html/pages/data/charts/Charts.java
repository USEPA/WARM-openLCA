package gov.epa.warm.html.pages.data.charts;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.html.pages.data.charts.IChartDataProvider.ChartData;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class Charts {

	private static List<IChartDataProvider> chartDataProviders = new ArrayList<>();

	static {
		chartDataProviders.add(new FlowContributionProvider());
		chartDataProviders.add(new SubtypeContributionProvider());
		chartDataProviders.add(new MaterialContributionProvider());
		chartDataProviders.add(new GroupedContributionProvider());
		chartDataProviders.add(new MaterialWeightProvider());
		chartDataProviders.add(new ProductionEOLProvider());
	}

	public static void prepareResults(IntermediateResult results, ObjectMap inputData, Map<String, String> data,
			ReportType type) {
		List<ObjectMap> materialResults = ObjectMap.fromJsonArray(data.get("results"));
		for (IChartDataProvider provider : chartDataProviders) {
			ChartData chart = provider.getChartData(results, materialResults, type);
			data.put(chart.getIdentifier(), new Gson().toJson(chart));
		}
	}

}
