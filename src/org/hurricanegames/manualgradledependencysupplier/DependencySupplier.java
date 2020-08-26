package org.hurricanegames.manualgradledependencysupplier;

import java.nio.file.Path;

public interface DependencySupplier {

	public Path supply(Path targetDirectory) throws Exception;

}
