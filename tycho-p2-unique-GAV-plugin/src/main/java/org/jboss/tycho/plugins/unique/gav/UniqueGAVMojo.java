package org.jboss.tycho.plugins.unique.gav;

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
import org.codehaus.plexus.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Generate a source feature from every other feature found in the tree
 * 
 * @goal
 * 
 * @phase process-sources
 */
public class UniqueGAVMojo extends AbstractMojo {

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

	private boolean doInfo = false;

	public void setDoInfo(boolean doInfo) {
		this.doInfo = doInfo;
	}

	private boolean doWarn = false;

	public void setDoWarn(boolean doWarn) {
		this.doWarn = doWarn;
	}

	private boolean doError = true;

	public void setDoError(boolean doError) {
		this.doError = doError;
	}

	public void execute() throws MojoExecutionException {
		Log log = getLog();

		// run somewhere in maven build tree, sourceDirectory
		setSourceDirectory(sourceDirectory);

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
					for (int j = 0; j < nl.getLength(); j++) {
						Node n = nl.item(j);
						if (n.getNodeName() == "groupId") {
							groupId = n.getTextContent();
							groupIdNode = n;
						} else if (n.getNodeName() == "artifactId") {
							artifactId = n.getTextContent();
						} else if (n.getNodeName() == "version") {
							version = n.getTextContent();
						} else if (n.getNodeName() == "name") {
							name = n.getTextContent();
						}
					}

					// check for best practices
					if (artifactId.indexOf(groupId) < 0) {
						if (doWarn) {
							log.warn(pom + "\n       ArtifactId (" + artifactId
									+ ") should contain prefix of " + groupId
									+ "\n       or groupId (" + groupId
									+ ") should be shortened.");
							warnings++;
						}
					}

					// if (name == "") {
					// if (doWarn) {
					// log.warn(pom + "\n       Name is not set.");
					// warnings++;
					// }
					// } else if (name != ""
					// && (name.indexOf(artifactId) < 0 || artifactId
					// .indexOf(name) < 0)) {
					// if (doInfo) {
					// log.info(pom + "\n       ArtifactId = "
					// + artifactId + ", but name = " + name
					// + "; should be the same");
					// infos++;
					// }
					// }

					// remove .plugins or .features from groupId
					if (groupId.endsWith(".plugins")
							|| groupId.endsWith(".features")) {
						if (groupId.endsWith(".plugins")) {
							groupIdNode.setTextContent(groupId.replace(
									".plugins", ""));
						}
						if (groupId.endsWith(".features")) {
							groupIdNode.setTextContent(groupId.replace(
									".features", ""));
						}
						writeDomToFile(pomFile.toString(), dom);
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
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}
			}

			if (doError) {
				System.out.println("Found " + errors + " errors.");
			}
			if (doWarn) {
				System.out.println("Found " + warnings + " warnings.");
			}
			if (doInfo) {
				System.out.println("Found " + infos + " infos.");
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
