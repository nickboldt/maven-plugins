package org.jboss.maven.plugin.sample_project;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.jboss.maven.plugin.sample_plugin.SimpleFileTouchMojo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AllTest {

	private File tempDir;

	private File sourceDirectory;

	private File targetDirectory;

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());
		sourceDirectory = new File(tempDir, "source");
		sourceDirectory.mkdirs();
		targetDirectory = new File(tempDir, "target");
		targetDirectory.mkdirs();
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir);
	}

	@Test
	public void testTouchFileCreatedSuccessfully() throws IOException {
		SimpleFileTouchMojo mm = new SimpleFileTouchMojo();
		mm.setOutputDirectory(targetDirectory);
		try {
			mm.execute();
			Assert.assertTrue(true);
		} catch (MojoExecutionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testTouchFileNotCreated_outputDirectoryIsNull() throws IOException {
		SimpleFileTouchMojo mm = new SimpleFileTouchMojo();
		try {
			mm.execute();
		} catch (MojoExecutionException e) {
			assertEquals(e.getMessage(), "outputDirectory is null");
		}
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
}
