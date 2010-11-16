package org.jboss.maven.plugins.unique.gav.test;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.jboss.tycho.plugins.unique.gav.UniqueGAVMojo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UniqueGAVMojoTest {

	private File tempDir;

	/**
	 * @parameter
	 */
	private File sourceDirectory;

	UniqueGAVMojo uniq;

	String excludes;

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());
		if (sourceDirectory == null) {
			sourceDirectory = new File("/home/nboldt/tru/jmx/");
		}
		sourceDirectory.mkdirs();
		uniq = new UniqueGAVMojo();
		uniq.setSourceDirectory(sourceDirectory);
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
	public void testInfo() throws IOException, MojoExecutionException {
		uniq.setDoInfo(true);
		uniq.setDoWarn(false);
		uniq.setDoError(false);
		uniq.execute();
		Assert.assertTrue(true);
	}

	@Test
	public void testWarnings() throws IOException, MojoExecutionException {
		uniq.setDoInfo(false);
		uniq.setDoWarn(true);
		uniq.setDoError(false);
		uniq.execute();
		Assert.assertTrue(true);
	}

	@Test
	public void testErrors() throws IOException, MojoExecutionException {
		uniq.setDoInfo(false);
		uniq.setDoWarn(false);
		uniq.setDoError(true);
		uniq.execute();
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
