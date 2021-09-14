package gov.epa.warm.backend;

import gov.epa.warm.backend.data.MapUtil;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.ParameterRedef;

public class ParameterContextBuilder {

	public List<ParameterRedef> build(String type, ObjectMap inputs) {
		List<ParameterRedef> parameters = new ArrayList<>();
		String actualType = type;
		if (type.equals("per_ton"))
			actualType = "alternative";
		for (String param : inputs.keySet()) {
			if (MapUtil.isMaterialInput(param) && !param.startsWith(actualType))
				continue;
			ParameterRedef parameter = new ParameterRedef();
			parameter.setName(param);
			double value = Double.parseDouble((String) inputs.get(param));
			if (type.equals("per_ton") && MapUtil.isMaterialInput(param))
				value = 1;
			parameter.setValue(value);
			parameters.add(parameter);
		}
		return parameters;
	}

}
