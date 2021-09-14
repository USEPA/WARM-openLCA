package gov.epa.warm.rcp;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(final IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.setFixed(false);
		layout.addPlaceholder(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, 0.8f, editorArea);
	}
}
