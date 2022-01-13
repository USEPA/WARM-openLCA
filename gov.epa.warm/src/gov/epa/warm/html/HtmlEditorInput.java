package gov.epa.warm.html;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

public class HtmlEditorInput implements IEditorInput {

	static final String KEY_SEPARATOR = "_@_";
	static final String KEY_PREFIX = "data.";

	private final String key;
	private final String name = "input";
	private String filename;
	private Map<String, String> data = new HashMap<>();

	public HtmlEditorInput(String key) {
		this.key = key;
	}

	public HtmlEditorInput(String key, File file) throws IOException {
		this.key = key;
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			getData().put("userInputs", reader.readLine());
			getData().put("filename", file.getAbsolutePath());
		}
	}

	public String getKey() {
		return key;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Map<String, String> getData() {
		return data;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IPersistableElement getPersistable() {
		return new IPersistableElement() {

			@Override
			public void saveState(IMemento memento) {
				memento.putString("key", key);
				StringBuilder keys = new StringBuilder();
				for (String key : data.keySet()) {
					if (keys.length() != 0)
						keys.append(KEY_SEPARATOR);
					keys.append(key);
					memento.putString(KEY_PREFIX + key, data.get(key));
				}
				memento.putString("dataKeys", keys.toString());
			}

			@Override
			public String getFactoryId() {
				return HtmlEditorInputFactory.ID;
			}
		};
	}

	@Override
	public String getToolTipText() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!obj.getClass().equals(this.getClass()))
			return false;
		HtmlEditorInput other = (HtmlEditorInput) obj;
		return Objects.equals(this.key, other.key);
	}
}
