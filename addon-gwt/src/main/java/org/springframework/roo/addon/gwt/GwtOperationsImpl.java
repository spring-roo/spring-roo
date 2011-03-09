package org.springframework.roo.addon.gwt;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.web.mvc.controller.WebMvcOperations;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

import java.io.File;
import java.util.List;

/**
 * Provides GWT installation services.
 *
 * @author Ben Alex
 * @author Alan Stewart
 * @author Stefan Schmidt
 * @author Ray Cromwell
 * @author Amit Manjhi
 * @since 1.1
 */
@Component
@Service
public class GwtOperationsImpl implements GwtOperations {
	@Reference private FileManager fileManager;
	@Reference private GwtConfigService gwtConfigService;
	@Reference private MetadataService metadataService;
	@Reference private WebMvcOperations mvcOperations;
	@Reference private ProjectOperations projectOperations;

	public boolean isSetupGwtAvailable() {
		if (!projectOperations.isProjectAvailable()) {
			return false;
		}

		// Do not permit installation if they have a gwt package already in their project shared is allowed
		for (GwtPath path : GwtPath.values()) {
			if (path == GwtPath.MANAGED_REQUEST || path == GwtPath.SCAFFOLD || path == GwtPath.MANAGED || path == GwtPath.MANAGED_UI) {
				String fPath = path.canonicalFileSystemPath(projectOperations.getProjectMetadata());
				if (fileManager.exists(fPath)) {
					return false;
				}
			}
		}
		return true;
	}

	public void setupGwt() {
		// Install web pieces if not already installed
		if (!fileManager.exists(projectOperations.getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, "/WEB-INF/web.xml"))) {
			mvcOperations.installAllWebMvcArtifacts();
		}

		gwtConfigService.updateConfiguration(true);

		// Get configuration.xml as document
		Element configuration = XmlUtils.getConfiguration(getClass());

		// Add POM repositories
		updateRepositories(configuration);

		// Do a "get" for every .java file, thus ensuring the metadata is fired
		PathResolver pathResolver = projectOperations.getPathResolver();
		FileDetails srcRoot = new FileDetails(new File(pathResolver.getRoot(Path.SRC_MAIN_JAVA)), null);
		String antPath = pathResolver.getRoot(Path.SRC_MAIN_JAVA) + File.separatorChar + "**" + File.separatorChar + "*.java";
		for (FileDetails fd : fileManager.findMatchingAntPath(antPath)) {
			String fullPath = srcRoot.getRelativeSegment(fd.getCanonicalPath());
			fullPath = fullPath.substring(1, fullPath.lastIndexOf(".java")).replace(File.separatorChar, '.'); // ditch the first / and .java
			JavaType javaType = new JavaType(fullPath);
			String id = GwtMetadata.createIdentifier(javaType, Path.SRC_MAIN_JAVA);
			metadataService.get(id);
		}
	}

	private void updateRepositories(Element configuration) {
		List<Element> repositories = XmlUtils.findElements("/configuration/gwt/repositories/repository", configuration);
		for (Element repositoryElement : repositories) {
			projectOperations.addRepository(new Repository(repositoryElement));
		}

		List<Element> pluginRepositories = XmlUtils.findElements("/configuration/gwt/pluginRepositories/pluginRepository", configuration);
		for (Element repositoryElement : pluginRepositories) {
			projectOperations.addPluginRepository(new Repository(repositoryElement));
		}
	}
}
