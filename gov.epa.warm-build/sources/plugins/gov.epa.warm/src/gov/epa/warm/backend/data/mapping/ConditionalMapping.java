package gov.epa.warm.backend.data.mapping;

import gov.epa.warm.rcp.utils.KeyValue;
import gov.epa.warm.rcp.utils.ObjectMap;

public class ConditionalMapping<T> {

	private KeyValue[] conditions;
	private T mapped;

	public ConditionalMapping(KeyValue[] conditions, T mapping) {
		this.conditions = conditions;
		this.mapped = mapping;
	}

	public boolean matches(ObjectMap map) {
		for (KeyValue condition : conditions)
			if (map.getString(condition.getKey()) == null)
				return false;
			else if (!map.getString(condition.getKey()).equals(condition.getValue()))
				return false;
		return true;
	}

	public T getMapped() {
		return mapped;
	}

	public String getIdentifier() {
		StringBuilder id = new StringBuilder();
		for (KeyValue condition : conditions) {
			id.append(condition.getKey());
			id.append("=");
			id.append(condition.getValue());
		}
		return id.toString();
	}
}
