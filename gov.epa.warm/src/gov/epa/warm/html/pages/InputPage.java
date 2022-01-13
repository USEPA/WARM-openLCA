package gov.epa.warm.html.pages;

import gov.epa.warm.backend.WarmCalculator;
import gov.epa.warm.backend.WarmCalculator.IntermediateResult;
import gov.epa.warm.backend.data.in.ChoiceMapper;
import gov.epa.warm.html.HtmlEditor;
import gov.epa.warm.html.HtmlEditorInput;
import gov.epa.warm.html.PageDescriptor;
import gov.epa.warm.html.pages.ReportPage.ReportType;
import gov.epa.warm.html.pages.data.InputData;
import gov.epa.warm.rcp.utils.Editors;
import gov.epa.warm.rcp.utils.FileChooser;
import gov.epa.warm.rcp.utils.ObjectMap;
import gov.epa.warm.rcp.utils.UI;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputPage extends HtmlEditor {

	public static final String ID = "gov.epa.warm.InputPage";
	private static final Logger log = LoggerFactory.getLogger(InputPage.class);
	private static final String FILE_EXTENSION = "*.warm";
	private boolean initialized = false;

	public static void open() {
		HtmlEditorInput input = new HtmlEditorInput(ID + randomId());
		InputData.putDefaultInputs(input.getData());
		Editors.open(input, ID);
	}

	public static void openWithFile() {
		File file = FileChooser.forOpening(FILE_EXTENSION);
		if (file == null || !file.exists())
			return;
		try {
			Editors.open(new HtmlEditorInput(ID + randomId(), file), ID);
		} catch (IOException e) {
			log.error("Couldn't open editor with warm data", e);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		updatePartName();
	}

	@Override
	protected PageDescriptor[] getPageDescriptors() {
		return new PageDescriptor[] { new PageDescriptor("Step1", "step1.html") };
	}

	@Override
	protected void onLoaded(PageDescriptor page) {
		registerFunction("saveInputs", InputPage.this::saveInputs);
		registerFunction("calculate", InputPage.this::calculate);
		if (!initialized) {
			onReloaded(page);
			initialized = true;
		}
	}

	@Override
	protected void onReloaded(PageDescriptor page) {
		call("initialize", getData().get("userInputs"));
		log.debug(getData().get("userInputs"));
	}

	private void saveInputs(String inputs) {
		String userInputs = getData().get("userInputs");
		if (userInputs != null && userInputs.equals(inputs))
			return;
		getData().put("userInputs", inputs);
		setDirty();
		editorDirtyStateChanged();
	}

	@Override
	protected void updatePartName() {
		String filename = getData().get("filename");
		if (filename == null) {
			setPartName("Data Entry - Unsaved");
			return;
		}
		filename = new File(filename).getName();
		setPartName("Data Entry - (" + filename + ")");
	}

	private void calculate(String type) {
		new CalculationDialog(type, getData().get("userInputs")).open();
	}

	private static String randomId() {
		StringBuilder id = new StringBuilder();
		for (int i = 0; i < 10; i++)
			id.append(Math.random() * 26 + 65);
		return id.toString();
	}

	@Override
	protected String getFileExtension() {
		return FILE_EXTENSION;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return getData().get("userInputs") != null;
	}

	private class CalculationDialog {

		private String type;
		private String jsonData;

		public CalculationDialog(String type, String jsonData) {
			this.type = type;
			this.jsonData = jsonData;
		}

		public void open() {
			try {
				new ProgressMonitorDialog(UI.shell()).run(true, false, this::doCalculate);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		private void doCalculate(IProgressMonitor monitor) {
			monitor.beginTask("Calculating...", IProgressMonitor.UNKNOWN);
			monitor.subTask("Note: Initial calculation might take longer");
			WarmCalculator calculator = new WarmCalculator();
			ReportType reportType = ReportType.valueOf(type);
			ObjectMap userInputs = ObjectMap.fromJson(jsonData);
			ObjectMap choices = ChoiceMapper.mapChoices(userInputs);
			IntermediateResult result = calculator.calculate(userInputs, choices, reportType);
			monitor.done();
			ReportPage.open(reportType, result, userInputs, choices);
		}

	}

}
