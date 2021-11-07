package io.github.shevchik.manualgradledependencysupplier;

import java.nio.file.Path;

public interface DependencySupplier {

	public Path supply() throws Exception;

}
