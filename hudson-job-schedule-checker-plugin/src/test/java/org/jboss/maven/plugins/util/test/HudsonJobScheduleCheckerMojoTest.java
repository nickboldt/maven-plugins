package org.jboss.maven.plugins.util.test;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.tools.ant.taskdefs.Sleep;
import org.codehaus.plexus.util.FileUtils;
import org.jboss.maven.plugins.util.HudsonJobScheduleCheckerMojo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HudsonJobScheduleCheckerMojoTest {

	private static final String separatorLine = "======================================================================";

	private File tempDir;
	private File hudsonDir;

	HudsonJobScheduleCheckerMojo scheduleChecker;

	// private static final String hudsonURL =
	// "https://jenkins.mw.lab.eng.bos.redhat.com/hudson/";
	private static final String hudsonURL = "http://localhost:8080/";
	private static boolean verbose = false;

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());
		hudsonDir = createTempDir("hudson");
		scheduleChecker = new HudsonJobScheduleCheckerMojo();
		scheduleChecker.setHudsonURL(hudsonURL);
		scheduleChecker.setUsername("SET USERNAME HERE");
		scheduleChecker.setPassword("SET PASSWORD HERE");
		scheduleChecker.setVerbose(verbose);
		if (verbose)
			System.out.println(separatorLine);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir);
		verbose = scheduleChecker.getVerbose();
		if (verbose)
			System.out.println(separatorLine);
	}

	// TODO: add real tests

	@Test
	public void testExecute() throws IOException, MojoExecutionException {
		scheduleChecker.execute();
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
