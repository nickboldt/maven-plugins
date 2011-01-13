package org.jboss.maven.plugins.util.test;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.dom4j.Document;
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
	// private static Process hudsonServer;
	private static boolean verbose;

	private static String components = "TESTING";
	
	private static String viewPath = "";

	// "archives, as, birt, bpel, bpmn, cdi, common, deltacloud, esb, examples, flow, freemarker, gwt, hibernatetools, jbpm, jmx, jsf, jst, maven, modeshape, portlet, profiler, runtime, seam, smooks, struts, tptp, usage, vpe, ws";

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());
		hudsonDir = createTempDir("hudson");
		publisher = new HudsonJobPublisherMojo();
		publisher.setHudsonURL(hudsonURL);
		publisher.setUsername("admin");
		publisher.setPassword("none");
		verbose = publisher.getVerbose();
		publisher.setViewPath(""); // don't use a viewPath value because localhost doesn't have view/DevStudio_Trunk/ available
		
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

	// // TODO: start up a unique, temporary Hudson instance
	// // testStartHudson must be first test
	// @Test
	// public void testStartHudson() throws IOException
	// {
	// String[] cmd = {"/bin/sh", "-c", "java -DHUDSON_HOME=" + hudsonDir +
	// " -jar /tmp/hudson.war –httpPort=8180"};
	// Process hudsonServer = Runtime.getRuntime().exec(cmd);
	// (new Sleep()).doSleep(5000);
	// }

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
		String[] names = null;
		try {
			names = publisher.getJobNames(viewPath);
			for (int i = 0; i < names.length; i++) {
				// System.out.println(":: " + names[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(names != null);
		String[] componentArray = components.split("[, ]+");
		Assert.assertTrue(componentArray.length != 0);
		for (int i = 0; i < componentArray.length; i++) {
			boolean match = false;
			for (int j = 0; j < names.length; j++) {
				if (names[j].equals(publisher.getJbosstoolsJobnamePrefix()
						+ componentArray[i]
						+ publisher.getComponentJobNameSuffix())) {
					match = true;
					break;
				}
			}
			Assert.assertTrue(match);
		}
	}

	@Test
	public void testAddProperties() throws IOException, MojoExecutionException {
		Properties jobProperties = new Properties();
		jobProperties
				.put("jbosstools-pi4soa-3.1_stable_branch",
						"https://pi4soa.svn.sourceforge.net/svnroot/pi4soa/branches/pi4soa-3.1.x");
		jobProperties.put("jbosstools-teiid-designer-7.1_stable_branch",
				"http://anonsvn.jboss.org/repos/tdesigner/branches/7.1");
		jobProperties.put("jbosstools-savara-1.1_stable_branch",
				"http://anonsvn.jboss.org/repos/savara/branches/1.1.x");
		publisher.setJobProperties(jobProperties);
		publisher.execute();
		String[] names = null;
		try {
			names = publisher.getJobNames(viewPath);
			for (int i = 0; i < names.length; i++) {
				// System.out.println(":: " + names[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(names != null);

		Enumeration jobNames = jobProperties.propertyNames();
		while (jobNames.hasMoreElements()) {
			String jobName = (String) jobNames.nextElement();
			String sourcesURL = jobProperties.getProperty(jobName);
			boolean match = false;
			for (int j = 0; j < names.length; j++) {
				if (names[j].equals(jobName)) {
					match = true;
					break;
				}
			}
			Assert.assertTrue(match);
		}
	}

	@Test
	public void testCopyJob() throws IOException, MojoExecutionException {
		testAddComponents();
		publisher.execute();
		String[] names = null;
		try {
			names = publisher.getJobNames(viewPath);
			for (int i = 0; i < names.length; i++) {
				// System.out.println(":: " + names[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(names != null);
		Document configXML = null;
		String jobToCopy = "jbosstools-TESTING_trunk";

		// delete existing job
		if (publisher.isReplaceExistingJob()) {
			publisher.deleteJob(publisher.getJobTemplateFile(), jobToCopy,
					false);
		}
		publisher.createOrUpdateJob(publisher.getJobTemplateFile(), jobToCopy);

		try {
			configXML = publisher.getJobConfigXML(jobToCopy);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (configXML != null) {
			publisher.copyJob(configXML.asXML(), jobToCopy,
					jobToCopy + "-copy", true);
		}

		try {
			names = publisher.getJobNames(viewPath);
			for (int i = 0; i < names.length; i++) {
				// System.out.println(":: " + names[i]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertTrue(publisher.jobExists(jobToCopy));
		Assert.assertTrue(publisher.jobExists(jobToCopy + "-copy"));
	}

	@Test
	public void testCopyConfigXMLToNewJob() throws IOException,
			MojoExecutionException {
		testAddComponents();
		publisher.execute();
		String[] names = null;
		try {
			names = publisher.getJobNames(viewPath);
			for (int i = 0; i < names.length; i++) {
				// System.out.println(":: " + names[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(names != null);
		Document configXML = null;
		String jobToCopy = "jbosstools-TESTING_trunk";

		// delete existing job
		if (publisher.isReplaceExistingJob()) {
			publisher.deleteJob(publisher.getJobTemplateFile(), jobToCopy,
					false);
		}
		publisher.createOrUpdateJob(publisher.getJobTemplateFile(), jobToCopy);

		try {
			configXML = publisher.getJobConfigXML(jobToCopy);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (configXML != null) {
			publisher.copyJobConfigXML(configXML.asXML(), jobToCopy, jobToCopy
					+ "-copy", true);
		}

		try {
			names = publisher.getJobNames(viewPath);
			for (int i = 0; i < names.length; i++) {
				// System.out.println(":: " + names[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertTrue(publisher.jobExists(jobToCopy));
		Assert.assertTrue(publisher.jobExists(jobToCopy + "-copy"));
	}

	@Test
	public void testCopyAndModifyJob() throws Exception, MojoExecutionException {
		publisher.setComponentJobNameSuffix("_trunk");
		publisher.setComponentJobNameSuffix2("_stable_build");
		publisher.setBranchOrTag("branches/jbosstools-3.2.0.CR1");
		testAddComponents();

		String xmlFile = publisher.getJobTemplateFile();
		publisher.loadComponentsIntoJobList();
		// work on those Properties - create or update jobs as needed
		publisher.createJobsFromJobList(xmlFile);

		// for any jobs ending with componentJobNameSuffix, copy them and edit
		// them using componentJobNameSuffix2 as new name suffix
		// then update both sourcesURL and buildURL by replacing trunk w/
		// branchOrTag
		publisher.copyJobs();
	}

	// // testShutdownHudson must be last test
	// @Test
	// public void testShutdownHudson() throws IOException
	// {
	// hudsonServer.destroy();
	// }

	public void execute() throws MojoExecutionException, MojoFailureException {
	}

	public File createTempDir(String prefix) throws IOException {
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
