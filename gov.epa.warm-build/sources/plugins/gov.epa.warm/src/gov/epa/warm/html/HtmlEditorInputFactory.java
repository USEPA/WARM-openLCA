package gov.epa.warm.html;

import gov.epa.warm.html.pages.HomePage;
import gov.epa.warm.rcp.utils.Editors;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class HtmlEditorInputFactory implements IElementFactory {

	static final String ID = "gov.epa.warm.HtmlEditorInputFactory";

	@Override
	public IAdaptable createElement(IMemento memento) {
		// hack to avoid opening second home view
		homeViewHack(memento);
		String key = memento.getString("key");
		if (key == null || key.isEmpty())
			return null;
		HtmlEditorInput input = new HtmlEditorInput(key);
		String keys = memento.getString("dataKeys");
		if (keys != null && !keys.isEmpty())
			for (String dataKey : keys.split(HtmlEditorInput.KEY_SEPARATOR)) {
				String value = memento.getString(HtmlEditorInput.KEY_PREFIX + dataKey);
				input.getData().put(dataKey, value);
			}
		return input;
	}

	private void homeViewHack(IMemento memento) {
		String id = memento.getString("editorId");
		if (!HomePage.ID.equals(id))
			return;
		Editors.closeAll(HomePage.ID);
	}

}
