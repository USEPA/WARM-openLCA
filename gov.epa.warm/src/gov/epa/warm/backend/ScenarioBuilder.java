package gov.epa.warm.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;

import com.google.gson.Gson;

import gov.epa.warm.backend.app.RefIds;
import gov.epa.warm.backend.data.MapUtil;
import gov.epa.warm.backend.data.mapping.ConditionalMapping;
import gov.epa.warm.backend.data.mapping.ProviderMapping;
import gov.epa.warm.backend.data.parser.ProviderMappingParser;
import gov.epa.warm.backend.system.ProductSystemBuilder;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;
import gov.epa.warm.rcp.utils.Rcp;

public class ScenarioBuilder {

	private static List<ConditionalMapping<ProviderMapping[]>> conditionsCo2;
	private static List<ConditionalMapping<ProviderMapping[]>> conditionsEnergy;
	private static List<ConditionalMapping<ProviderMapping[]>> conditionsEconomic;
	private final ProductSystemBuilder productSystemBuilder;
	private final ProductSystemDao productSystemDao;
	private final ProcessDao processDao;

	public ScenarioBuilder(MatrixCache matrixCache) {
		this.productSystemBuilder = new ProductSystemBuilder(matrixCache);
		this.processDao = new ProcessDao(matrixCache.getDatabase());
		this.productSystemDao = new ProductSystemDao(matrixCache.getDatabase());
	}

	public ProductSystem buildBaselineScenario(ObjectMap choices, ObjectMap mappedInputs, ReportType reportType) {
		return buildScenario("baseline", RefIds.PROCESS_BASELINE, "Baseline Scenario", choices, mappedInputs,
				reportType);
	}

	public ProductSystem buildAlternativeScenario(ObjectMap choices, ObjectMap mappedInputs, ReportType reportType) {
		return buildScenario("alternative", RefIds.PROCESS_ALTERNATIVE, "Alternative Scenario", choices, mappedInputs,
				reportType);
	}

	public ProductSystem buildPerTonScenario(ObjectMap choices, ObjectMap mappedInputs, ReportType reportType) {
		return buildScenario("per_ton", RefIds.PROCESS_ALTERNATIVE, "Alternative Scenario - Per Ton", choices,
				mappedInputs, reportType);
	}

	private ProductSystem buildScenario(String type, String processRefId, String name, ObjectMap choices,
			ObjectMap mappedInputs, ReportType reportType) {
		String refId = getHashIdentifier(processRefId, choices, type, reportType);
		ProductSystem system = productSystemDao.getForRefId(refId);
		if (system == null) {
			system = new ProductSystem();
			system.refId = refId;
			system.name = name + " - " + refId;
			system.description = new Gson().toJson(choices);
			Process process = processDao.getForRefId(processRefId);
			Exchange exchange = process.quantitativeReference;
			system.referenceProcess = process;
			system.referenceExchange = exchange;
			system.targetFlowPropertyFactor = exchange.flowPropertyFactor;
			system.targetUnit = exchange.unit;
			double total = addParameters(type, system, mappedInputs);
			system.targetAmount = total;
			system = productSystemDao.insert(system);
		} else {
			double total = addParameters(type, system, mappedInputs);
			system.targetAmount = total;
			system = productSystemDao.update(system);
		}
		ProviderMapping[] mappings = getProviderMappings(choices, reportType);
		return productSystemBuilder.autoComplete(system, mappings);
	}

	private double addParameters(String type, ProductSystem system, ObjectMap mappedInputs) {
		ParameterContextBuilder parameterContextBuilder = new ParameterContextBuilder();
		List<ParameterRedefSet> parameters = parameterContextBuilder.build(type, mappedInputs);
		system.parameterSets.clear();
		system.parameterSets.addAll(parameters);

		double total = 0;
		for (ParameterRedefSet redefSet : parameters)
			for (ParameterRedef redef : redefSet.parameters) {
				if (MapUtil.isMaterialInput(redef.name))
					total += redef.value;
			}
		return total;
	}

	private ProviderMapping[] getProviderMappings(ObjectMap userInput, ReportType reportType) {
		List<ProviderMapping> mappings = new ArrayList<>();
		List<ConditionalMapping<ProviderMapping[]>> conditions = getConditions(reportType);
		for (ConditionalMapping<ProviderMapping[]> condition : conditions)
			if (condition.matches(userInput))
				for (ProviderMapping mapping : condition.getMapped())
					mappings.add(mapping);
		return mappings.toArray(new ProviderMapping[mappings.size()]);
	}

	private String getHashIdentifier(String processRefId, ObjectMap userInput, String type, ReportType reportType) {
		StringBuilder identifier = new StringBuilder(processRefId);
		List<ConditionalMapping<ProviderMapping[]>> conditions = getConditions(reportType);
		for (ConditionalMapping<ProviderMapping[]> condition : conditions)
			if (condition.matches(userInput)) {
				identifier.append("@");
				identifier.append(condition.getIdentifier());
			}
		identifier.append("@");
		identifier.append(type);
		identifier.append("@");
		identifier.append(reportType.name());
		return UUID.nameUUIDFromBytes(identifier.toString().getBytes(Charset.forName("utf-8"))).toString();
	}

	private List<ConditionalMapping<ProviderMapping[]>> getConditions(ReportType reportType) {
		try {
			if (reportType == ReportType.ENERGY) {
				File file = new File(Rcp.getWorkspace(), "mappings/conditions_energy.txt");
				if (conditionsEnergy == null)
					conditionsEnergy = ProviderMappingParser.parse(new FileInputStream(file));
				return conditionsEnergy;
			}
			else if (reportType == ReportType.JOBS || reportType == ReportType.TAXES || reportType == ReportType.WAGES) {
				File file = new File(Rcp.getWorkspace(), "mappings/conditions_economic.txt");
				if (conditionsEconomic == null)
					conditionsEconomic = ProviderMappingParser.parse(new FileInputStream(file));
				return conditionsEconomic;
			}else {
				File file = new File(Rcp.getWorkspace(), "mappings/conditions_co2.txt");
				if (conditionsCo2 == null)
					conditionsCo2 = ProviderMappingParser.parse(new FileInputStream(file));
				return conditionsCo2;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
