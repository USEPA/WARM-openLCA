package gov.epa.warm.html.pages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.LoggerFactory;

import gov.epa.warm.html.HtmlEditor;
import gov.epa.warm.html.HtmlEditorInput;
import gov.epa.warm.html.PageDescriptor;
import gov.epa.warm.rcp.utils.Editors;
import gov.epa.warm.rcp.utils.FileChooser;

public class HomePage extends HtmlEditor {

	public static final String ID = "gov.epa.warm.HomePage";

	public static void open() {
		Editors.open(new HtmlEditorInput(ID), ID);
	}

	@Override
	protected PageDescriptor[] getPageDescriptors() {
		return new PageDescriptor[] { new PageDescriptor("Home", "home.html") };
	}

	@Override
	protected void onLoaded(PageDescriptor page) {
		registerFunction("downloadUserGuide", HomePage.this::downloadUserGuide);
		registerFunction("start", InputPage::open);
	}

	private void downloadUserGuide() {
		File file = FileChooser.forSaving("*.pdf", "warm_user_guide.pdf");
		if (file == null)
			return;
		try (InputStream input = getClass().getResourceAsStream("/resources/warm_user_guide.pdf")) {
			Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Error downloading user guide", e);
		}
	}

}
