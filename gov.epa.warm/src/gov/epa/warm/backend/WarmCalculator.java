package gov.epa.warm.backend;

import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.CalculationType;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.FullResult;

import gov.epa.warm.backend.app.App;
import gov.epa.warm.backend.app.RefIds;
import gov.epa.warm.backend.data.in.InputMapper;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

public class WarmCalculator {

	private final static String DATABASE = "warm";

	public IntermediateResult calculate(ObjectMap userInputs, ObjectMap choices, ReportType reportType) {
		App.activateDatabase(DATABASE);
		ScenarioBuilder scenarioBuilder = new ScenarioBuilder(App.getMatrixCache());
		ObjectMap mappedInputs = InputMapper.mapInputValues(userInputs);
		ProductSystem baselineScenario = scenarioBuilder.buildBaselineScenario(choices, mappedInputs, reportType);
		ProductSystem alternativeScenario = scenarioBuilder.buildAlternativeScenario(choices, mappedInputs, reportType);
		ProductSystem perTonScenario = scenarioBuilder.buildPerTonScenario(choices, mappedInputs, reportType);
		FullResult baselineResult = calculate(baselineScenario, reportType);
		FullResult alternativeResult = calculate(alternativeScenario, reportType);
		FullResult perTonResult = calculate(perTonScenario, reportType);
		return new IntermediateResult(baselineResult, alternativeResult, perTonResult);
	}

	private FullResult calculate(ProductSystem scenario, ReportType reportType) {
		SystemCalculator systemCalculator = new SystemCalculator(App.getMatrixCache().getDatabase());
		CalculationSetup setup = new CalculationSetup(CalculationType.CONTRIBUTION_ANALYSIS, scenario);
		setup.withAmount(scenario.targetAmount);
		setup.withAllocation(AllocationMethod.USE_DEFAULT);
		setup.withFlowPropertyFactor(scenario.targetFlowPropertyFactor);
		setup.withUnit(scenario.targetUnit);
		setup.withParameters(scenario.parameterSets.get(0).parameters); // Only one parameterSet -> parameterSet at index 0
		setImpactMethod(setup, reportType);
		return systemCalculator.calculateFull(setup);
	}

	private void setImpactMethod(CalculationSetup setup, ReportType reportType) {
		String query = "SELECT id FROM tbl_impact_methods WHERE ref_id = '" + getMethod(reportType) + "'";
		NativeSql.on(App.getDatabase()).query(query, (resultSet) -> {
			long id = resultSet.getLong("id");
			ImpactMethod descriptor = new ImpactMethodDao(App.getDatabase()).getForId(id);
			setup.withImpactMethod(descriptor);
			return true;
		});
	}

	private String getMethod(ReportType type) {
		switch (type) {
		case MTCO2E:
			return RefIds.METHOD_MTCO2E;
		case MTCE:
			return RefIds.METHOD_MTCE;
		case ENERGY:
			return RefIds.METHOD_ENERGY;
		case JOBS:
			return RefIds.METHOD_JOBS;
		case TAXES:
			return RefIds.METHOD_TAXES;
		case WAGES:
			return RefIds.METHOD_WAGES;
		}
		return null;
	}

	public class IntermediateResult {
		private FullResult baselineResult;
		private FullResult alternativeResult;
		private FullResult perTonResult;

		private IntermediateResult(FullResult baselineResult, FullResult alternativeResult, FullResult perTonResult) {
			this.baselineResult = baselineResult;
			this.alternativeResult = alternativeResult;
			this.perTonResult = perTonResult;
		}

		public FullResult getBaselineResult() {
			return baselineResult;
		}

		public FullResult getAlternativeResult() {
			return alternativeResult;
		}

		public FullResult getPerTonResult() {
			return perTonResult;
		}
	}

}
