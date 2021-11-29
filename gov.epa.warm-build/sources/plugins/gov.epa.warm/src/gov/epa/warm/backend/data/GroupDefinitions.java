package gov.epa.warm.backend.data;

import gov.epa.warm.rcp.utils.Rcp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroupDefinitions {

	private static List<Group> groups = new ArrayList<>();

	static {
		File file = new File(Rcp.getWorkspace(), "mappings/groups.txt");
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] cells = line.split(",");
				if (cells.length < 2)
					continue;
				Group group = new Group();
				group.name = cells[0];
				for (int i = 1; i < cells.length; i++)
					group.processRefIds.add(cells[i]);
				groups.add(group);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<Group> getGroups() {
		return groups;
	}

	public static class Group {
		private String name;
		private List<String> processRefIds = new ArrayList<>();

		public String getName() {
			return name;
		}

		public List<String> getProcessRefIds() {
			return processRefIds;
		}
	}

}
