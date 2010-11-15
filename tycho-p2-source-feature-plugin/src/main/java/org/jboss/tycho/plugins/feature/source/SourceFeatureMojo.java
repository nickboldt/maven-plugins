package org.jboss.tycho.plugins.feature.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Generate a source feature from every other feature found in the tree
 * 
 * @goal
 * 
 * @phase process-sources
 */
public class SourceFeatureMojo extends AbstractMojo {

	private File sourceDirectory;

	public File getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	private String excludes = "*.jar, **/*.jar, target, target/**, rootfiles, rootfiles/**, sourceTemplatePlugin, sourceTemplatePlugin/**, sourceTemplateFeature, sourceTemplateFeature/**, *.svn-base, **/*.svn-base, **/**/*.svn-base, .svn, .svn/**, .svn/**/**, CVS, CVS/**, CVS/**/**";

	public String getExcludes() {
		return excludes;
	}

	public void setExcludes(String excludes) {
		this.excludes = excludes;
	}

	public void execute() throws MojoExecutionException {
		// run somewhere in maven build tree, sourceDirectory
		setSourceDirectory(sourceDirectory);
		List<String> featureParentDirectoryNames = null;
		try {
			featureParentDirectoryNames = getFeatureParentDirectoryNames();
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
				List featureDirectoryNames = getFeatureDirectoryNames(aFeatureParentDirectoryName);
				// System.out.println("featureDirectoryNames = "
				// + featureDirectoryNames.toString());

				for (Iterator j = featureDirectoryNames.iterator(); j.hasNext();) {
					// // // IF NOT EXIST .feature.source dir:
					String aFeatureDirectoryName = (String) j.next();
					File aSourceFeatureDirectoryFile = new File(
							featureDirectoryName2sourceFeatureDirectoryName(aFeatureDirectoryName));
					if (!aSourceFeatureDirectoryFile.isDirectory()) {
						// System.out.println("Create new source feature in " +
						// aSourceFeatureDirectoryFile);
						// // // create matching *.feature.source dir
						aSourceFeatureDirectoryFile.mkdirs();
						FileUtils.copyDirectory(
								new File(aFeatureDirectoryName),
								aSourceFeatureDirectoryFile, null, excludes);

						// TODO: set correct input/output paths here
						transformXML(
								"src/main/resources/featureXml2sourceFeatureXml.xsl",
								aSourceFeatureDirectoryFile.toString()
										+ File.separator + "feature.xml", true);

						// Change artifactId
						// TODO: verify transform works; ensure that container
						// features/pom.xml is updated too
						transformXML(
								"src/main/resources/pomXml2sourcePomXml.xsl",
								aSourceFeatureDirectoryFile.toString()
										+ File.separator + "pom.xml", true);

						// // // xslt to change featureName=foo to
						// featureName=foo Source
						// // // xslt to change <feature
						// id="org.jboss.tools.jmx.feature" to
						// // // <feature
						// id="org.jboss.tools.jmx.feature.source", and included
						// // // plugins add .source into them too
						// // // xslt to remove <requires> section
						// TraXLiaison tl = new TraXLiaison();
						// tl.setStylesheet()

						/*
						 * File settings = new File( "settings.xml" ); Document
						 * dom = DocumentBuilderFactory.newInstance()
						 * .newDocumentBuilder().parse(settings); Transformer
						 * xslTransformer = TransformerFactory
						 * .newInstance().newTransformer();
						 * xslTransformer.transform( new
						 * DOMSource(dom.getDocumentElement()), new
						 * StreamResult(settings));
						 */

						// // // add new feature into feature/ aggregator
						// pom.xml
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
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
		return featureDirectoryName2sourceFeatureDirectoryName(
				aFeatureDirectoryName, null);
	}

	public String featureDirectoryName2sourceFeatureDirectoryName(
			String aFeatureDirectoryName, File targetDir) {
		if (aFeatureDirectoryName.indexOf(".feature.source") < 0) {
			if (targetDir != null && targetDir.isDirectory()) {
				// System.out.println(targetDir +
				// aFeatureDirectoryName.replaceAll("\\.feature$",
				// ".feature.source"));
				return targetDir
						+ aFeatureDirectoryName.replaceAll("\\.feature$",
								".feature.source");
			} else {
				// System.out.println(aFeatureDirectoryName.replaceAll("\\.feature$",
				// ".feature.source"));
				return aFeatureDirectoryName.replaceAll("\\.feature$",
						".feature.source");
			}
		} else {
			return aFeatureDirectoryName;
		}
	}

	public List getFeatureDirectoryNames(File aFeatureParentDirectoryName)
			throws IOException {
		List directoryNames2 = FileUtils.getDirectoryNames(
				aFeatureParentDirectoryName, "**/*.feature", excludes
						+ ", **/*.source*, **/*.sdk.*", true);
		return directoryNames2;
	}

	public List<String> getFeatureParentDirectoryNames() throws IOException {
		List directoryNames = FileUtils.getDirectoryNames(sourceDirectory,
				"features", excludes, true);
		List<String> featureParentDirectoryNames = directoryNames;
		return featureParentDirectoryNames;
	}

	public void transformXML(String XSLName, String XMLName, boolean replaceFile) {
		transformXML(XSLName, new String[] { XMLName }, replaceFile);
	}

	public void transformXML(String XSLName, String[] XMLNames,
			boolean replaceFile) {
		Document xmlFile = null;
		for (int i = 0; i < XMLNames.length; i++) {
			File featureXMLFile = new File(XMLNames[i]);
			if (featureXMLFile.exists()) {
				try {
					xmlFile = DocumentBuilderFactory.newInstance()
							.newDocumentBuilder().parse(featureXMLFile);

					Transformer xslTransformer = TransformerFactory
							.newInstance().newTransformer(
									new SAXSource(new InputSource(
											new FileInputStream(XSLName))));
					StreamResult sr = new StreamResult(featureXMLFile);
					sr.setOutputStream(new FileOutputStream(
							replaceFile ? XMLNames[i] : XMLNames[i].replace(
									".xml", ".out.xml")));
					xslTransformer.transform(
							new DOMSource(xmlFile.getDocumentElement()), sr);
				} catch (TransformerFactoryConfigurationError e) {
					e.printStackTrace();
				} catch (TransformerConfigurationException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (TransformerException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("File " + featureXMLFile
						+ " must exist in order to be transformed.");
			}
		}
	}
}
