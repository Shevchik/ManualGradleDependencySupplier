package org.hurricanegames.manualgradledependencysupplier;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterZipFileDependencySupplier implements DependencySupplier {

	private static final Map<String, Object> zipFSEnv = createZipFSEnv();
	private static Map<String, Object> createZipFSEnv() {
		Map<String, Object> env = new HashMap<>();
		env.put("create", Boolean.TRUE);
		return env;
	}

	private final String targetName;
	private final Path buildDirectory;
	private final DependencySupplier supplier;
	private final List<String> allowedDirs;

	public FilterZipFileDependencySupplier(String targetName, Path buildDirectory, DependencySupplier supplier, List<String> allowedDirs) {
		this.targetName = targetName;
		this.buildDirectory = buildDirectory;
		this.supplier = supplier;
		this.allowedDirs = allowedDirs;
	}

	@Override
	public Path supply(Path targetDirectory) throws Exception {
		Path resultPath = targetDirectory.resolve(targetName).toAbsolutePath();
		if (Files.exists(resultPath)) {
			return resultPath;
		}

		ClassLoader loader = ManualGradleDependencySupplier.class.getClassLoader();
		try (
			FileSystem sourceZipFS = FileSystems.newFileSystem(supplier.supply(buildDirectory), loader);
			FileSystem targetZipFS = FileSystems.newFileSystem(new URI("jar:file:" + resultPath.toUri().getPath()), zipFSEnv, loader)
		) {
			Files.walkFileTree(sourceZipFS.getRootDirectories().iterator().next(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if (dir.getNameCount() == 0) {
						return FileVisitResult.CONTINUE;
					}
					if (allowedDirs.stream().anyMatch(allowedDir -> dir.startsWith(allowedDir) || allowedDir.startsWith(dir.toString()))) {
						Files.createDirectories(targetZipFS.getPath(dir.toString()));
						return FileVisitResult.CONTINUE;
					} else {
						return FileVisitResult.SKIP_SUBTREE;
					}
				}
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (allowedDirs.stream().anyMatch(allowedDir -> file.startsWith(allowedDir))) {
						Files.copy(file, targetZipFS.getPath(file.toString()), StandardCopyOption.REPLACE_EXISTING);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}

		return resultPath;
	}

}
