package gov.epa.warm.backend.data.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaterialFormulaParser {

	private static final Logger log = LoggerFactory.getLogger(MaterialFormulaParser.class);

	public static List<String[]> parse(InputStream stream) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			List<String[]> formulaInputs = new ArrayList<>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] formulaInput = parse(line);
				if (formulaInput != null) {
					formulaInputs.add(formulaInput);
					if (formulaInput[0].startsWith("alternative_"))
						formulaInputs.add(createPerTonFormulas(formulaInput));
				}
			}
			return formulaInputs;
		} catch (IOException e) {
			log.error("Error parsing stream of material specific formula inputs", e);
			return Collections.emptyList();
		}
	}

	private static String[] createPerTonFormulas(String[] formulaInput) {
		String[] perTonFormula = new String[formulaInput.length];
		for (int i = 0; i < formulaInput.length; i++)
			perTonFormula[i] = formulaInput[i].replace("alternative_", "per_ton_");
		return perTonFormula;
	}

	private static String[] parse(String line) {
		return line.split(",");
	}

}
