package gov.epa.warm.backend.data;

import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.rcp.utils.Rcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class FlowRefIdMappings {

	private static Properties mappings = new Properties();

	static {
		try {
			mappings.load(new FileInputStream(new File(Rcp.getWorkspace(), "mappings/flow_mappings.txt")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String> getFlowContributionRefIds(ReportType type) {
		String value = mappings.getProperty(type.name());
		return Arrays.asList(value.split(","));
	}

}
