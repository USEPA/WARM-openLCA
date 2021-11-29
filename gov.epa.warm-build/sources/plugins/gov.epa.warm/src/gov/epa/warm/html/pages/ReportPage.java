package gov.epa.warm.html.pages;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.backend.io.ReportExport;
import gov.epa.warm.html.HtmlEditor;
import gov.epa.warm.html.HtmlEditorInput;
import gov.epa.warm.html.PageDescriptor;
import gov.epa.warm.html.pages.data.ReportData;
import gov.epa.warm.html.pages.data.charts.ChartUtil;
import gov.epa.warm.html.pages.data.charts.Charts;
import gov.epa.warm.html.pages.data.charts.IChartDataProvider.ChartData;
import gov.epa.warm.rcp.utils.Editors;
import gov.epa.warm.rcp.utils.FileChooser;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.eclipse.ui.IEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class ReportPage extends HtmlEditor {

	private static final Logger log = LoggerFactory.getLogger(ReportPage.class);
	public static final String ID = "gov.epa.warm.ReportPage";

	public static void open(ReportType type, IntermediateResult results, ObjectMap inputData, ObjectMap choices) {
		HtmlEditorInput input = new HtmlEditorInput(UUID.randomUUID().toString());
		input.getData().put("reportType", type.name());
		ReportData.prepareResults(type, results, inputData, choices, input.getData());
		Charts.prepareResults(results, inputData, input.getData(), type);
		Editors.open(input, ReportPage.ID);
	}

	@Override
	protected PageDescriptor[] getPageDescriptors() {
		ReportType type = ReportType.valueOf(getData().get("type"));
		String summaryPage = "summary-" + type.name().toLowerCase() + ".html";
		String analysisPage = "analysis-" + type.name().toLowerCase() + ".html";
		String chartsPage = "charts.html";
		String productionEolPage = "production-" + type.name().toLowerCase() + ".html";
		if (type == ReportType.ENERGY || type == ReportType.MTCE || type == ReportType.MTCO2E) {
			return new PageDescriptor[] { new PageDescriptor("Summary", summaryPage),
					new PageDescriptor("Analysis", analysisPage),
					new PageDescriptor("Production + EOL", productionEolPage),
					new PageDescriptor("Charts", chartsPage) };
		} else {
			return new PageDescriptor[] { new PageDescriptor("Summary", summaryPage),
					new PageDescriptor("Analysis", analysisPage) };
		}
	}

	@Override
	protected void onLoaded(PageDescriptor page) {
		registerFunction("applyFilterToSubtypeContributions", this::applyFilterToSubtypeContributions);
		registerFunction("applyFilterToMaterialContributions", this::applyFilterToMaterialContributions);
		registerFunction("applyFilterToMaterialWeightContributions", this::applyFilterToMaterialWeightContributions);
		registerFunction("loadGroupedContributions", this::loadGroupedContributions);
		registerFunction("loadProductionEOLContributions", this::loadProductionEOLContributions);
		call("setInputs", getData().get("userInputs"), "element");
		if ("Summary".equals(page.getId()))
			onSummaryPageLoaded();
		else if ("Analysis".equals(page.getId()))
			onAnalysisPageLoaded();
		else if ("Charts".equals(page.getId()))
			onChartsPageLoaded();
		else if ("Production + EOL".equals(page.getId()))
			onProductionPageLoaded();
	}

	private ReportType getReportType() {
		String reportType = getData().get("reportType");
		return ReportType.valueOf(reportType);
	}

	private void onSummaryPageLoaded() {
		String results = getData().get("results");
		String equivalents = getData().get("equivalents");
		String totalBaseline = getData().get("baseline_total");
		String totalAlternative = getData().get("alternative_total");
		call("setSummaryResults", results, equivalents, totalBaseline, totalAlternative);
	}

	private void onProductionPageLoaded() {
		String results = getData().get("results");
		String totalBaseline = getData().get("baseline_total");
		String totalAlternative = getData().get("alternative_total");
		log.debug("setProductionResults(" + results + ", " + totalBaseline + ", " + totalAlternative + ")");
		call("setProductionResults", results, /* equivalents, */ totalBaseline, totalAlternative);
	}

	private void onAnalysisPageLoaded() {
		String results = getData().get("results");
		String totalBaseline = getData().get("baseline_total");
		String totalAlternative = getData().get("alternative_total");
		log.debug("setAnalysisResults(" + results + ", " + totalBaseline + ", " + totalAlternative + ")");
		call("setAnalysisResults", results, totalBaseline, totalAlternative);
	}

	private void onChartsPageLoaded() {
		String results = getData().get("results");
		call("setAvailableMaterials", results);
		ChartData flowContributions = new Gson().fromJson(getData().get("flowContributions"), ChartData.class);
		call("updateChart", flowContributions);
		call("updateChartLabels", getData().get("reportType"));
	}

	public static void exportAsHtml() {
		IEditorPart part = Editors.getActive();
		if (!(part instanceof ReportPage))
			return;
		ReportPage page = (ReportPage) part;
		String type = page.getReportType().name().toLowerCase();
		File file = FileChooser.forSaving("*.html", "warm_results.html");
		try (FileOutputStream output = new FileOutputStream(file)) {
			String url = getUrl("for-export/all-" + type + ".html");
			ReportExport.exportAsHtml(url, page.getData(), output);
		} catch (IOException e) {
			log.error("Error exporting report as HTML", e);
		}
	}

	private void applyFilterToSubtypeContributions(String selection) {
		if (selection == null || selection.isEmpty())
			selection = ChartUtil.getInitialMaterialFilter(getData());
		getData().put("subtypeChartFilter", selection);
		ChartData chartData = new Gson().fromJson(getData().get("subtypeContributions"), ChartData.class);
		ChartData filtered = ChartUtil.applyFilterToSubTypes(chartData, selection);
		call("updateChart", filtered);
	}

	private void applyFilterToMaterialContributions(String selection) {
		if (selection == null || selection.isEmpty())
			selection = ChartUtil.getInitialMaterialFilter(getData());
		getData().put("materialChartFilter", selection);
		ChartData chartData = new Gson().fromJson(getData().get("materialContributions"), ChartData.class);
		ChartData filtered = ChartUtil.applyFilterToMaterials(chartData, selection);
		call("updateChart", filtered);
	}

	private void loadProductionEOLContributions(String selection) {
		if (selection == null || selection.isEmpty())
			selection = ChartUtil.getInitialMaterialFilter(getData());
		getData().put("productionEOLFilter", selection);
		ChartData chartData = new Gson().fromJson(getData().get("productionEOLContributions"), ChartData.class);
		call("updateChart", chartData);
	}

	private void applyFilterToMaterialWeightContributions(String selection) {
		if (selection == null || selection.isEmpty())
			selection = ChartUtil.getInitialMaterialFilter(getData());
		getData().put("materialWeightChartFilter", selection);
		ChartData chartData = new Gson().fromJson(getData().get("materialWeightContributions"), ChartData.class);
		ChartData filtered = ChartUtil.applyFilterToMaterials(chartData, selection);
		call("updateChart", filtered);
	}

	private void loadGroupedContributions() {
		ChartData chartData = new Gson().fromJson(getData().get("groupedContributions"), ChartData.class);
		call("updateChart", chartData);
	}

	public static enum ReportType {
		MTCO2E, MTCE, ENERGY, JOBS, WAGES, TAXES;
	}

}
