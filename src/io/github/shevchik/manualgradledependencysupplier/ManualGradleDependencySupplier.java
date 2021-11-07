package io.github.shevchik.manualgradledependencysupplier;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
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

	public static FileCollection supplyDependecies(List<DependencySupplier> suppliers) throws Exception {
		Set<Path> libraries = new HashSet<>();
		for (DependencySupplier supplier : suppliers) {
			libraries.add(supplier.supply());
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
