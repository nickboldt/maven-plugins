package org.jboss.maven.plugin.test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.jboss.tycho.plugins.source.feature.SourceFeatureMojo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SourceFeatureMojoTest {

	private File tempDir;

	private File sourceDirectory;
	
	SourceFeatureMojo sfm;

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());
		// sourceDirectory = new File(tempDir, ".");
		sourceDirectory = new File("/home/nboldt/tru/jmx");
		sourceDirectory.mkdirs();
		sfm = new SourceFeatureMojo();
		sfm.setSourceDirectory(sourceDirectory);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir);
	}

	@Test
	public void testTouchFileCreatedSuccessfully() throws IOException,
			MojoExecutionException {
//		System.out.println("Source = " + sfm.getSourceDirectory());
		sfm.execute();
		Assert.assertTrue(true);
	}

	// test a predefined list of strings
	@Test
	public void testFeatureDirectoryNameToSourceFeatureDirectoryName_static() throws IOException {
		// these should result in a new filename (pattern match succeeds)
		String[] featureDirectoryNames = {"/home/nboldt/tru/jmx/features/org.jboss.tools.jmx.sdk.feature", 
				"/home/nboldt/tru/jmx/features/org.jboss.tools.jmx.feature",
				"/home/nboldt/tru/jmx/features/org.jboss.tools.jmx.tests.feature"};
		for (int i = 0; i < featureDirectoryNames.length; i++) {
			String aFeatureDirectoryName = featureDirectoryNames[i]; 
			String aSourceFeatureDirectoryName = sfm.featureDirectoryName2sourceFeatureDirectoryName(aFeatureDirectoryName);
			Assert.assertNotSame(aFeatureDirectoryName, aSourceFeatureDirectoryName);
		}

		// these should not result in a new filename (pattern match fails)
		String[] featureDirectoryNames2 = {"/home/nboldt/tru/jmx/features/org.jboss.tools.feature.foo", 
				"/home/nboldt/tru/jmx/features/org.jboss.tools.jmx"};
		for (int i = 0; i < featureDirectoryNames2.length; i++) {
			String aFeatureDirectoryName = featureDirectoryNames2[i]; 
			String aSourceFeatureDirectoryName = sfm.featureDirectoryName2sourceFeatureDirectoryName(aFeatureDirectoryName);
			Assert.assertSame(aFeatureDirectoryName, aSourceFeatureDirectoryName);
		}

	}

	// test whatever is passed into via sourceDirectory
	@Test
	public void testFeatureDirectoryNameToSourceFeatureDirectoryName_dynamic() throws IOException {
		List<String> featureParentDirectoryNames = null;
		try {
			featureParentDirectoryNames = sfm.getFeatureParentDirectoryNames();
//			System.out.println("featureParentDirectoryNames = "
//					+ featureParentDirectoryNames.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// foreach dir, get *.feature/ dirs
		for (Iterator <String>i = featureParentDirectoryNames.iterator(); i
				.hasNext();) {
			File aFeatureParentDirectoryName = new File((String)i.next());
			try {
				List featureDirectoryNames = sfm.getFeatureDirectoryNames(aFeatureParentDirectoryName);
//				System.out.println("featureDirectoryNames = "
//						+ featureDirectoryNames.toString());
				
				for (Iterator j = featureDirectoryNames.iterator(); j
						.hasNext();) {
					// // // IF NOT EXIST .source.feature dir:
					String aFeatureDirectoryName = (String) j.next();
					File aSourceFeatureDirectoryName = new File(sfm.featureDirectoryName2sourceFeatureDirectoryName(aFeatureDirectoryName));
					Assert.assertNotSame(aFeatureDirectoryName, aSourceFeatureDirectoryName);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
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

	public void execute() throws MojoExecutionException, MojoFailureException {
	}
}
