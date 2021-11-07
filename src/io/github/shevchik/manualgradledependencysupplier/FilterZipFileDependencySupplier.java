package io.github.shevchik.manualgradledependencysupplier;

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
import java.util.function.Predicate;

public class FilterZipFileDependencySupplier implements DependencySupplier {

	private static final Map<String, Object> zipFSEnv = createZipFSEnv();
	private static Map<String, Object> createZipFSEnv() {
		Map<String, Object> env = new HashMap<>();
		env.put("create", Boolean.TRUE);
		return env;
	}

	private final DependencySupplier supplier;
	private final List<String> filterDirs;
	private final boolean filterAllows;
	private final Path workDirectory;
	private final String targetName;

	public FilterZipFileDependencySupplier(DependencySupplier supplier, List<String> filterDirs, boolean filterAllows, Path workDirectory) {
		this(supplier, filterDirs, filterAllows, workDirectory, null);
	}

	public FilterZipFileDependencySupplier(DependencySupplier supplier, List<String> filterDirs, boolean filterAllows, Path workDirectory, String targetName) {
		this.supplier = supplier;
		this.filterDirs = filterDirs;
		this.filterAllows = filterAllows;
		this.workDirectory = workDirectory;
		this.targetName = targetName;
	}

	@Override
	public Path supply() throws Exception {
		Path supplierPath = supplier.supply();

		Path resultArtifactPath = workDirectory.resolve(targetName != null ? targetName : supplierPath.getFileName().toString());
		if (Files.exists(resultArtifactPath)) {
			return resultArtifactPath;
		}

		Files.createDirectories(workDirectory);

		Predicate<Path> allowDirsPredicate = filterAllows ?
			path -> filterDirs.stream().anyMatch(dir -> path.startsWith(dir) || dir.startsWith(path.toString())) :
			path -> filterDirs.stream().noneMatch(dir -> path.startsWith(dir));
		Predicate<Path> allowFilePredicate = filterAllows ?
			path -> filterDirs.stream().anyMatch(dir -> path.startsWith(dir)) :
			path -> true;

		ClassLoader loader = ManualGradleDependencySupplier.class.getClassLoader();
		try (
			FileSystem sourceZipFS = FileSystems.newFileSystem(supplierPath, loader);
			FileSystem targetZipFS = FileSystems.newFileSystem(new URI("jar:file:" + resultArtifactPath.toUri().getPath()), zipFSEnv, loader)
		) {
			Files.walkFileTree(sourceZipFS.getRootDirectories().iterator().next(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if (dir.getNameCount() == 0) {
						return FileVisitResult.CONTINUE;
					}
					if (allowDirsPredicate.test(dir)) {
						Files.createDirectories(targetZipFS.getPath(dir.toString()));
						return FileVisitResult.CONTINUE;
					} else {
						return FileVisitResult.SKIP_SUBTREE;
					}
				}
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (allowFilePredicate.test(file)) {
						Files.copy(file, targetZipFS.getPath(file.toString()), StandardCopyOption.REPLACE_EXISTING);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}

		return resultArtifactPath;
	}

}
