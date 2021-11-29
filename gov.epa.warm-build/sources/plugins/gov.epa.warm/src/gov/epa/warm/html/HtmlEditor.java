package gov.epa.warm.html;

import gov.epa.warm.rcp.Activator;
import gov.epa.warm.rcp.utils.FileChooser;
import gov.epa.warm.rcp.utils.HtmlFolder;
import gov.epa.warm.rcp.utils.Rcp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public abstract class HtmlEditor extends FormEditor {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private List<HtmlEditorPage> pages = new ArrayList<>();
	private boolean dirty = false;

	@Override
	protected final void addPages() {
		try {
			for (PageDescriptor pageDescriptor : getPageDescriptors()) {
				HtmlEditorPage page = new HtmlEditorPage(this, pageDescriptor);
				pages.add(page);
				addPage(page);
			}
		} catch (Exception e) {
			log.error("failed to add pages", e);
		}
	}

	protected abstract PageDescriptor[] getPageDescriptors();

	protected void onLoaded(PageDescriptor page) {
		// subclasses may override
	}

	protected void onReloaded(PageDescriptor page) {
		// subclasses may override
	}

	protected static String getUrl(String route) {
		return HtmlFolder.getUrl(Activator.getDefault().getBundle(), Rcp.getWorkspace(), route);
	}

	protected final Map<String, String> getData() {
		return getEditorInput().getData();
	}

	protected void call(String method, Object... args) {
		// WORKAROUND FIX: swt has some issues when using xulrunner 24
		// need to use setTimeout to be in JS UI thread when executing code
		// otherwise: SWTException: Permission denied to access ...
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=429739
		StringBuilder command = new StringBuilder("setTimeout(function() {");
		command.append(method);
		command.append("(");
		if (args != null) {
			Gson gson = new Gson();
			for (int i = 0; i < args.length; i++) {
				if (i != 0)
					command.append(",");
				command.append(gson.toJson(args[i]));
			}
		}
		command.append(");}, 10)");
		try {
			getBrowser().evaluate(command.toString());
		} catch (SWTException e) {
			log.error("Failed to call JavaScript function " + method, e);
		}
	}

	protected final void registerFunction(String name, Runnable function) {
		new BrowserFunction(getBrowser(), name) {
			@Override
			public Object function(Object[] arguments) {
				function.run();
				return null;
			}
		};
	}

	protected final void registerFunction(String name, Consumer<String> function) {
		new BrowserFunction(getBrowser(), name) {
			@Override
			public Object function(Object[] arguments) {
				String argument = null;
				if (arguments != null && arguments.length > 0)
					argument = arguments[0].toString();
				function.accept(argument);
				return null;
			}
		};
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		String filename = getData().get("filename");
		if (filename == null) {
			doSaveAs();
			return;
		}
		File file = new File(filename);
		String userInputs = getData().get("userInputs");
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(userInputs);
			dirty = false;
			editorDirtyStateChanged();
			updatePartName();
		} catch (IOException e) {
			log.error("Error while saving", e);
		}
	}

	protected void updatePartName() {
		// subclasses may override
	}

	protected String getFileExtension() {
		return null;
	}

	@Override
	public void doSaveAs() {
		String filename = getData().get("filename");
		File file = FileChooser.forSaving(getFileExtension(), filename);
		if (file == null)
			return;
		getData().put("filename", file.getAbsolutePath());
		doSave(null);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	protected final Browser getBrowser() {
		return pages.get(getActivePage()).getBrowser();
	}

	@Override
	public HtmlEditorInput getEditorInput() {
		return (HtmlEditorInput) super.getEditorInput();
	}

	@Override
	protected void createPages() {
		super.createPages();
		if (getPageCount() == 1 && getContainer() instanceof CTabFolder)
			((CTabFolder) getContainer()).setTabHeight(0);
	}

	@Override
	public boolean isDirty() {
		return this.dirty;
	}

	public void setDirty() {
		this.dirty = true;
	}

}
