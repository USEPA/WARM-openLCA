package gov.epa.warm.html.pages.data.charts;

import gov.epa.warm.html.pages.data.charts.IChartDataProvider.ChartData;
import gov.epa.warm.html.pages.data.charts.IChartDataProvider.ChartDataEntry;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ChartUtil {

	private static DecimalFormat NUMBER_FORMAT = new DecimalFormat("#.##");

	public static double round(double number) {
		try {
			return NUMBER_FORMAT.parse(NUMBER_FORMAT.format(number)).doubleValue();
		} catch (ParseException e) {
			return number;
		}
	}

	public static String getInitialMaterialFilter(Map<String, String> data) {
		List<String> materials = getMaterials(data);
		String materialFilter = "";
		int amount = 6;
		if (materials.size() < 6)
			amount = materials.size();
		for (int i = 0; i < amount; i++) {
			if (i != 0)
				materialFilter += '@';
			materialFilter += materials.get(i);
		}
		return materialFilter;
	}

	private static List<String> getMaterials(Map<String, String> data) {
		List<ObjectMap> results = ObjectMap.fromJsonArray(data.get("results"));
		List<String> materials = new ArrayList<>();
		for (ObjectMap result : results)
			materials.add(result.getString("name"));
		return materials;
	}

	public static ChartData applyFilterToSubTypes(ChartData chartData, String selection) {
		List<String> filter = Arrays.asList(selection.split("@"));
		ChartDataEntry[][] entries = new ChartDataEntry[filter.size() * 2][];
		int count = 0;
		for (ChartDataEntry[] entry : chartData.getSeries())
			if (filter.contains(entry[0].getIdentifier())) {
				entries[count] = new ChartDataEntry[entry.length];
				for (int j = 0; j < entry.length; j++)
					entries[count][j] = entry[j];
				count++;
			}
		List<String> legend = new ArrayList<>();
		for (String scenario : new String[] { "Baseline", "Alternative" })
			for (String material : filter)
				legend.add(scenario + " - " + material);
		ChartData filtered = new ChartData();
		filtered.setIdentifier(chartData.getIdentifier());
		filtered.setLabels(new ArrayList<>(chartData.getLabels()));
		filtered.setLegend(legend);
		filtered.setSeries(entries);
		return filtered;
	}

	public static ChartData applyFilterToMaterials(ChartData chartData, String selection) {
		List<String> filter = Arrays.asList(selection.split("@"));
		List<Integer> remainingIndices = new ArrayList<>();
		for (int i = 0; i < chartData.getLabels().size(); i++)
			if (filter.contains(chartData.getLabels().get(i)))
				remainingIndices.add(i);
		List<String> labels = new ArrayList<>();
		ChartDataEntry[][] entries = new ChartDataEntry[chartData.getSeries().length][];
		for (int i = 0; i < entries.length; i++)
			entries[i] = new ChartDataEntry[remainingIndices.size()];
		for (int index : remainingIndices) {
			labels.add(chartData.getLabels().get(index));
			for (int i = 0; i < chartData.getSeries().length; i++)
				for (int j = 0; j < remainingIndices.size(); j++)
					entries[i][j] = chartData.getSeries()[i][remainingIndices.get(j)];
		}
		ChartData filtered = new ChartData();
		filtered.setIdentifier(chartData.getIdentifier());
		filtered.setLabels(labels);
		filtered.setLegend(new ArrayList<>(chartData.getLegend()));
		filtered.setSeries(entries);
		return filtered;
	}

	public static ChartData sort(ChartData data) {
		int indices = data.getSeries()[0].length;
		List<SortElement> sortable = new ArrayList<>();
		for (int i = 0; i < indices; i++) {
			SortElement element = new SortElement();
			element.oldIndex = i;
			element.value = getSeriesValue(data.getSeries(), i);
			sortable.add(element);
		}
		Collections.sort(sortable, new DataComparator());
		List<String> labels = new ArrayList<>();
		ChartDataEntry[][] entries = new ChartDataEntry[data.getSeries().length][];
		for (int i = 0; i < data.getSeries().length; i++)
			entries[i] = new ChartDataEntry[indices];
		for (int i = 0; i < sortable.size(); i++) {
			SortElement element = sortable.get(i);
			labels.add(data.getLabels().get(element.oldIndex));
			for (int j = 0; j < entries.length; j++)
				entries[j][i] = data.getSeries()[j][element.oldIndex];
		}
		ChartData sorted = new ChartData();
		sorted.setIdentifier(data.getIdentifier());
		sorted.setLegend(data.getLegend());
		for (int i = 0; i < 5; i++) {
			sorted.setLabels(labels);
			sorted.setSeries(entries);
		}
		return sorted;
	}

	private static double getSeriesValue(ChartDataEntry[][] dataEntries, int index) {
		double value = 0;
		for (ChartDataEntry[] entry : dataEntries)
			value += entry[index].getValue();
		return value;
	}

	private static class SortElement {
		private int oldIndex;
		private double value;
	}

	private static class DataComparator implements Comparator<SortElement> {
		@Override
		public int compare(SortElement o1, SortElement o2) {
			return -1 * Double.compare(Math.abs(o1.value), Math.abs(o2.value));
		}
	}

}
