package gov.epa.warm.rcp.utils;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A helper class for selecting a file for an import or export via a file
 * dialog.
 */
public class FileChooser {

	private static String getDialogText(int swtFlag) {
		switch (swtFlag) {
		case SWT.OPEN:
			return "Open file...";
		case SWT.SAVE:
			return "Save file...";
		default:
			return "";
		}
	}

	private static String openFileDialog(Shell shell, String extension, String defaultName, int swtFlag) {
		FileDialog dialog = new FileDialog(shell, swtFlag);
		dialog.setText(getDialogText(swtFlag));
		if (extension != null)
			dialog.setFilterExtensions(new String[] { extension });
		if (defaultName != null && defaultName.contains(File.separator)) {
			String dir = defaultName.substring(0, defaultName.lastIndexOf(File.separatorChar));
			dialog.setFilterPath(dir);
			defaultName = defaultName.substring(defaultName.lastIndexOf(File.separatorChar) + 1);
		}
		dialog.setFileName(defaultName);
		return dialog.open();
	}

	private static File selectForPath(String path) {
		File file = new File(path);
		if (!file.exists() || file.isDirectory())
			return file;
		boolean write = MessageDialog.openQuestion(UI.shell(),
				"File already exists",
				"Do you want to override the existing file?");
		if (write)
			return file;
		return null;
	}

	public static File forSaving(String extension, String defaultName) {
		Shell shell = UI.shell();
		if (shell == null)
			return null;
		String path = openFileDialog(shell, extension, defaultName, SWT.SAVE);
		if (path == null)
			return null;
		return selectForPath(path);
	}

	public static File forOpening(String extension) {
		Shell shell = UI.shell();
		if (shell == null)
			return null;
		String path = openFileDialog(shell, extension, null, SWT.OPEN);
		if (path == null)
			return null;
		File file = new File(path);
		if (!file.exists())
			return null;
		return file;
	}
}
