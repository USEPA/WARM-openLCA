package gov.epa.warm.html.pages.data.charts;

import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProductionEOLProvider implements IChartDataProvider {

	private final static Logger log = LoggerFactory.getLogger(ProductionEOLProvider.class);
	
	@Override
	public ChartData getChartData(IntermediateResult results,
			List<ObjectMap> materials, ReportType type) {
		ChartData data = new ChartData();
		data.setIdentifier("productionEOLContributions");
		data.setLabels(getLabels(results, materials));
		data.setLegend(getLegend(results, materials));
		data.setSeries(getSeries(results, materials));
		return data;
//		return ChartUtil.sort(data);
	}

	private List<String> getLabels(IntermediateResult results,
			List<ObjectMap> materials) {
		List<String> labels = new ArrayList<>();
		for (ObjectMap materialResult : materials) {
			String name = materialResult.getString("name");
			labels.add(name + " Baseline");
			labels.add(name + " Alternative");
		}
		return labels;
	}

	private List<String> getLegend(IntermediateResult results,
			List<ObjectMap> materials) {
		List<String> legend = new ArrayList<>();
		legend.add("Production");
		legend.add("Recycling");
		legend.add("Landfilling");
		legend.add("Combustion");
		legend.add("Composting");
		legend.add("Anaerobic Digestion");
		return legend;
	}

	private ChartDataEntry[][] getSeries(IntermediateResult results,
			List<ObjectMap> materials) {
		/*
		 * values array:
		 * 	[sub_type][type+material['name']] = material[type+sub_type+"_result"]
		 *
		 */
		String[] types = {"Baseline", "Alternative"};
		String[] sub_types = {"Production", "Recycling", "Landfilling", "Combustion", "Composting", "Anaerobic Digestion"};
		ChartDataEntry[][] values = new ChartDataEntry[sub_types.length][];
		for (int j = 0; j < sub_types.length; j++) {
			values[j] = new ChartDataEntry[types.length * materials.size()];
			int k = 0;
			for (ObjectMap material : materials) {
				log.debug(material.getString("name"));
				for (int i = 0; i < types.length; i++) {
					ChartDataEntry entry = new ChartDataEntry();
					if (j == 0) {
						double production = 0.0;
						if (material.containsKey("per_ton_source_reduction_result")) {
							if (i == 0) {
								production = ChartUtil.round(-1.0 * material.getDouble("baseline") * material.getDouble("per_ton_source_reduction_result"));
							}
							else {
								production = ChartUtil.round(-1.0 * (material.getDouble("baseline") - material.getDouble("source_reduction_input_change")) * material.getDouble("per_ton_source_reduction_result"));
							}
						}
						entry.setValue(production);
						entry.setMeta(getProductionMetaLabel(types[i], material.getString("name")));
					}
					else {
						log.debug(types[i].toLowerCase()+"_"+sub_types[j].toLowerCase().replace(' ', '_')+"_result="+ChartUtil.round(material.getDouble(types[i].toLowerCase()+"_"+sub_types[j].toLowerCase().replace(' ', '_')+"_result")));
						entry.setValue(ChartUtil.round(material.getDouble(types[i].toLowerCase()+"_"+sub_types[j].toLowerCase().replace(' ', '_')+"_result")));
						entry.setMeta(getMetaLabel(types[i], sub_types[j], material.getString("name")));
					}
					entry.setIdentifier(types[i]);
					values[j][k++] = entry;
				}
			}
		}
		return values;
	}

	private String getMetaLabel(String scenario, String subtype, String material) {
		return "<strong>" + scenario + "</strong><br>Waste treatment: "
				+ subtype + "<br>Material: " + material;
	}

	private String getProductionMetaLabel(String scenario, String material) {
		return "<strong>" + scenario + "</strong><br>Production<br>Material: " + material;
	}

//	private String getKeyForSubType(String type, String subType) {
//		type = type.substring(0, type.indexOf(' ')).toLowerCase();
//		StringBuilder key = new StringBuilder(type);
//		if (!subType.equals("All")) {
//			key.append("_");
//			key.append(subType.toLowerCase().replace(" ", "_"));
//		}
//		key.append("_result");
//		return key.toString();
//	}

}
