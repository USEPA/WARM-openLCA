package gov.epa.warm.rcp;

import gov.epa.warm.html.pages.HomePage;
import gov.epa.warm.rcp.utils.Editors;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class WindowAdvisor extends WorkbenchWindowAdvisor {

	public WindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(800, 600));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);
		configurer.setShowMenuBar(true);
		configurer.setTitle("WARM");
	}

	@Override
	public void postWindowOpen() {
		if (!Editors.isOpen(HomePage.ID))
			HomePage.open();
	}

}
