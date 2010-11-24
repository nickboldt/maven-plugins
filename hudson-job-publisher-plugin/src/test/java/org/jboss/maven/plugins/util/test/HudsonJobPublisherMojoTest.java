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
import org.jboss.maven.plugins.util.HudsonJobPublisherMojo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HudsonJobPublisherMojoTest {

	private static final String separatorLine = "======================================================================";

	private File tempDir;
	private File hudsonDir;

	HudsonJobPublisherMojo publisher;

	String excludes;

	// actual JBoss Hudson server: http://hudson.qa.jboss.com/hudson/
	private static final String hudsonURL = "http://localhost:8080/";
//	private static Process hudsonServer;
	private static boolean verbose;

	private static String components = "archives, as, birt, bpel, bpmn, cdi, common, deltacloud, esb, examples, flow, freemarker, gwt, hibernatetools, jbpm, jmx, jsf, jst, maven, modeshape, portlet, profiler, runtime, seam, smooks, struts, tptp, usage, vpe, ws";

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());
		hudsonDir = createTempDir("hudson");
		publisher = new HudsonJobPublisherMojo();
		publisher.setHudsonURL(hudsonURL);
		publisher.setUsername("admin");
		publisher.setPassword("none");
		verbose = publisher.getVerbose();
		if (verbose)
			System.out.println(separatorLine);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir);
		verbose = publisher.getVerbose();
		if (verbose)
			System.out.println(separatorLine);
	}

//	// TODO: start up a unique, temporary Hudson instance
//	// testStartHudson must be first test 
//	@Test
//	public void testStartHudson() throws IOException
//	{
//		String[] cmd = {"/bin/sh", "-c", "java -DHUDSON_HOME=" + hudsonDir + " -jar /tmp/hudson.war –httpPort=8180"};
//		Process hudsonServer = Runtime.getRuntime().exec(cmd);
//		(new Sleep()).doSleep(5000);
//	}

	@Test
	public void testAddComponents() throws IOException, MojoExecutionException {
		// Note: "drools", "xulrunner", "pi4soa", "teiid", "savara" are special
		// cases
		publisher.setComponents(components);
		Assert.assertTrue(publisher.getComponents().equals(components));
	}

	@Test
	public void testAddComponentJobNameSuffix() throws IOException,
			MojoExecutionException {
		publisher.setComponentJobNameSuffix("-test-suffix");
		Assert.assertTrue(publisher.getComponentJobNameSuffix().equals(
				"-test-suffix"));
	}

	@Test
	public void testAddComponentJobs() throws IOException,
			MojoExecutionException {
		testAddComponents();
		testAddComponentJobNameSuffix();
		publisher.execute();
		String jobList = null;
		try {
			jobList = publisher.listJobsOnSecureServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(jobList != null);
		String[] componentArray = components.split("[, ]+");
		Assert.assertTrue(componentArray.length == 30);
		for (int i = 0; i < componentArray.length; i++) {
			Assert.assertTrue(jobList.indexOf(publisher.JOB_NAME
					+ publisher.JBOSSTOOLS_JOBNAME_PREFIX + componentArray[i]
					+ publisher.getComponentJobNameSuffix()) >= 0);
		}
	}

	@Test
	public void testAddProperties() throws IOException, MojoExecutionException {
		Properties jobProperties = new Properties();
		jobProperties
				.put("jbosstools-pi4soa-stable-branch",
						"https://pi4soa.svn.sourceforge.net/svnroot/pi4soa/branches/pi4soa-3.1.x");
		jobProperties.put("jbosstools-teiid-designer-stable-branch",
				"http://anonsvn.jboss.org/repos/tdesigner/branches/7.1");
		jobProperties.put("jbosstools-savara-stable-branch",
				"http://anonsvn.jboss.org/repos/savara/branches/1.1.x");
		publisher.setJobProperties(jobProperties);
		publisher.execute();
		String jobList = null;
		try {
			jobList = publisher.listJobsOnSecureServer();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Enumeration jobNames = jobProperties.propertyNames();
		while (jobNames.hasMoreElements()) {
			String jobName = (String) jobNames.nextElement();
			String sourcesURL = jobProperties.getProperty(jobName);
			Assert.assertTrue(jobList.indexOf(publisher.JOB_NAME + jobName) >= 0);
		}

	}

//	// testShutdownHudson must be last test
//	@Test
//	public void testShutdownHudson() throws IOException
//	{
//		hudsonServer.destroy();
//	}

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
