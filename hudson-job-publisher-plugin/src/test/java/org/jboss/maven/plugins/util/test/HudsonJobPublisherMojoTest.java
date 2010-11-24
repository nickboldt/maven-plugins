package org.jboss.maven.plugins.util.test;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.jboss.maven.plugins.util.HudsonJobPublisherMojo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HudsonJobPublisherMojoTest {

	private static final String separatorLine = "======================================================================";

	private File tempDir;

	HudsonJobPublisherMojo publisher;

	String excludes;

	private static final String hudsonURL = "http://localhost:8080/";

	private static boolean verbose;

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());
		publisher = new HudsonJobPublisherMojo();
		publisher.setHudsonURL(hudsonURL);
		verbose = publisher.getVerbose();
		if (verbose)
			System.out.println(separatorLine);
		// TODO: start up a unique, temporary Hudson instance
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir);
		verbose = publisher.getVerbose();
		if (verbose)
			System.out.println(separatorLine);
		// TODO: tear down temporary Hudson instance
	}

	@Test
	public void testTrue() throws IOException,
			MojoExecutionException {
		publisher.execute();
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
