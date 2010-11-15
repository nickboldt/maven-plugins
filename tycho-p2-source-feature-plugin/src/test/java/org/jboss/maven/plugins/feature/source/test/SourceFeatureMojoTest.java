package org.jboss.maven.plugins.feature.source.test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.jboss.tycho.plugins.feature.source.SourceFeatureMojo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SourceFeatureMojoTest {

	private File tempDir;

	private File sourceDirectory;

	SourceFeatureMojo sfm;

	String excludes;

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());
		sourceDirectory = new File("/home/nboldt/tru/jmx");
		sourceDirectory.mkdirs();
		sfm = new SourceFeatureMojo();
		excludes = sfm.getExcludes();
		sfm.setSourceDirectory(sourceDirectory);
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir);
	}

	@Test
	public void testFileCreatedSuccessfully() throws IOException,
			MojoExecutionException {
		// System.out.println("Source = " + sfm.getSourceDirectory());
		sfm.execute();
		Assert.assertTrue(true);
	}

	// test a predefined list of strings
	@Test
	public void testFeatureDirectoryNameToSourceFeatureDirectoryName_static()
			throws IOException {
		// these should result in a new filename (pattern match succeeds)
		String[] featureDirectoryNames = {
				"/home/nboldt/tru/jmx/features/org.jboss.tools.jmx.feature",
				"/home/nboldt/tru/jmx/features/org.jboss.tools.jmx.tests.feature" };
		for (int i = 0; i < featureDirectoryNames.length; i++) {
			String aFeatureDirectoryName = featureDirectoryNames[i];
			String aSourceFeatureDirectoryName = sfm
					.featureDirectoryName2sourceFeatureDirectoryName(aFeatureDirectoryName);
			Assert.assertNotSame(aFeatureDirectoryName,
					aSourceFeatureDirectoryName);
		}

		// these should not result in a new filename (pattern match fails)
		String[] featureDirectoryNames2 = {
				"/home/nboldt/tru/jmx/features/org.jboss.tools.feature.foo",
				"/home/nboldt/tru/jmx/features/org.jboss.tools.jmx" };
		for (int i = 0; i < featureDirectoryNames2.length; i++) {
			String aFeatureDirectoryName = featureDirectoryNames2[i];
			String aSourceFeatureDirectoryName = sfm
					.featureDirectoryName2sourceFeatureDirectoryName(aFeatureDirectoryName);
			Assert.assertSame(aFeatureDirectoryName,
					aSourceFeatureDirectoryName);
		}

	}

	// test whatever is passed into via sourceDirectory
	@Test
	public void testFeatureDirectoryNameToSourceFeatureDirectoryName_dynamic()
			throws IOException {
		List<String> featureParentDirectoryNames = null;
		try {
			featureParentDirectoryNames = sfm.getFeatureParentDirectoryNames();
			// System.out.println("featureParentDirectoryNames = "
			// + featureParentDirectoryNames.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// foreach dir, get *.feature/ dirs
		for (Iterator<String> i = featureParentDirectoryNames.iterator(); i
				.hasNext();) {
			File aFeatureParentDirectoryName = new File((String) i.next());
			try {
				List featureDirectoryNames = sfm
						.getFeatureDirectoryNames(aFeatureParentDirectoryName);
				// System.out.println("featureDirectoryNames = "
				// + featureDirectoryNames.toString());

				for (Iterator j = featureDirectoryNames.iterator(); j.hasNext();) {
					// // // IF NOT EXIST .feature.source dir:
					String aFeatureDirectoryName = (String) j.next();
					File aSourceFeatureDirectoryFile = new File(
							sfm.featureDirectoryName2sourceFeatureDirectoryName(
									aFeatureDirectoryName, tempDir));
					Assert.assertNotSame(aFeatureDirectoryName,
							aSourceFeatureDirectoryFile);
				}
			} catch (IOException e) {
				System.out.println("Error creating ");
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testFeatureDirectoryCreated() throws IOException {
		List<String> featureParentDirectoryNames = null;
		try {
			featureParentDirectoryNames = sfm.getFeatureParentDirectoryNames();
			// System.out.println("featureParentDirectoryNames = "
			// + featureParentDirectoryNames.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// foreach dir, get *.feature/ dirs
		for (Iterator<String> i = featureParentDirectoryNames.iterator(); i
				.hasNext();) {
			File aFeatureParentDirectoryName = new File((String) i.next());
			File aSourceFeatureDirectoryFile = null;
			try {
				List featureDirectoryNames = sfm
						.getFeatureDirectoryNames(aFeatureParentDirectoryName);
				for (Iterator j = featureDirectoryNames.iterator(); j.hasNext();) {
					// // // IF NOT EXIST .feature.source dir:
					String aFeatureDirectoryName = (String) j.next();
					aSourceFeatureDirectoryFile = new File(
							sfm.featureDirectoryName2sourceFeatureDirectoryName(
									aFeatureDirectoryName, tempDir));
					Assert.assertNotSame(aFeatureDirectoryName,
							aSourceFeatureDirectoryFile);
				}
			} catch (IOException e) {
				System.out.println("Error creating "
						+ aSourceFeatureDirectoryFile);
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testFeatureDirectoryContentsCopied() throws IOException {
		List<String> featureParentDirectoryNames = null;
		try {
			featureParentDirectoryNames = sfm.getFeatureParentDirectoryNames();
			// System.out.println("featureParentDirectoryNames = "
			// + featureParentDirectoryNames.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// foreach dir, get *.feature/ dirs
		for (Iterator<String> i = featureParentDirectoryNames.iterator(); i
				.hasNext();) {
			File aFeatureParentDirectoryName = new File((String) i.next());
			try {
				List featureDirectoryNames = sfm
						.getFeatureDirectoryNames(aFeatureParentDirectoryName);
				// System.out.println("featureDirectoryNames = "
				// + featureDirectoryNames.toString());

				for (Iterator j = featureDirectoryNames.iterator(); j.hasNext();) {
					// IF NOT EXIST .feature.source dir:
					String aFeatureDirectoryName = (String) j.next();
					File aSourceFeatureDirectoryFile = new File(
							sfm.featureDirectoryName2sourceFeatureDirectoryName(
									aFeatureDirectoryName, tempDir));
					Assert.assertNotSame(aFeatureDirectoryName,
							aSourceFeatureDirectoryFile);
					if (!aSourceFeatureDirectoryFile.isDirectory()) {
						File aFeatureDirectoryFile = new File(
								aFeatureDirectoryName);
						// create matching *.feature.source dir
						aSourceFeatureDirectoryFile.mkdirs();
						FileUtils.copyDirectory(aFeatureDirectoryFile,
								aSourceFeatureDirectoryFile, null, excludes);
						Assert.assertTrue(true);

						// TODO: validate that all the right files have been
						// copied
						/*
						 * List sourceDirectoryNames = FileUtils
						 * .getDirectoryNames(aFeatureDirectoryFile, null,
						 * excludes, true); List targetDirectoryNames =
						 * FileUtils
						 * .getDirectoryNames(aSourceFeatureDirectoryFile, null,
						 * excludes, true); System.out.println("tempDir = " +
						 * tempDir);
						 * System.out.println("sourceDirectoryNames => " +
						 * sourceDirectoryNames);
						 * System.out.println("targetDirectoryNames => " +
						 * targetDirectoryNames); for (Iterator<String> k =
						 * sourceDirectoryNames.iterator(); k .hasNext();) {
						 * String sourceDirectoryName = (String) k.next();
						 * System.out.println(" :: " + sourceDirectoryName); if
						 * (!targetDirectoryNames .contains(tempDir +
						 * sourceDirectoryName)) { Assert.assertTrue(
						 * "Directory " + sourceDirectoryName +
						 * " not found copied to " + aSourceFeatureDirectoryFile
						 * .toString(), false); } }
						 */
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Change artifactId TODO: verify transform works; ensure that container
	 * features/pom.xml is updated too
	 */
	@Test
	public void testTransformPomXML() throws IOException {

		String[] XMLNames = {
				"src/test/resources/org.jboss.tools.jmx.feature.source/pom.xml",
				"src/test/resources/org.jboss.tools.jmx.tests.feature.source/pom.xml" };
		sfm.transformXML("src/main/resources/pomXml2sourcePomXml.xsl",
				XMLNames, false);
		verifyTransformedXML(XMLNames, false);
	}

	/*
	 * xslt to change featureName=foo to featureName=foo Source; xslt to change
	 * <feature id="org.jboss.tools.jmx.feature" to <feature
	 * id="org.jboss.tools.jmx.feature.source", and included source plugins
	 * instead; xslt to remove <requires> section
	 */
	@Test
	public void testTransformFeatureXML() throws IOException {

		String[] XMLNames = {
				"src/test/resources/org.jboss.tools.jmx.feature.source/feature.xml",
				"src/test/resources/org.jboss.tools.jmx.tests.feature.source/feature.xml" };
		sfm.transformXML("src/main/resources/featureXml2sourceFeatureXml.xsl",
				XMLNames, false);
		verifyTransformedXML(XMLNames, false);
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

	public void verifyTransformedXML(String XMLName, boolean replaceFile)
			throws IOException {
		verifyTransformedXML(new String[] { XMLName }, replaceFile);
	}

	public void verifyTransformedXML(String[] XMLNames, boolean replaceFile)
			throws IOException {
		for (int i = 0; i < XMLNames.length; i++) {

			String expectedName = XMLNames[i].replace(".xml",
					".out.expected.xml");
			String actualName = replaceFile ? XMLNames[i]: XMLNames[i].replace(".xml",
					".out.xml");
			String expected = FileUtils.fileRead(expectedName);
			String actual = FileUtils.fileRead(actualName);
			Assert.assertEquals("File " + XMLNames[i]
					+ " did not transform as expected! Compare actual ouptut "
					+ actualName + " with expected output" + expectedName,
					expected, actual);
			// System.out
			// .println("==============================================================");
			// System.out.println(actual);
			// System.out
			// .println("==============================================================");
			// System.out.println(expected);
			// System.out
			// .println("==============================================================");
		}
	}

}
