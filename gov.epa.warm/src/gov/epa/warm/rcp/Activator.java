package gov.epa.warm.rcp;

import java.io.File;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.openlca.core.DataDir;
import org.openlca.julia.Julia;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.epa.warm.backend.app.LoggerConfig;
import gov.epa.warm.rcp.utils.HtmlFolder;
import gov.epa.warm.rcp.utils.Rcp;
import gov.epa.warm.rcp.utils.Resources;

public class Activator extends AbstractUIPlugin {

	private Logger log = LoggerFactory.getLogger(getClass());
	private static Activator plugin;

	public static Activator getDefault() {
		return plugin;
	}

	public void start(BundleContext context) throws Exception {
		log.info("Start application");
		super.start(context);
		plugin = this;
		File workspace = Rcp.getWorkspace();
		DataDir.setRoot(workspace);
		LoggerConfig.setUp();
		Julia.load();
		HtmlFolder.initialize(getBundle(), workspace, "resources/html.zip");
		Resources.initialize(workspace);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static ImageDescriptor imageDescriptor(String imageFilePath) {
		return imageDescriptorFromPlugin("gov.epa.warm", imageFilePath);
	}

}
