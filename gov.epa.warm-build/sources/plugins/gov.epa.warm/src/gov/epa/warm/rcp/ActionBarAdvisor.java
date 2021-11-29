package gov.epa.warm.rcp;

import gov.epa.warm.html.pages.HomePage;
import gov.epa.warm.html.pages.InputPage;
import gov.epa.warm.html.pages.ReportPage;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.IActionBarConfigurer;

public class ActionBarAdvisor extends
		org.eclipse.ui.application.ActionBarAdvisor {

	private IWorkbenchAction closeAction;
	private IWorkbenchAction closeAllAction;
	private IWorkbenchAction exitAction;
	private IWorkbenchAction saveAction;
	private IWorkbenchAction saveAllAction;
	private IWorkbenchAction saveAsAction;

	private Action homeAction;
	private Action openAction;
	private Action exportAction;

	public ActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.LEFT);
		coolBar.add(new ToolBarContributionItem(toolbar, "main"));
		toolbar.add(openAction);
		toolbar.add(new Separator());
		toolbar.add(saveAction);
		toolbar.add(saveAsAction);
		toolbar.add(saveAllAction);
		toolbar.add(exportAction);
		toolbar.add(new Separator());
		toolbar.add(homeAction);
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		super.fillMenuBar(menuBar);
		fillFileMenu(menuBar);
	}

	private void fillFileMenu(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("File", IWorkbenchActionConstants.M_FILE);
		fileMenu.add(openAction);
		fileMenu.add(new Separator());
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(saveAllAction);
		fileMenu.add(new Separator());
		fileMenu.add(closeAction);
		fileMenu.add(closeAllAction);
		fileMenu.add(new Separator());
		fileMenu.add(exitAction);
		menuBar.add(fileMenu);
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		saveAction = ActionFactory.SAVE.create(window);
		saveAsAction = ActionFactory.SAVE_AS.create(window);
		saveAllAction = ActionFactory.SAVE_ALL.create(window);
		closeAction = ActionFactory.CLOSE.create(window);
		closeAllAction = ActionFactory.CLOSE_ALL.create(window);
		exitAction = ActionFactory.QUIT.create(window);
		homeAction = createAction("Home", "resources/icons/home.gif", HomePage::open);
		openAction = createAction("Open...", "resources/icons/open.png", InputPage::openWithFile);
		exportAction = createAction("Export as HTML...", "resources/icons/export.gif", ReportPage::exportAsHtml);
	}

	private Action createAction(String text, String imagePath, Runnable runnable) {
		Action action = new Action() {
			@Override
			public void run() {
				if (runnable != null)
					runnable.run();
			}
		};
		action.setText(text);
		if (imagePath != null)
			action.setImageDescriptor(Activator.imageDescriptor(imagePath));
		return action;
	}

}
