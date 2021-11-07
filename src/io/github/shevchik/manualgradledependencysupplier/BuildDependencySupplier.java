package io.github.shevchik.manualgradledependencysupplier;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildDependencySupplier implements DependencySupplier {

	private final URL downloadSourceURL;
	private final Path buildDirectory;
	private final Path downloadDestinationPath;
	private final List<String> buildCommand;
	private final Path resultArtifactPath;

	private final Map<String, String> buildEnv = new HashMap<>();

	public BuildDependencySupplier(URL sourceURL, Path workDirectory, String downloadDestinationFileName, List<String> buildCommand, List<String> buildArtifactPathElements) {
		this.downloadSourceURL = sourceURL;
		this.buildDirectory = workDirectory;
		this.downloadDestinationPath = workDirectory.resolve(downloadDestinationFileName);
		this.buildCommand = new ArrayList<>(buildCommand);
		Path lBuildArtifactPath = workDirectory;
		for (String buildArtifactPathElement : buildArtifactPathElements) {
			lBuildArtifactPath = lBuildArtifactPath.resolve(buildArtifactPathElement);
		}
		this.resultArtifactPath = lBuildArtifactPath;
	}

	public BuildDependencySupplier setBuildEnv(Map<String, String> buildEnv) {
		this.buildEnv.clear();
		this.buildEnv.putAll(buildEnv);
		return this;
	}

	@Override
	public Path supply() throws Exception {
		if (Files.exists(resultArtifactPath)) {
			return resultArtifactPath;
		}

		Files.createDirectories(buildDirectory);

		Files.copy(downloadSourceURL.openStream(), downloadDestinationPath, StandardCopyOption.REPLACE_EXISTING);

		ProcessBuilder buildProcess = new ProcessBuilder(buildCommand).directory(buildDirectory.toFile()).inheritIO();
		buildProcess.environment().putAll(buildEnv);
		buildProcess.start().waitFor();

		return resultArtifactPath;
	}

}
