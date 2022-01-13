package gov.epa.warm.html;

import gov.epa.warm.rcp.utils.Desktop;
import gov.epa.warm.rcp.utils.UI;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

class HtmlEditorPage extends FormPage implements IHtmlPage {

	private Browser browser;
	private final PageDescriptor page;

	HtmlEditorPage(HtmlEditor editor, PageDescriptor pageDescriptor) {
		super(editor, id(editor, pageDescriptor.getFile()), pageDescriptor.getTitle());
		this.page = pageDescriptor;
	}

	private static String id(HtmlEditor editor, String file) {
		String editorName = editor.getClass().getSimpleName();
		String fileName = file.substring(0, file.lastIndexOf('.'));
		return "gov.epa.warm.rcp.pages." + editorName + "." + fileName;
	}

	@Override
	public String getUrl() {
		return HtmlEditor.getUrl(page.getFile());
	}

	Browser getBrowser() {
		return browser;
	}

	@Override
	public HtmlEditor getEditor() {
		return (HtmlEditor) super.getEditor();
	}

	@Override
	public void onLoaded() {
		getEditor().registerFunction("navigate", this::navigate);
		getEditor().registerFunction("openInExternalBrowser", Desktop::browse);
		String lastRoute = getEditor().getData().get("lastRoute");
		if (lastRoute != null && !lastRoute.isEmpty())
			navigate(lastRoute);
		getEditor().onLoaded(page);
	}

	@Override
	public void onReloaded() {
		getEditor().onReloaded(page);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		Composite composite = form.getBody();
		composite.setLayout(new FillLayout());
		browser = UI.createBrowser(composite, this);
	}

	private void navigate(String route) {
		if (!getFirstPage().equals(route))
			getEditor().getData().put("lastRoute", route);
		else
			getEditor().getData().remove("lastRoute");
		getBrowser().setUrl(HtmlEditor.getUrl(route));
	}

	private String getFirstPage() {
		return getEditor().getPageDescriptors()[0].getFile();
	}

}
