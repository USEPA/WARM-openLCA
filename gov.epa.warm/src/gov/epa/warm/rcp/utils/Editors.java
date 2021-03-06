package gov.epa.warm.rcp.utils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Editors {

	private static Logger log = LoggerFactory.getLogger(Editors.class);

	private Editors() {
	}

	public static void closeAll() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
		} catch (Exception e) {
			log.error("Failed to close editors", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends IEditorPart> T getActive() {
		try {
			return (T) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		} catch (ClassCastException e) {
			log.error("Error getting active editor", e);
			return null;
		}
	}

	public static boolean isOpen(IEditorInput input, String editorId) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] editors = page.findEditors(input, editorId,
				IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
		return editors != null && editors.length > 0;
	}

	public static boolean isOpen(String editorId) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] editors = page.findEditors(null, editorId, IWorkbenchPage.MATCH_ID);
		return editors != null && editors.length > 0;
	}

	public static void open(IEditorInput input, String editorId) {
		new OpenInUIJob(input, editorId).schedule();
	}

	public static void closeAll(String id) {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditors(getReferences(id), true);
	}

	public static void close(IEditorReference ref) {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.closeEditor(ref.getEditor(false), true);
		} catch (Exception e) {
			log.error("Failed to close an editor", e);
		}
	}

	public static IEditorReference[] getReferences(String id) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		return page.findEditors(null, id, IWorkbenchPage.MATCH_ID);
	}

	public static IEditorReference[] getReferences() {
		try {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		} catch (Exception e) {
			log.error("Failed to get editor references", e);
			return new IEditorReference[0];
		}
	}

	private static class OpenInUIJob extends UIJob {

		private IEditorInput input;
		private String editorId;

		public OpenInUIJob(IEditorInput input, String editorId) {
			super("Open editor");
			this.input = input;
			this.editorId = editorId;
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, editorId);
				return Status.OK_STATUS;
			} catch (Exception e) {
				log.error("Open editor " + editorId + " failed.", e);
				return Status.CANCEL_STATUS;
			}
		}
	}

}
