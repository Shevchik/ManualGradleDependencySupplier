package org.hurricanegames.manualgradledependencysupplier;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.AbstractFileCollection;


public class ManualGradleDependencySupplier implements Plugin<Project> {

	@Override
	public void apply(Project project) {
	}

	public static FileCollection supplyDependecies(Path targetDirectory, List<DependencySupplier> suppliers) throws Exception {
		return supplyDependecies(targetDirectory, suppliers, false);
	}

	public static FileCollection supplyDependecies(Path targetDirectory, List<DependencySupplier> suppliers, boolean cleanTarget) throws Exception {
		Files.createDirectories(targetDirectory);
		Set<Path> libraries = new HashSet<>();
		for (DependencySupplier supplier : suppliers) {
			libraries.add(supplier.supply(targetDirectory));
		}
		if (cleanTarget) {
			Files.walkFileTree(targetDirectory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			        Objects.requireNonNull(file);
			        Objects.requireNonNull(attrs);
			        if (!libraries.contains(file)) {
						Files.delete(file);
			        }
			        return FileVisitResult.CONTINUE;
				}
			});
		}
		return new AbstractFileCollection() {
			final Set<File> files = new HashSet<>(libraries.stream().map(Path::toFile).collect(Collectors.toList()));
			@Override
			public Set<File> getFiles() {
				return files;
			}
			@Override
			public String getDisplayName() {
				return "ManualGradleDependencySupplier file collection of " + getAsPath();
			}
		};
	}

}
