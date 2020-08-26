package org.hurricanegames.manualgradledependencysupplier;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildDependencySupplier implements DependencySupplier {

	private final String targetName;
	private final URL downloadSourceURL;
	private final Path buildDirectory;
	private final Path downloadDestinationPath;
	private final List<String> buildCommand;
	private final Path buildArtifactPath;

	private final Map<String, String> buildEnv = new HashMap<>();

	public BuildDependencySupplier(String targetName, URL sourceURL, Path buildDirectory, String downloadDestinationFileName, List<String> buildCommand, List<String> buildArtifactPathElements) {
		this.targetName = targetName;
		this.downloadSourceURL = sourceURL;
		this.buildDirectory = buildDirectory;
		this.downloadDestinationPath = buildDirectory.resolve(downloadDestinationFileName);
		this.buildCommand = new ArrayList<>(buildCommand);
		Path lBuildArtifactPath = buildDirectory;
		for (String buildArtifactPathElement : buildArtifactPathElements) {
			lBuildArtifactPath = lBuildArtifactPath.resolve(buildArtifactPathElement);
		}
		this.buildArtifactPath = lBuildArtifactPath;
	}

	public BuildDependencySupplier setBuildEnv(Map<String, String> buildEnv) {
		this.buildEnv.clear();
		this.buildEnv.putAll(buildEnv);
		return this;
	}

	@Override
	public Path supply(Path targetDirectory) throws Exception {
		Path dependencyPath = targetDirectory.resolve(targetName);

		if (Files.notExists(dependencyPath)) {
			Files.createDirectories(buildDirectory);

			Files.copy(downloadSourceURL.openStream(), downloadDestinationPath, StandardCopyOption.REPLACE_EXISTING);

			ProcessBuilder buildProcess = new ProcessBuilder(buildCommand).directory(buildDirectory.toFile()).inheritIO();
			buildProcess.environment().putAll(buildEnv);
			buildProcess.start().waitFor();

			Files.copy(buildArtifactPath, dependencyPath);
		}

		return dependencyPath;
	}

}
