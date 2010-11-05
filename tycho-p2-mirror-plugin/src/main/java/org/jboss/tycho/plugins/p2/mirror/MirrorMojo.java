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

	private String sourceDir;
	public String getSourceDir() {
		return sourceDir;
	}

	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}

	public String getTargetDir() {
		return targetDir;
	}

	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}

	private String targetDir;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		List<String> contentArgs = new ArrayList<String>();
		contentArgs.add("-source");
		contentArgs.add(sourceDir);
		contentArgs.add("-destination");
		contentArgs.add(targetDir);
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
