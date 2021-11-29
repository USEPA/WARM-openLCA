package gov.epa.warm;

import gov.epa.warm.backend.data.mapping.ConditionalMapping;
import gov.epa.warm.backend.data.mapping.ProviderMapping;
import gov.epa.warm.backend.data.parser.ProviderMappingParser;
import gov.epa.warm.rcp.utils.ObjectMap;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ProviderMappingTest {

	@Test
	public void testProviderMappingParser() throws IOException {
		List<ConditionalMapping<ProviderMapping[]>> mappings = ProviderMappingParser.parse(ProviderMappingTest.class
				.getResourceAsStream("conditions_test.txt"));
		ObjectMap inputCorrect = new ObjectMap();
		inputCorrect.put("location", "location_national_average");
		inputCorrect.put("landfill_type", "landfill_type_lfg_recovery_for_energy");
		inputCorrect.put("landfill_gas_recovery_collection", "landfill_gas_recovery_worst_case_collection");
		inputCorrect.put("landfill_moisture", "landfill_moisture_dry");
		ObjectMap inputIncorrect = new ObjectMap();
		inputIncorrect.put("location", "location_south_pacific");
		inputIncorrect.put("landfill_type", "landfill_type_lfg_recovery_for_flare");
		inputIncorrect.put("landfill_gas_recovery_collection", "landfill_gas_recovery_worst_case_collection");
		inputIncorrect.put("landfill_moisture", "landfill_moisture_dry");
		Assert.assertEquals(2, mappings.size());
		Assert.assertEquals("location=location_national_average", mappings.get(0).getIdentifier());
		Assert.assertEquals(true, mappings.get(0).matches(inputCorrect));
		Assert.assertEquals(false, mappings.get(0).matches(inputIncorrect));
		Assert.assertEquals(3, mappings.get(0).getMapped().length);
		Assert.assertEquals(true, mappings.get(0).getMapped()[0].matches("006a9d44-23c1-4560-8679-c59bfd0faf8e",
				"6ff43b96-4ce1-4545-b60b-e29291b0da0b"));
		Assert.assertEquals("c7ffaf84-2ce2-4d7d-a76a-0a08421e4073", mappings.get(0).getMapped()[0].getProviderId());
		Assert.assertEquals(true, mappings.get(0).getMapped()[1].matches("00823676-a53c-4739-961b-f2a7ffc36316",
				"6ff43b96-4ce1-4545-b60b-e29291b0da0b"));
		Assert.assertEquals("c7ffaf84-2ce2-4d7d-a76a-0a08421e4073", mappings.get(0).getMapped()[1].getProviderId());
		Assert.assertEquals(true, mappings.get(0).getMapped()[2].matches("fff1db80-41e1-4152-8fe7-c9cba263bc87",
				"6ff43b96-4ce1-4545-b60b-e29291b0da0b"));
		Assert.assertEquals("c7ffaf84-2ce2-4d7d-a76a-0a08421e4073", mappings.get(0).getMapped()[2].getProviderId());
		Assert.assertEquals(3, mappings.get(1).getMapped().length);
		Assert.assertEquals(
				"landfill_type=landfill_type_lfg_recovery_for_energylandfill_gas_recovery_collection=landfill_gas_recovery_worst_case_collectionlandfill_moisture=landfill_moisture_dry",
				mappings.get(1).getIdentifier());
		Assert.assertEquals(true, mappings.get(1).matches(inputCorrect));
		Assert.assertEquals(false, mappings.get(1).matches(inputIncorrect));
		Assert.assertEquals(true, mappings.get(1).getMapped()[0].matches("95934a07-65ef-43d8-aed2-6a33a2b2cf06",
				"6b5ce2ed-c5a4-4829-8b60-c3f808632b50"));
		Assert.assertEquals("a0347d87-303c-483b-806a-afe5143bcaed", mappings.get(1).getMapped()[0].getProviderId());
		Assert.assertEquals(true, mappings.get(1).getMapped()[1].matches("c82e09b8-617e-4427-a812-6bcaea5622e8",
				"a67a7c30-a7b1-4e39-9d61-680b890532f2"));
		Assert.assertEquals("605bf857-d856-4682-94b1-0089babbaccd", mappings.get(1).getMapped()[1].getProviderId());
		Assert.assertEquals(true, mappings.get(1).getMapped()[2].matches("82363f5c-5345-4f59-bc03-1beaa21644cc",
				"90ae8f6d-b16d-4a6a-822d-4d881149951c"));
		Assert.assertEquals("c880fa81-ba7d-45b6-8391-9346495f7343", mappings.get(1).getMapped()[2].getProviderId());
	}
}
