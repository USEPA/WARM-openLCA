package gov.epa.warm.backend.data.parser;

import gov.epa.warm.backend.data.mapping.ConditionalMapping;
import gov.epa.warm.backend.data.mapping.ProviderMapping;
import gov.epa.warm.rcp.utils.KeyValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderMappingParser {

	private final static String CELL_SPLITTER = ",";
	private final static String KEY_VALUE_SPLITTER = "=";
	private final static String ID_SPLITTER = ";";
	private final static Logger log = LoggerFactory.getLogger(ProviderMappingParser.class);

	public static List<ConditionalMapping<ProviderMapping[]>> parse(InputStream stream) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			List<ConditionalMapping<ProviderMapping[]>> mappings = new ArrayList<>();
			String line = null;
			while ((line = reader.readLine()) != null)
				mappings.add(parse(line));
			return mappings;
		} catch (IOException e) {
			log.error("Error parsing stream of provider mappings", e);
			return Collections.emptyList();
		}
	}

	// parser expects conditions (key=value) first. The first cell that
	// isn't a condition (and isn't empty) is the provider mapping cell
	// (id1,id2,id3)
	private static ConditionalMapping<ProviderMapping[]> parse(String line) {
		String[] cells = line.split(CELL_SPLITTER);
		KeyValue[] conditions = parseConditions(cells);
		ProviderMapping[] mappings = parseMappings(cells);
		return new ConditionalMapping<>(conditions, mappings);
	}

	private static KeyValue[] parseConditions(String[] cells) {
		List<KeyValue> conditions = new ArrayList<>();
		for (String cell : cells) {
			if (!cell.contains(KEY_VALUE_SPLITTER))
				break;
			String[] keyValue = cell.trim().split(KEY_VALUE_SPLITTER);
			String key = keyValue[0].trim();
			String value = keyValue[1].trim();
			conditions.add(new KeyValue(key, value));
		}
		return conditions.toArray(new KeyValue[conditions.size()]);
	}

	private static ProviderMapping[] parseMappings(String[] cells) {
		List<ProviderMapping> mappings = new ArrayList<>();
		for (String cell : cells) {
			cell = cell.trim();
			if (cell.contains(KEY_VALUE_SPLITTER))
				continue;
			if (cell.trim().isEmpty())
				continue;
			if (!cell.contains(ID_SPLITTER))
				break;
			String[] ids = cell.split(ID_SPLITTER);
			String processId = ids[0].trim();
			String flowId = ids[1].trim();
			String providerId = ids[2].trim();
			mappings.add(new ProviderMapping(processId, flowId, providerId));
		}
		return mappings.toArray(new ProviderMapping[mappings.size()]);
	}

}
