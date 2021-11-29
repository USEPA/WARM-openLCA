package gov.epa.warm.backend;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.ParameterRedefSet;

import gov.epa.warm.backend.data.MapUtil;
import gov.epa.warm.rcp.utils.ObjectMap;

public class ParameterContextBuilder {

	public List<ParameterRedefSet> build(String type, ObjectMap inputs) {
		List<ParameterRedefSet> parametersSet = new ArrayList<>();
		ParameterRedefSet parameterSet = new ParameterRedefSet();
		String actualType = type;
		if (type.equals("per_ton"))
			actualType = "alternative";
		for (String param : inputs.keySet()) {
			if (MapUtil.isMaterialInput(param) && !param.startsWith(actualType))
				continue;
			ParameterRedef parameter = new ParameterRedef();
			parameter.name = param;
			double value = Double.parseDouble((String) inputs.get(param));
			if (type.equals("per_ton") && MapUtil.isMaterialInput(param))
				value = 1;
			parameter.value = value;
			parameterSet.parameters.add(parameter);
		}
		parametersSet.add(parameterSet);
		return parametersSet;
	}

}
