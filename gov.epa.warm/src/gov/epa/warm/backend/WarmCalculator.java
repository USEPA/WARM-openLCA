package gov.epa.warm.backend;

import gov.epa.warm.backend.app.App;
import gov.epa.warm.backend.app.RefIds;
import gov.epa.warm.backend.data.in.InputMapper;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.sql.SQLException;

import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NativeSql;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.results.FullResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarmCalculator {

	private final static Logger log = LoggerFactory.getLogger(WarmCalculator.class);
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
		SystemCalculator systemCalculator = new SystemCalculator(App.getMatrixCache(), App.getSolver());
		CalculationSetup setup = new CalculationSetup(scenario);
		setup.setAmount(scenario.getTargetAmount());
		setup.setAllocationMethod(AllocationMethod.USE_DEFAULT);
		setup.setFlowPropertyFactor(scenario.getTargetFlowPropertyFactor());
		setup.setUnit(scenario.getTargetUnit());
		setup.getParameterRedefs().addAll(scenario.getParameterRedefs());
		setImpactMethod(setup, reportType);
		return systemCalculator.calculateFull(setup);
	}

	private void setImpactMethod(CalculationSetup setup, ReportType reportType) {
		try {
			String query = "SELECT id FROM tbl_impact_methods WHERE ref_id = '" + getMethod(reportType) + "'";
			NativeSql.on(App.getDatabase()).query(query, (resultSet) -> {
				long id = resultSet.getLong("id");
				ImpactMethodDescriptor descriptor = new ImpactMethodDao(App.getDatabase()).getDescriptor(id);
				setup.setImpactMethod(descriptor);
				return true;
			});
		} catch (SQLException e) {
			log.error("Could not load impact method", e);
		}
	}

	private String getMethod(ReportType type) {
		switch (type) {
		case MTCO2E:
			return RefIds.METHOD_MTCO2E;
		case MTCE:
			return RefIds.METHOD_MTCE;
		case ENERGY:
			return RefIds.METHOD_ENERGY;
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
