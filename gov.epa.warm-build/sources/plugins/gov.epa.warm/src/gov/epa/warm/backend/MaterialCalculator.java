package gov.epa.warm.backend;

import gov.epa.warm.backend.app.App;
import gov.epa.warm.backend.data.MapUtil;
import gov.epa.warm.backend.data.parser.MaterialFormulaParser;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;
import gov.epa.warm.rcp.utils.Rcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaterialCalculator {

	private final static Logger log = LoggerFactory.getLogger(MaterialCalculator.class);
	private static List<String[]> formulaInputsCo2;
	private static List<String[]> formulaInputsEnergy;
	private static List<String[]> formulaInputsJobs;
	private static List<String[]> formulaInputsWages;
	private static List<String[]> formulaInputsTaxes;
	private final ObjectMap materialInputs;
	private final List<ObjectMap> materialResults;
	private final List<Parameter> globalParameters;

	public MaterialCalculator(ObjectMap materialInputs, List<ObjectMap> materialResults) {
		this.materialInputs = materialInputs;
		this.materialResults = materialResults;
		this.globalParameters = new ParameterDao(App.getDatabase()).getGlobalParameters();
	}

	public void applyFormulas(ReportType type) {
		for (String[] formulaInput : getFormulaInputs(type)) {
			String key = getRealKeyFromResultKey(formulaInput[0]);
			String materialName = getMaterialNameFromResultKey(formulaInput[0]);
			double initialResult = getResultingAmount(formulaInput[0]);
			if (initialResult == 0d)
				continue;
			double correctedResult = applyFormula(formulaInput);
			if (Double.isNaN(correctedResult)) {
				log.debug("Could not apply formulas, corrected result is NaN for " + formulaInput[0]);
				continue;
			}
			ObjectMap materialResult = findMaterialResult(materialName);
			if (materialResult == null) {
				log.debug("Could not find material result for " + formulaInput[0]);
				continue;
			}
			materialResult.put(key, correctedResult);
		}
	}

	private double applyFormula(String[] variables) {
		if (variables == null)
			return 0d;
		switch (variables.length) {
		case 4:
			return applyFormula1(variables);
		case 6:
			return applyFormula2(variables);
		case 7:
			return applyFormula3(variables);
		case 10:
			return applyFormula4(variables);
		default:
			return 0;
		}
	}

	// a*(b/(b+c*d))
	// a = the resulting amount of the specific material
	// b = the input amount of the specific material
	// c = the share of the specific material in the mixed material
	// d = the input amount of the mixed material
	private double applyFormula1(String[] variables) {
		double a = getResultingAmount(variables[0]);
		double b = getInputAmount(variables[1]);
		if (b == 0d)
			return 0d;
		double c = getShareInMix(variables[2]);
		double d = getInputAmount(variables[3]);
		return a * (b / (b + c * d));
	}

	// a*(b/(b+c*d+e*f))
	// a = the resulting amount of the specific material
	// b = the input amount of the specific material
	// c = the share of the specific material in the mixed material 1
	// d = the input amount of the mixed material 1
	// e = the share of the specific material in the mixed material 2
	// f = the input amount of the mixed material 2
	private double applyFormula2(String[] variables) {
		double a = getResultingAmount(variables[0]);
		double b = getInputAmount(variables[1]);
		if (b == 0d)
			return 0d;
		double c = getShareInMix(variables[2]);
		double d = getInputAmount(variables[3]);
		double e = getShareInMix(variables[4]);
		double f = getInputAmount(variables[5]);
		return a * (b / (b + c * d + e * f));
	}

	// a*(b/(b+c*d+e*f*g))
	// a = the resulting amount of the specific material
	// b = the input amount of the specific material
	// c = the share of the specific material in the mixed material 1
	// d = the input amount of the mixed material 1
	// e = the share of the specific material in the mixed material 2
	// f = the share of the specific material in the mixed material 3
	// g = the input amount of the mixed material 2
	private double applyFormula3(String[] variables) {
		double a = getResultingAmount(variables[0]);
		double b = getInputAmount(variables[1]);
		if (b == 0d)
			return 0d;
		double c = getShareInMix(variables[2]);
		double d = getInputAmount(variables[3]);
		double e = getShareInMix(variables[4]);
		double f = getShareInMix(variables[5]);
		double g = getInputAmount(variables[6]);
		return a * (b / (b + c * d + e * f * g));
	}

	// a*(b/(b+c*d+e*f+g*h+i*j)
	// a = the resulting amount of the specific material
	// b = the input amount of the specific material
	// c = the share of the specific material in the mixed material 1
	// d = the input amount of the mixed material 1
	// e = the share of the specific material in the mixed material 2
	// f = the input amount of the mixed material 2
	// g = the share of the specific material in the mixed material 3
	// h = the input amount of the mixed material 3
	// i = the share of the specific material in the mixed material 4
	// j = the input amount of the mixed material 4
	private double applyFormula4(String[] variables) {
		double a = getResultingAmount(variables[0]);
		double b = getInputAmount(variables[1]);
		if (b == 0d)
			return 0d;
		double c = getShareInMix(variables[2]);
		double d = getInputAmount(variables[3]);
		double e = getShareInMix(variables[4]);
		double f = getInputAmount(variables[5]);
		double g = getShareInMix(variables[6]);
		double h = getInputAmount(variables[7]);
		double i = getShareInMix(variables[8]);
		double j = getInputAmount(variables[9]);
		return a * (b / (b + c * d + e * f + g * h + i * j));
	}

	// The input amount is part of the user input map
	private double getInputAmount(String key) {
		if (key.startsWith("per_ton"))
			return 1d;
		String materialName = getMaterialNameFromInputKey(key);
		String realKey = getRealKeyFromInputKey(key);
		for (String index : materialInputs.keySet()) {
			String nameFromResult = MapUtil.convertMaterial(materialInputs.getString(index + ".name"));
			if (materialName.equals(nameFromResult))
				return materialInputs.getDouble(index + "." + realKey);
		}
		log.debug("Could not find input amount for " + key);
		return 0d;
	}

	// The resulting amount is part of the result map
	private double getResultingAmount(String key) {
		// key is like 'baseline_{materialName}_{type}_result
		String materialName = getMaterialNameFromResultKey(key);
		String realKey = getRealKeyFromResultKey(key);
		ObjectMap materialResult = findMaterialResult(materialName);
		if (materialResult == null) {
			log.debug("Could not find resulting amount for " + key);
			return 0;
		}
		return materialResult.getDouble(realKey);
	}

	private ObjectMap findMaterialResult(String materialName) {
		for (ObjectMap materialResult : materialResults) {
			String nameFromResult = MapUtil.convertMaterial(materialResult.getString("name"));
			if (materialName.equals(nameFromResult))
				return materialResult;
		}
		return null;
	}

	private String getRealKeyFromInputKey(String key) {
		String[] splittedKey = key.split("_");
		StringBuilder realKey = new StringBuilder();
		realKey.append(splittedKey[0]);
		realKey.append("_");
		if (key.startsWith("per_ton")) {
			realKey.append(splittedKey[1]);
			realKey.append("_");
		}
		realKey.append(splittedKey[splittedKey.length - 1]);
		return realKey.toString();
	}

	private String getMaterialNameFromInputKey(String key) {
		String[] splittedKey = key.split("_");
		StringBuilder materialName = new StringBuilder();
		int start = 1;
		if (key.startsWith("per_ton"))
			start = 2;
		for (int i = start; i < splittedKey.length - 1; i++) {
			if (i != start)
				materialName.append("_");
			materialName.append(splittedKey[i]);
		}
		return materialName.toString();
	}

	private String getRealKeyFromResultKey(String key) {
		String[] splittedKey = key.split("_");
		StringBuilder realKey = new StringBuilder();
		realKey.append(splittedKey[0]);
		realKey.append("_");
		if (key.startsWith("per_ton")) {
			realKey.append(splittedKey[1]);
			realKey.append("_");
		}
		realKey.append(splittedKey[splittedKey.length - 2]);
		realKey.append("_");
		realKey.append(splittedKey[splittedKey.length - 1]);
		return realKey.toString();
	}

	private String getMaterialNameFromResultKey(String key) {
		String[] splittedKey = key.split("_");
		StringBuilder materialName = new StringBuilder();
		int start = 1;
		if (key.startsWith("per_ton"))
			start = 2;
		for (int i = start; i < splittedKey.length - 2; i++) {
			if (i != start)
				materialName.append("_");
			materialName.append(splittedKey[i]);
		}
		return materialName.toString();
	}

	// The share is saved as a global parameter in the database
	private double getShareInMix(String parameterName) {
		for (Parameter parameter : globalParameters)
			if (parameter.name.equals(parameterName))
				return parameter.value;
		log.debug("Could not find mix parameter " + parameterName);
		return 0;
	}

	private List<String[]> getFormulaInputs(ReportType reportType) {
		try {
			if (reportType == ReportType.ENERGY) {
				File file = new File(Rcp.getWorkspace(), "mappings/material_specific_formulas_energy.txt");
				if (formulaInputsEnergy == null)
					formulaInputsEnergy = MaterialFormulaParser.parse(new FileInputStream(file));
			}
			if (reportType == ReportType.JOBS || reportType == ReportType.TAXES || reportType == ReportType.WAGES) {
				File file = new File(Rcp.getWorkspace(), "mappings/material_specific_formulas_economic.txt");
				if (formulaInputsJobs == null) {
					if (!file.exists()) {
						log.error("File not found: "+file.getAbsolutePath());
					}
					else {
						formulaInputsJobs = MaterialFormulaParser.parse(new FileInputStream(file));
					}
				}
				return formulaInputsJobs;
			}
			File file = new File(Rcp.getWorkspace(), "mappings/material_specific_formulas_co2.txt");
			if (formulaInputsCo2 == null)
				formulaInputsCo2 = MaterialFormulaParser.parse(new FileInputStream(file));
			return formulaInputsCo2;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
