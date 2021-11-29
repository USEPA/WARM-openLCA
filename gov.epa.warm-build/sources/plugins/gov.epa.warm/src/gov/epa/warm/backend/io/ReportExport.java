package gov.epa.warm.backend.io;

import gov.epa.warm.html.pages.data.charts.ChartUtil;
import gov.epa.warm.html.pages.data.charts.IChartDataProvider.ChartData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class ReportExport {

	private static final Logger log = LoggerFactory.getLogger(ReportExport.class);

	public static void exportAsHtml(String urlString, Map<String, String> data, OutputStream output) {
		try {
			URL url = new URL(urlString);
			try (InputStream stream = url.openStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
				StringBuilder page = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					page.append(line);
					page.append("\r\n");
				}
				int scriptTagClosed = page.lastIndexOf("</script>") + 9;
				String dataScript = getDataScripts(data);
				page.insert(scriptTagClosed, dataScript);
				OutputStreamWriter writer = new OutputStreamWriter(output);
				writer.write(page.toString());
				writer.flush();
			}
		} catch (Exception e) {
			log.error("Error during result HTML export", e);
		}
	}

	private static String getDataScripts(Map<String, String> data) {
		StringBuilder dataScript = new StringBuilder("<script>");
		dataScript.append(getReportScriptContent(data, "Summary"));
		dataScript.append("</script>");
		dataScript.append("<script>");
		dataScript.append(getReportScriptContent(data, "Analysis"));
		dataScript.append("</script>");
		dataScript.append("<script>");
		dataScript.append(getChartsScriptContent(data));
		dataScript.append("</script>");
		return dataScript.toString();
	}

	private static String getChartsScriptContent(Map<String, String> data) {
		StringBuilder script = new StringBuilder();
		script.append("var chartsLoaded = false;");
		script.append("$('.top-navigation button').on('click', function() {var button = $(this);var type = button.attr('data-type'); $('.page-container').addClass('hidden'); $('.page-container.'+type).removeClass('hidden'); if (type === 'charts' && !chartsLoaded) {setFlowContributions(); chartsLoaded = true;}});");
		script.append("window.setFlowContributions = function() {updateChart(JSON.parse('"
				+ data.get("flowContributions") + "'));};");
		String subtypeFilter = getFilter(data, "subtypeChartFilter");
		ChartData subtypeData = getChartData(data, "subtypeContributions");
		subtypeData = ChartUtil.applyFilterToSubTypes(subtypeData, subtypeFilter);
		String materialFilter = getFilter(data, "materialChartFilter");
		ChartData materialData = getChartData(data, "materialContributions");
		materialData = ChartUtil.applyFilterToMaterials(materialData, materialFilter);
		ChartData groupedData = getChartData(data, "groupedContributions");
		ChartData materialWeightData = getChartData(data, "materialWeightContributions");
		ChartData productionEOLData = getChartData(data, "productionEOLContributions");

		Gson gson = new Gson();
		script.append("window.applyFilterToSubtypeContributions = function() {updateChart(JSON.parse('"
				+ gson.toJson(subtypeData) + "'));};");
		script.append("window.applyFilterToMaterialContributions = function() {updateChart(JSON.parse('"
				+ gson.toJson(materialData) + "'));};");
		script.append("window.loadGroupedContributions = function() {updateChart(JSON.parse('"
				+ gson.toJson(groupedData) + "'));};");
		script.append("window.applyFilterToMaterialWeightContributions = function() {updateChart(JSON.parse('"
				+ gson.toJson(materialWeightData) + "'));};");
		script.append("window.applyFilterToProductionEOLContributions = function() {updateChart(JSON.parse('"
				+ gson.toJson(productionEOLData) + "'));};");
		return script.toString();
	}

	private static ChartData getChartData(Map<String, String> data, String chartId) {
		return new Gson().fromJson(data.get(chartId), ChartData.class);
	}

	private static String getFilter(Map<String, String> data, String key) {
		String filter = data.get(key);
		if (filter == null)
			filter = ChartUtil.getInitialMaterialFilter(data);
		return filter;
	}

	private static String getReportScriptContent(Map<String, String> data, String pageId) {
		String setInputs = "setInputs('" + data.get("userInputs") + "', 'element');";
		String results = data.get("results");
		String equivalents = data.get("equivalents");
		String totalBaseline = data.get("baseline_total");
		String totalAlternative = data.get("alternative_total");
		String setResults = null;
		if ("Summary".equals(pageId))
			setResults = "setSummaryResults('" + results + "', '" + equivalents + "', "
					+ totalBaseline + ", " + totalAlternative + ");";
		else
			setResults = "setAnalysisResults('" + results + "', "
					+ totalBaseline + ", " + totalAlternative + ");";
		return setInputs + setResults;
	}

}
