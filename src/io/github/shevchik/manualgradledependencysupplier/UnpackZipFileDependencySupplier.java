package io.github.shevchik.manualgradledependencysupplier;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class UnpackZipFileDependencySupplier implements DependencySupplier {

	protected static String getLastPathElement(String path) {
		String[] elements = path.split("/");
		return elements[elements.length - 1];
	}

	private final DependencySupplier supplier;
	private final String unpackPath;
	private final Path workDirectory;
	private final String targetName;

	public UnpackZipFileDependencySupplier(DependencySupplier supplier, String unpackPath, Path workDirectory) {
		this(supplier, unpackPath, workDirectory, getLastPathElement(unpackPath));
	}

	public UnpackZipFileDependencySupplier(DependencySupplier supplier, String unpackPath, Path workDirectory, String targetName) {
		this.supplier = supplier;
		this.unpackPath = unpackPath;
		this.workDirectory = workDirectory;
		this.targetName = targetName;
	}

	@Override
	public Path supply() throws Exception {
		Path resultArtifactPath = workDirectory.resolve(targetName);
		if (Files.exists(resultArtifactPath)) {
			return resultArtifactPath;
		}

		Path supplierPath = supplier.supply();

		Files.createDirectories(workDirectory);


		ClassLoader loader = ManualGradleDependencySupplier.class.getClassLoader();
		try (FileSystem sourceZipFS = FileSystems.newFileSystem(supplierPath, loader)) {
			Files.copy(sourceZipFS.getPath(unpackPath), resultArtifactPath);
		}

		return resultArtifactPath;
	}

}
