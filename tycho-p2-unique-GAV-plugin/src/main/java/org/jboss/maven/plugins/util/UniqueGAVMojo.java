package org.jboss.maven.plugins.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.tools.ant.taskdefs.condition.IsFailure;
import org.codehaus.plexus.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Check for non-unique GAVs and apply best practices to G:A:V naming
 * 
 * @goal run
 * 
 * @phase validate
 * 
 */
public class UniqueGAVMojo extends AbstractMojo {

	/**
	 * @parameter expression="${doWarn}" default-value="false"
	 */
	private boolean verbose = false;

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * @parameter expression="${sourceDirectory}" default-value="."
	 */
	private File sourceDirectory;

	public File getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(File sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	private Hashtable<String, String> GAV = new Hashtable<String, String>();

	public Hashtable<String, String> getGAV() {
		return GAV;
	}

	/**
	 * @parameter expression="${doInfo}" default-value="false"
	 */
	private boolean doInfo = false;

	public void setDoInfo(boolean doInfo) {
		this.doInfo = doInfo;
	}

	/**
	 * @parameter expression="${doWarn}" default-value="false"
	 */
	private boolean doWarn = false;

	public void setDoWarn(boolean doWarn) {
		this.doWarn = doWarn;
	}

	/**
	 * @parameter expression="${doError}" default-value="true"
	 */
	private boolean doError = true;

	public void setDoError(boolean doError) {
		this.doError = doError;
	}

	public void execute() throws MojoExecutionException {
		Log log = getLog();

		// run somewhere in maven build tree, sourceDirectory
		setSourceDirectory(sourceDirectory);
		if (verbose) {
			log.info("Checking GAVs in " + sourceDirectory);
		}

		/*
		 * find all pom.xml files in tree load pom.xml files and store filename
		 * and G:A:V strings in hash if new; if find a non-unique one, store
		 * filename in a second hash dump results of non-unique hash
		 */

		int errors = 0;
		int warnings = 0;
		int infos = 0;
		List<String> poms = null;
		try {
			poms = findPOMs();
			for (Iterator i = poms.iterator(); i.hasNext();) {
				String pom = (String) i.next();

				/*
				 * <project> <groupId>org.jboss.tools</groupId>
				 * <artifactId>jmx</artifactId>
				 * <version>0.0.1-SNAPSHOT</version> <name>jmx.all</name>
				 */
				// System.out.println(pom);
				File pomFile = new File(pom);
				Document dom;
				try {
					dom = DocumentBuilderFactory.newInstance()
							.newDocumentBuilder().parse(pomFile);
					Node project = dom.getElementsByTagName("project").item(0);
					// System.out.println("Project node: " +
					// project.toString());
					NodeList nl = project.getChildNodes();
					String groupId = "";
					String artifactId = "";
					String version = "";
					String name = "";
					Node groupIdNode = null;
					Node artifactIdNode = null;
					for (int j = 0; j < nl.getLength(); j++) {
						Node n = nl.item(j);
						if (n.getNodeName() == "groupId") {
							groupId = n.getTextContent();
							groupIdNode = n;
						} else if (n.getNodeName() == "artifactId") {
							artifactId = n.getTextContent();
							artifactIdNode = n;
						} else if (n.getNodeName() == "version") {
							version = n.getTextContent();
						} else if (n.getNodeName() == "name") {
							name = n.getTextContent();
						}
					}

					boolean writeToFile = false;
					// remove .plugins or .features from groupId
					if (groupId.endsWith(".plugins")
							|| groupId.endsWith(".features")
							|| groupId.endsWith(".tests")) {
						if (groupId.endsWith(".plugins")) {
							groupId = groupId.replace(".plugins", "");
							groupIdNode.setTextContent(groupId);
						}
						if (groupId.endsWith(".features")) {
							groupId = groupId.replace(".features", "");
							groupIdNode.setTextContent(groupId);
						}
						if (groupId.endsWith(".tests")) {
							groupId = groupId.replace(".tests", "");
							groupIdNode.setTextContent(groupId);
						}
						writeToFile = true;
					}
					String thisFolder = pom.replaceFirst(
							".+/([^/]+)/pom\\.xml", "$1");
					String parentFolder = pom.replaceFirst(
							".+/([^/]+)/([^/]+)/pom\\.xml", "$1");
					// if (doInfo) {
					// log.info("In folder: " + parentFolder + "/"
					// + thisFolder);
					// }
					if (artifactId.equals("features")) {
						artifactId = groupId + ".features";
						artifactIdNode.setTextContent(artifactId);
						writeToFile = true;
					} else if (artifactId.equals(parentFolder + ".features")) {
						artifactId = groupId + "." + parentFolder + ".features";
						artifactIdNode.setTextContent(artifactId);
						writeToFile = true;
					} else if (artifactId.equals("plugins")) {
						artifactId = groupId + ".plugins";
						artifactIdNode.setTextContent(artifactId);
						writeToFile = true;
					} else if (artifactId.equals(parentFolder + ".plugins")) {
						artifactId = groupId + "." + parentFolder + ".plugins";
						artifactIdNode.setTextContent(artifactId);
						writeToFile = true;
					} else if (artifactId.equals("tests")) {
						artifactId = groupId + ".tests";
						artifactIdNode.setTextContent(artifactId);
						writeToFile = true;
					} else if (artifactId.equals(parentFolder + "tests")) {
						artifactId = groupId + "." + parentFolder + ".tests";
						artifactIdNode.setTextContent(artifactId);
						writeToFile = true;
					} else if (artifactId.equals("site")) {
						artifactId = groupId + ".site";
						artifactIdNode.setTextContent(artifactId);
						writeToFile = true;
					} else if (artifactId.equals(parentFolder + ".site")) {
						artifactId = groupId + "." + parentFolder + ".site";
						artifactIdNode.setTextContent(artifactId);
						writeToFile = true;
					}

					if (writeToFile) {
						writeDomToFile(pomFile.toString(), dom);
					}

					// check for best practices
					if (doWarn) {
						if (artifactId.indexOf(groupId) < 0) {
							log.warn(pom + "\n       ArtifactId (" + artifactId
									+ ") should contain prefix of " + groupId
									+ "\n       or groupId (" + groupId
									+ ") should be shortened.");
							warnings++;
						}
					}

					if (name == "") {
						if (doWarn) {
							log.info(pom + "\n       Name is not set.");
							warnings++;
						}
					} else if (name != ""
							&& (name.indexOf(artifactId) < 0 || artifactId
									.indexOf(name) < 0)) {
						if (doInfo) {
							log.info(pom + "\n       ArtifactId = "
									+ artifactId + ", but name = " + name
									+ "; should be the same");
							infos++;
						}
					}

					// check for duplicates
					String GAVkey = groupId + ":" + artifactId + ":" + version;
					if (!GAV.containsKey(GAVkey)) {
						GAV.put(GAVkey, pom);
					} else {
						if (doError) {
							log.error(pom
									+ "\n  and   "
									+ GAV.get(GAVkey)
									+ "\n  ===   Identical GAVs: GAVs must be unique");
							errors++;
						}
					}

				} catch (SAXException e) {
					System.out.println("Error parsing pom file " + pom);
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			}

			if (doError && (errors > 0 || verbose)) {
				log.info("Found " + errors + " errors.");
			}
			if (doWarn && (warnings > 0 || verbose)) {
				log.info("Found " + warnings + " warnings.");
			}
			if (doInfo && (infos > 0 || verbose)) {
				log.info("Found " + infos + " infos.");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writeDomToFile(File pomFile, Document dom)
			throws MojoExecutionException {
		writeDomToFile(pomFile.toString(), dom);
	}

	public void writeDomToFile(String pomFile, Document dom)
			throws MojoExecutionException {
		FileWriter w = null;
		try {
			w = new FileWriter(pomFile);
			w.write(domToString(dom));
		} catch (IOException e) {
			throw new MojoExecutionException("Error updating file " + pomFile,
					e);
		} finally {
			if (w != null) {
				try {
					w.close();
				} catch (IOException e) {
					// ignore
				}
			}

		}
	}

	private String domToString(Document dom) {
		StringBuffer s = new StringBuffer();
		Node project = dom.getElementsByTagName("project").item(0);
		s.append("<project\n");
		s.append(nodeAttributesToString(project));
		s.append(">");
		s.append(childNodesToString(project));
		s.append("\n</project>\n");
		return s.toString();
	}

	public String nodeAttributesToString(Node aNode) {
		StringBuffer s = new StringBuffer();
		NamedNodeMap nnm = aNode.getAttributes();
		for (int j = 0; j < nnm.getLength(); j++) {
			Node n = nnm.item(j);
			s.append(" " + n.getNodeName() + "=\"" + n.getTextContent() + "\"");
		}
		return s.toString();
	}

	public String childNodesToString(Node aNode) {
		final StringBuffer s = new StringBuffer();
		final NodeList nl = aNode.getChildNodes();
		for (int j = 0; j < nl.getLength(); j++) {
			Node n = nl.item(j);
			if (n.getNodeName() != "#text") {
				s.append("<" + n.getNodeName() + nodeAttributesToString(n)
						+ ">");
				if (n.hasChildNodes()) {
					s.append(childNodesToString(n));
				} else {
					s.append(n.getTextContent());
				}
				s.append("</" + n.getNodeName() + ">");
			} else {
				if (!n.getTextContent().replaceAll("[\n\r ]+", "").equals("")) {
					s.append(n.getTextContent());
				}
			}
		}

		return s.toString();
	}

	public List<String> findPOMs() throws IOException {
		List poms = FileUtils.getFileNames(sourceDirectory, "**/pom.xml",
				"**/target/**", true);
		List<String> pomList = poms;
		return pomList;
	}

}
