package gov.epa.warm.html.pages.data.charts;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.util.List;

public interface IChartDataProvider {

	static final String[] TYPES = { "Baseline scenario", "Alternative scenario" };
	static final String[] SUB_TYPES = {
			"All", "Recycling", "Landfilling", "Combustion", "Composting", "Source reduction"
	};

	ChartData getChartData(IntermediateResult results, List<ObjectMap> materials, ReportType type);

	public class ChartData {
		private String identifier;
		private List<String> labels;
		private List<String> legend;
		private ChartDataEntry[][] series;

		public List<String> getLabels() {
			return labels;
		}

		public void setLabels(List<String> labels) {
			this.labels = labels;
		}

		public List<String> getLegend() {
			return legend;
		}

		public void setLegend(List<String> legend) {
			this.legend = legend;
		}

		public String getIdentifier() {
			return identifier;
		}

		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}

		public ChartDataEntry[][] getSeries() {
			return series;
		}

		public void setSeries(ChartDataEntry[][] series) {
			this.series = series;
		}
	}

	public class ChartDataEntry {
		private double value;
		private String meta;
		private String identifier;

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
		}

		public String getIdentifier() {
			return identifier;
		}

		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}

		public String getMeta() {
			return meta;
		}

		public void setMeta(String meta) {
			this.meta = meta;
		}

	}

}
