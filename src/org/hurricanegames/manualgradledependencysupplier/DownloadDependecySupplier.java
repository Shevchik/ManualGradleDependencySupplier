package org.hurricanegames.manualgradledependencysupplier;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DownloadDependecySupplier implements DependencySupplier {

	private final String targetName;
	private final URL downloadSourceURL;

	public DownloadDependecySupplier(String targetName, URL downloadSourceURL) {
		this.targetName = targetName;
		this.downloadSourceURL = downloadSourceURL;
	}

	@Override
	public Path supply(Path targetDirectory) throws Exception {
		Path dependencyPath = targetDirectory.resolve(targetName);

		if (Files.notExists(dependencyPath)) {
			Files.copy(downloadSourceURL.openStream(), dependencyPath, StandardCopyOption.REPLACE_EXISTING);
		}

		return dependencyPath;
	}

}
