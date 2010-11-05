package org.jboss.tycho.plugins.source.feature;

import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;

// TODO: find a better logger (use the Tycho one?)
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Generate a source feature from every other feature found in the tree
 * 
 * @goal
 * 
 * @phase process-sources
 */
public class SourceFeatureMojo extends AbstractMojo {
	private File sourceDirectory;

	private Log logger;

	public File getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public void execute() throws MojoExecutionException {
		// run somewhere in maven build tree, sourceDirectory
		setSourceDirectory(sourceDirectory);
		List<String> featureParentDirectoryNames = null;
		try {
			featureParentDirectoryNames = getFeatureParentDirectoryNames();
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
				List featureDirectoryNames = getFeatureDirectoryNames(aFeatureParentDirectoryName);
//				System.out.println("featureDirectoryNames = "
//						+ featureDirectoryNames.toString());
				
				for (Iterator j = featureDirectoryNames.iterator(); j
						.hasNext();) {
					// // // IF NOT EXIST .source.feature dir:
					String aFeatureDirectoryName = (String) j.next();
					File aSourceFeatureDirectoryName = new File(featureDirectoryName2sourceFeatureDirectoryName(aFeatureDirectoryName));
					if (!aSourceFeatureDirectoryName.isDirectory())
					{
						System.out.println("Create new source feature in " + aSourceFeatureDirectoryName);
						// // // create matching *.source.feature dir
						// // // xslt to change featureName=foo to featureName=foo Source
						// // // xslt to change <feature id="org.jboss.tools.jmx.feature" to
						// // // <feature id="org.jboss.tools.jmx.source.feature", and included
						// // // plugins add .source into them too
						// // // xslt to remove <requires> section
						// // // add new feature into feature/ aggregator pom.xml
					}
					else
					{
						// skip folder because it already exists
						System.out.println("Source feature already exists in " + aSourceFeatureDirectoryName);
					}
					
				}
				


			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		/*
		 * if (sourceDirectory != null) { File f = sourceDirectory;
		 * 
		 * if (!f.exists()) { f.mkdirs(); }
		 * 
		 * File touch = new File(f, "feature.xml");
		 * 
		 * FileWriter w = null; try { w = new FileWriter(touch);
		 * 
		 * w.write("<feature/>"); } catch (IOException e) { throw new
		 * MojoExecutionException( "Error creating file " + touch, e); } finally
		 * { if (w != null) { try { w.close(); } catch (IOException e) { //
		 * ignore } } } } else { throw new
		 * MojoExecutionException("outputDirectory is null"); }
		 */
	}

	public String featureDirectoryName2sourceFeatureDirectoryName(
			String aFeatureDirectoryName) {
		return aFeatureDirectoryName.replaceAll("\\.feature$", ".source.feature");
	}

	public List getFeatureDirectoryNames(File aFeatureParentDirectoryName)
			throws IOException {
		List directoryNames2 = FileUtils.getDirectoryNames(
				aFeatureParentDirectoryName, "**/*.feature", null, true);
		List<String> featureDirectoryNames = directoryNames2;
		return directoryNames2;
	}

	public List<String> getFeatureParentDirectoryNames() throws IOException {
		List directoryNames = FileUtils.getDirectoryNames(sourceDirectory,
				"features", null, true);
		List<String> featureParentDirectoryNames = directoryNames;
		return featureParentDirectoryNames;
	}
}
