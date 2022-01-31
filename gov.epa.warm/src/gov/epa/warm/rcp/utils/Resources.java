package gov.epa.warm.rcp.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

public class Resources {

	public static void initialize(File workspace) throws IOException {
		File databaseDestination = new File(workspace, "databases/warm");
		InputStream databaseSource = Resources.class.getResourceAsStream("/resources/database.zip");
		extract(databaseSource, databaseDestination);
		File mappingsDestination = new File(workspace, "mappings");
		InputStream mappingsSource = Resources.class.getResourceAsStream("/resources/mappings.zip");
		extract(mappingsSource, mappingsDestination);
	}

	private static void extract(InputStream in, File directory) throws IOException {
		if (directory.exists())
			return;
		directory.mkdirs();
		ZipInputStream zip = new ZipInputStream(in);
		ZipEntry entry = null;
		while ((entry = zip.getNextEntry()) != null) {
			File to = new File(directory, entry.getName());
			if (entry.isDirectory()) {
				to.mkdirs();
				continue;
			}

			// Make sure folder is present before copying file.
			to.getParentFile().mkdirs();

			FileOutputStream out = new FileOutputStream(to);
			IOUtils.copy(zip, out);
			out.close();
		}
		zip.close();
	}

}
