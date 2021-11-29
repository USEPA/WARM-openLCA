package gov.epa.warm.backend.data.mapping;

public class ProviderMapping {

	private String processId;
	private String flowId;
	private String providerId;

	public ProviderMapping(String processId, String flowId, String providerId) {
		this.processId = processId;
		this.flowId = flowId;
		this.providerId = providerId;
	}

	public boolean matches(String processId, String flowId) {
		return this.processId.equals(processId) && this.flowId.equals(flowId);
	}

	public String getProviderId() {
		return providerId;
	}

}
