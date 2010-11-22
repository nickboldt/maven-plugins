package org.jboss.maven.plugins.util.test;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.jboss.maven.plugins.util.SiteAggregateMojo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SiteAggregateMojoTest {

	private File tempDir;

	/**
	 * @parameter
	 */
	private String sourceURL;

	SiteAggregateMojo site;

	String excludes;

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());

		if (sourceURL == null) {
			sourceURL = "http://download.jboss.org/jbosstools/builds/staging/";
			// sourceURL = "file:///home/nboldt/tru/jmx/"; // file:// not
			// supported
		}
		site = new SiteAggregateMojo();
		site.setSourceURL(sourceURL);
		System.out
				.println("======================================================================");
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir);
		System.out
				.println("======================================================================");
	}

	@Test
	public void testNothing() throws IOException, MojoExecutionException {
		site.execute();
		Assert.assertTrue(true);
	}

	private File createTempDir(String prefix) throws IOException {
		File directory = File.createTempFile(prefix, "");
		if (directory.delete()) {
			directory.mkdirs();
			return directory;
		} else {
			throw new IOException("Could not create temp directory at: "
					+ directory.getAbsolutePath());
		}
	}

	public void execute() throws MojoExecutionException, MojoFailureException {
	}

}
