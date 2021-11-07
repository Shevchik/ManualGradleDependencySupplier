package io.github.shevchik.manualgradledependencysupplier;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DownloadDependencySupplier implements DependencySupplier {

	private final URL downloadSourceURL;
	private final Path resultArtifactPath;

	public DownloadDependencySupplier(URL downloadSourceURL, Path workDirectory) {
		this(downloadSourceURL, workDirectory, null);
	}

	public DownloadDependencySupplier(URL downloadSourceURL, Path workDirectory, String targetName) {
		this.downloadSourceURL = downloadSourceURL;
		this.resultArtifactPath = workDirectory.resolve(targetName != null ? targetName : new File(downloadSourceURL.getPath()).getName());
	}

	@Override
	public Path supply() throws Exception {
		if (Files.exists(resultArtifactPath)) {
			return resultArtifactPath;
		}

		Files.createDirectories(resultArtifactPath.getParent());
		Files.copy(downloadSourceURL.openStream(), resultArtifactPath, StandardCopyOption.REPLACE_EXISTING);

		return resultArtifactPath;
	}

}
