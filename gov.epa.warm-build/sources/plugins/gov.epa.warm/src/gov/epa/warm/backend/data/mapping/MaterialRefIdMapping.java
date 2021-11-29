package gov.epa.warm.backend.data.mapping;

public class MaterialRefIdMapping {

	private String material;
	private String baselineRefId;
	private String alternativeRefId;

	public MaterialRefIdMapping(String material, String baselineRefId, String alternativeRefId) {
		this.material = material;
		this.baselineRefId = baselineRefId;
		this.alternativeRefId = alternativeRefId;
	}

	public String getMaterial() {
		return material;
	}

	public String getBaselineRefId() {
		return baselineRefId;
	}

	public String getAlternativeRefId() {
		return alternativeRefId;
	}

}
