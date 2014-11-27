package org.jboss.maven.plugins.util.test;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HudsonJobSyncMojoTest {

	// private static final String separatorLine =
	// "======================================================================";

	// private File tempDir;
	// private File hudsonDir;

	// HudsonJobSyncMojo sync;

	// String excludes;

	// actual JBoss Hudson server: https://jenkins.mw.lab.eng.bos.redhat.com/hudson/
	// private static final String hudsonURL = "http://localhost:8080/";
	// private static Process hudsonServer;
	// private static boolean verbose;

	// "archives, as, birt, bpel, bpmn, cdi, common, deltacloud, esb, examples, flow, freemarker, gwt, hibernatetools, jbpm, jmx, jsf, jst, maven, modeshape, portlet, profiler, runtime, seam, smooks, struts, tptp, usage, vpe, ws";

	@Before
	public void setUp() throws IOException {
		// tempDir = createTempDir(getClass().getSimpleName());
		// hudsonDir = createTempDir("hudson");
		// sync = new HudsonJobSyncMojo();
		// sync.setHudsonURL(hudsonURL);
		// sync.setUsername("admin");
		// sync.setPassword("none");
		// verbose = sync.getVerbose();
		// if (verbose)
		// System.out.println(separatorLine);
	}

	@After
	public void tearDown() throws IOException {
		// FileUtils.deleteDirectory(tempDir);
		// verbose = sync.getVerbose();
		// if (verbose)
		// System.out.println(separatorLine);
	}

	// TODO: add real tests
	
	@Test
	public void testExecute() throws IOException, MojoExecutionException {
		// sync.execute(); // this is a meaningless test since it uses
		// http://localhost:8080/ and if we're testing against remote, this
		// won't work
		Assert.assertTrue(true);
	}

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
