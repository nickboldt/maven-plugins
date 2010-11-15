package org.jboss.tycho.plugins.p2.mirror;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @phase package
 * @goal materialize-products
 */
@SuppressWarnings("nls")
public final class MirrorMojo extends
		AbstractMirrorMojo {

	private static String ARTIFACT_MIRROR_APP_NAME = "org.eclipse.equinox.p2.artifact.repository.mirrorApplication";
	private static String METADATA_MIRROR_APP_NAME = "org.eclipse.equinox.p2.metadata.repository.mirrorApplication";

	private String sourceDirectory;
	private String targetDirectory;
	
	public String getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(String sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public String getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(String targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
		List<String> contentArgs = new ArrayList<String>();
		contentArgs.add("-source");
		contentArgs.add(sourceDirectory);
		contentArgs.add("-destination");
		contentArgs.add(targetDirectory);
		try {
			executeMirrorApplication(ARTIFACT_MIRROR_APP_NAME,
					(String[]) contentArgs.toArray(new String[contentArgs.size()]));
			executeMirrorApplication(METADATA_MIRROR_APP_NAME,
					(String[]) contentArgs.toArray(new String[contentArgs.size()]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
