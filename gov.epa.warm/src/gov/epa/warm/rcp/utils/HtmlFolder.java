package gov.epa.warm.rcp.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

/**
 * In the HTML folder, resources like HTML pages, JavaScript files etc. are
 * stored. The folder is located in the workspace directory with the name
 * 'html'. The name of the application plugin (or other plugins) is used as a
 * sub-folder within the HTML folder. A .version-file indicates the version of
 * the plugin.
 */
public class HtmlFolder {

	private static final Logger log = LoggerFactory.getLogger(HtmlFolder.class);

	/**
	 * Get the directory of the HTML folder for the given plugin in the given
	 * workspace directory.
	 * 
	 * @param bundle
	 *            The plugin for which the HTML folder should be returned.
	 * @param workspace
	 *            The workspace directory.
	 * @return The directory <workspace>/html/<plugin name>
	 */
	public static File getDir(Bundle bundle, File workspace) {
		File htmlDir = new File(workspace, "html");
		return new File(htmlDir, bundle.getSymbolicName());
	}

	/**
	 * Initializes the HTML folder for the given plugin. It extracts all
	 * resources from the given ZIP file into this folder if the version tag
	 * does not match with the version of the given bundle.
	 * 
	 * @param bundle
	 *            The plugin for which the HTML folder should be initialized.
	 * @param workspace
	 *            The workspace directory.
	 * @param zipPath
	 *            The plugin-relative path to a ZIP file that contains all HTML
	 *            resources of the given plugin (e.g. html/html_pages.zip)
	 */
	public static void initialize(Bundle bundle, File workspace, String zipPath) {
		if (!isValid(bundle) || zipPath == null)
			return;
		Version version = getWorkspaceVersion(bundle, workspace);
		if (Objects.equals(version, bundle.getVersion())) {
			log.trace("HTML folder for {} up-to-date");
			return;
		}
		log.trace("initialize html folder {} for {}", zipPath, bundle);
		try {
			extractFolder(bundle, workspace, zipPath);
		} catch (Exception e) {
			log.error("failed to extract HTML folder " + zipPath, e);
		}
	}

	private static boolean isValid(Bundle bundle) {
		if (bundle == null) {
			log.error("invalid bundle: NULL");
			return false;
		}
		if (bundle.getSymbolicName() == null) {
			log.error("invalid bundle: no symbolic name");
			return false;
		}
		if (bundle.getVersion() == null) {
			log.error("invalid bundle: no version");
			return false;
		}
		return true;
	}

	private static Version getWorkspaceVersion(Bundle bundle, File workspace) {
		File versionFile = new File(getDir(bundle, workspace), ".version");
		if (!versionFile.exists())
			return null;
		try {
			byte[] bytes = Files.readAllBytes(versionFile.toPath());
			String version = new String(bytes, "utf-8");
			return Version.parseVersion(version);
		} catch (Exception e) {
			log.error("failed to read HTML folder version", e);
			return null;
		}
	}

	private static void extractFolder(Bundle bundle, File workspace, String zipPath) throws Exception {
		File dir = getDir(bundle, workspace);
		if (dir.exists())
			FileUtils.deleteDirectory(dir);
		dir.mkdirs();
		writeVersion(bundle, workspace);
		InputStream zipStream = FileLocator.openStream(bundle,
				new Path(zipPath), false);
		File zipFile = new File(dir, "@temp.zip");
		try (FileOutputStream out = new FileOutputStream(zipFile)) {
			IOUtils.copy(zipStream, out);
		}
		ZipUtil.unpack(zipFile, dir);
		if (!zipFile.delete())
			zipFile.deleteOnExit();
	}

	private static void writeVersion(Bundle bundle, File workspace) throws Exception {
		File versionFile = new File(getDir(bundle, workspace), ".version");
		String version = bundle.getVersion().toString();
		Files.write(versionFile.toPath(), version.getBytes("utf-8"));
	}

	public static String getUrl(Bundle bundle, File workspace, String page) {
		File file = getFile(bundle, workspace, page);
		if (file == null)
			return null;
		try {
			URL url = file.toURI().toURL();
			return url.toString();
		} catch (Exception e) {
			log.error("failed to get URL for page " + bundle + "/" + page, e);
			return null;
		}
	}

	public static File getFile(Bundle bundle, File workspace, String page) {
		if (!isValid(bundle))
			return null;
		File file = new File(getDir(bundle, workspace), page);
		if (!file.exists()) {
			log.error("the requested file {} does not exist", file);
			return null;
		}
		return file;
	}

}
