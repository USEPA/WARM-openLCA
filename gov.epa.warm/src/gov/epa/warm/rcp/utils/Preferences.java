package gov.epa.warm.rcp.utils;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import gov.epa.warm.rcp.Activator;

public final class Preferences extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
	}

	public static IPreferenceStore getStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	public static void set(String key, String value) {
		if (key == null)
			return;
		IPreferenceStore store = getStore();
		if (store == null)
			return;
		String val = value == null ? "" : value;
		store.setValue(key, val);
	}

	public static String get(String key) {
		if (key == null)
			return "";
		IPreferenceStore store = getStore();
		if (store == null)
			return "";
		return store.getString(key);
	}

}
