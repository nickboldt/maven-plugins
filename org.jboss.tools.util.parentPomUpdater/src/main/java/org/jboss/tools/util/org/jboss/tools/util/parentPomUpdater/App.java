package org.jboss.tools.util.org.jboss.tools.util.parentPomUpdater;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class App {
	public static void main(String[] args) throws IOException,
			DocumentException {
		if (args.length < 3) {
			System.out
					.println("Usage: updater <dir> <oldParentGAV> <newParentPomGAV>");
			System.out
					.println("Example: updater ~/tru/jmx org.jboss.tools:org.jboss.tools.parent.pom:0.0.1-SNAPSHOT org.jboss.tools:org.jboss.tools.parent.pom:0.0.2-SNAPSHOT");
			System.exit(1);
		}

		// cmdline vars
		File root = new File(args[0].toString());
		System.out.println("Search in " + root);

		String oldParentGAV = args[1].toString();
		String newParentGAV = args[2].toString();
		System.out.println("Source GAV: " + oldParentGAV);
		System.out.println("Target GAV: " + newParentGAV);

		// find poms in the dir
		ArrayList<File> poms = new ArrayList<File>();
		poms.addAll(getChildPoms(root));

		// replace pom versions in the pom.xml files
		String[] oldGAV = oldParentGAV.split(":");
		String[] newGAV = newParentGAV.split(":");
		for (Iterator i = poms.iterator(); i.hasNext();) {
			File pom = (File) i.next();
			System.out.println("Check for parent GAV in " + pom.toString());
			replacePomVersion(pom, oldGAV, newGAV);
		}
	}

	private static void replacePomVersion(File pom, String[] oldGAV,
			String[] newGAV) {
		// open file & get dom
		Document dom = null;
		try {
			dom = new SAXReader().read(pom);
		} catch (DocumentException e) {
			e.printStackTrace();
		}

		// fix dom, if there's a parent node
		if (dom != null && dom.asXML().toString().indexOf("<parent>")>=0) {
			Element node = dom.getRootElement().element("parent");
			if (node != null && dom.asXML().toString().indexOf(oldGAV[0])>=0 && dom.asXML().toString().indexOf(oldGAV[1])>=0) {
				System.out.println("         Replace GAV in "
						+ pom.toString());
				for (Element elem : (List<Element>) node.elements()) {
					Element oldElem = elem;
					if (elem.getName().equals("groupId")) {
						elem = replaceNodeText(elem, oldGAV[0], newGAV[0]);
					} else if (elem.getName().equals("artifactId")) {
						elem = replaceNodeText(elem, oldGAV[1], newGAV[1]);
					} else if (elem.getName().equals("version")) {
						elem = replaceNodeText(elem, oldGAV[2], newGAV[2]);
					}
				}
				// write file
				writeDomToFile(dom, pom);
			}
		}
	}

	public static Element replaceNodeText(Element node, String oldText,
			String newText) {
		// System.out.println(" :: " + node.getText());
		if (node != null && node.getText().equals(oldText)) {
			node.setText(newText);
		}
		return node;
	}

	public static ArrayList<File> getChildPoms(File dir) throws IOException {
		ArrayList<File> poms = new ArrayList<File>();
		List children = FileUtils.getFileAndDirectoryNames(dir, "*",
				".svn, .git, CVS, *~, target, bin, classes", true, true, true,
				true);
		for (Iterator<Object> i = children.iterator(); i.hasNext();) {
			File child = new File((String) i.next());
			if (child.isFile() && child.toString().endsWith("pom.xml")) {
				poms.add(child);
				// System.out.println("F: " + child);
			} else if (child.isDirectory()) {
				poms.addAll(getChildPoms(child));
				// System.out.println("D: " + child);
			}
		}
		return poms;
	}

	public static void writeDomToFile(Document dom, File file) {
		FileWriter w = null;
		if (dom != null) {
			try {
				w = new FileWriter(file);
				dom.write(w);
			} catch (IOException e) {
				e.printStackTrace();
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
	}
}
