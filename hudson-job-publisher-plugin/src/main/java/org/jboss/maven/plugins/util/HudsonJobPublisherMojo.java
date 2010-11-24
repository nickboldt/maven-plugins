package org.jboss.maven.plugins.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Given some basic params, publish a Hudson job to a given server
 * 
 * @goal run
 * 
 * @phase validate
 * 
 */
public class HudsonJobPublisherMojo extends AbstractMojo {

	/**
	 * @parameter expression="${verbose}" default-value="false"
	 */
	private boolean verbose = false;

	public boolean getVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * @parameter expression="${hudsonURL}"
	 *            default-value="http://localhost:8080/"
	 */
	private String hudsonURL = "http://localhost:8080/";

	public String getHudsonURL() {
		return hudsonURL;
	}

	public void setHudsonURL(String hudsonURL) {
		this.hudsonURL = hudsonURL;
	}

	/**
	 * @parameter expression="${username}" default-value="admin"
	 */
	private String username = "admin";

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @parameter expression="${password}" default-value="none"
	 */
	private String password = "none";

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @parameter expression="${replaceExistingJob}" default-value="false"
	 */
	private boolean replaceExistingJob = true;

	public boolean isReplaceExistingJob() {
		return replaceExistingJob;
	}

	public void setReplaceExistingJob(boolean replaceExistingJob) {
		this.replaceExistingJob = replaceExistingJob;
	}

	private PostMethod post;

	public void execute() throws MojoExecutionException {
		Log log = getLog();

		// run against a local or remote URL
		setHudsonURL(hudsonURL);
		if (verbose) {
			log.info("Hudson URL: " + hudsonURL);
		}

		String xmlTemplate = "src/main/resources/templates/config.xml";
		String xml = "target/config.xml";

		// String sourcesURL =
		// "http://svn.jboss.org/repos/jbosstools/trunk/bpel";
		// String buildURL =
		// "http://svn.jboss.org/repos/jbosstools/trunk/build";
		// String jobName = "jbosstools-bpel";
		String sourcesURL = "http://svn.jboss.org/repos/jbosstools/branches/jbosstools-3.2.0.Beta2/bpel";
		String buildURL = "http://svn.jboss.org/repos/jbosstools/branches/jbosstools-3.2.0.Beta2/build";
		String jobName = "jbosstools-bpel-stable-branch";

		// replace params above into XML template
		Document dom;
		FileWriter w = null;
		try {
			dom = new SAXReader().read(new File(xmlTemplate));
			Node node;
			node = dom
					.selectSingleNode("/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[1]/remote");
			node.setText(sourcesURL);
			node = dom
					.selectSingleNode("/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[2]/remote");
			node.setText(buildURL);
			// TODO: add destination folder option to publish.sh
			w = new FileWriter(new File(xml));
			dom.write(w);
		} catch (DocumentException e) {
			e.printStackTrace();
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

		String jobURL = hudsonURL + "createItem?name=" + jobName;
		int result = postXML(xml, jobURL);
		if (result == 400) {
			String error = getErrorMessage();
			if (error.indexOf("A job already exists with the name '" + jobName
					+ "'") == 0) {
				if (replaceExistingJob) {
					// post to $hudsonURL/job/$jobName/config.xml
					log.info("Update config.xml for job " + jobName);
					result = postXML(xml, hudsonURL + "/job/" + jobName
							+ "/config.xml");
				} else {
					log.error(error
							+ ". Set replaceExistingJob = true to overwrite existing jobs.");
					throw new MojoExecutionException(error);
				}
			}
		}

		try {
			listJobsOnSecureServer("api/xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getErrorMessage() {
		// scan through the job list and retrieve error message
		Document dom;
		String error = null;
		try {
			dom = new SAXReader().read(post.getResponseBodyAsStream());
			// <p>A job already exists with the name
			// 'jbosstools-bpel'</p>
			// see src/main/resources/400-JobExistsError.html
			for (Element el : (List<Element>) dom
					.selectNodes("/html/body/table[2]/tr/td/h1")) {
				if (el.getTextTrim().equals("Error")) {
					error = el.getParent().selectSingleNode("p").getText();
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			post.releaseConnection();
		}
		return error;
	}

	private int postXML(String xml, String jobURL) {
		Log log = getLog();
		int result = -1;
		File configXMLTemplateFile = new File(xml);
		post = new PostMethod(jobURL);

		try {
			post.setRequestEntity(new InputStreamRequestEntity(
					new FileInputStream(configXMLTemplateFile),
					configXMLTemplateFile.length()));
		} catch (FileNotFoundException e) {
			log.equals("File not found: " + configXMLTemplateFile);
			e.printStackTrace();
		}

		// Specify content type and encoding; default to ISO-8859-1
		post.setRequestHeader("Content-type", "text/xml; charset=ISO-8859-1");

		HttpClient client = getHttpClient(username, password);
		try {
			result = client.executeMethod(post);
			// if (verbose) {
			// log.info("Response status code: " + result);
			// log.info("Response body: ");
			// log.info(post.getResponseBodyAsString());
			// }
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
		return result;
	}

	public void listJobsOnSecureServer(String URLsuffix) throws Exception {
		Log log = getLog();
		HttpClient client = getHttpClient(username, password);

		HttpMethod method = new GetMethod(hudsonURL + URLsuffix);
		client.executeMethod(method);
		checkResult(method.getStatusCode());

		Document dom = new SAXReader().read(method.getResponseBodyAsStream());
		// scan through the job list and print its status
		for (Element job : (List<Element>) dom.getRootElement().elements("job")) {
			log.info(String.format("Job Name: %s\tStatus: %s",
					job.elementText("name"), job.elementText("color")));
		}
	}

	public HttpClient getHttpClient(String username, String password) {
		HttpClient client = new HttpClient();
		// establish a connection within 5 seconds
		client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(5000);
		/* simpler authentication method, may not work w/ secured Hudson */
		// Credentials creds = new UsernamePasswordCredentials("username",
		// "password");
		// if (creds != null) {
		// client.getState().setCredentials(AuthScope.ANY, creds);
		// }

		GetMethod loginLink = new GetMethod(hudsonURL + "loginEntry");
		try {
			client.executeMethod(loginLink);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			checkResult(loginLink.getStatusCode());
		} catch (IOException e) {
			e.printStackTrace();
		}

		String location = hudsonURL + "j_security_check";
		while (true) {
			PostMethod loginMethod = new PostMethod(location);
			loginMethod.addParameter("j_username", username);
			loginMethod.addParameter("j_password", password);
			loginMethod.addParameter("action", "login");
			try {
				client.executeMethod(loginMethod);
			} catch (HttpException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (loginMethod.getStatusCode() / 100 == 3) {
				// Commons HTTP client refuses to handle redirects for POST
				// so we have to do it manually.
				location = loginMethod.getResponseHeader("Location").getValue();
				continue;
			}
			try {
				checkResult(loginMethod.getStatusCode());
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
		return client;
	}

	public void listJobsOnInsecureServer(String URLsuffix) throws Exception {
		Log log = getLog();
		URL url = new URL(hudsonURL + URLsuffix);
		Document dom;
		dom = new SAXReader().read(url);
		// scan through the job list and print its status
		for (Element job : (List<Element>) dom.getRootElement().elements("job")) {
			log.info(String.format("Name:%s\tStatus:%s",
					job.elementText("name"), job.elementText("color")));
		}
	}

	public String getResponseFromURL(String url) {
		Log log = getLog();
		String responseBody = null;
		HttpClient client = getHttpClient(username, password);

		HttpMethod method = null;
		method = new GetMethod(url);
		method.setFollowRedirects(true);
		try {
			client.executeMethod(method);
			responseBody = method.getResponseBodyAsString();
		} catch (HttpException he) {
			log.error("Http error connecting to '" + url + "'");
			log.error(he.getMessage());
			System.exit(-4);
		} catch (IOException ioe) {
			log.error("Unable to connect to '" + url + "'");
			System.exit(-3);
		}
		if (verbose) {
			log.debug("*** Request ***");
			log.debug("Request Path: " + method.getPath());
			log.debug("Request Query: " + method.getQueryString());
			Header[] requestHeaders = method.getRequestHeaders();
			for (int i = 0; i < requestHeaders.length; i++) {
				log.debug(requestHeaders[i].toString());
			}
			log.debug("*** Response ***");
			log.debug("Status Line: " + method.getStatusLine());
			Header[] responseHeaders = method.getResponseHeaders();
			for (int i = 0; i < responseHeaders.length; i++) {
				log.debug(responseHeaders[i].toString());
			}
			log.debug("*** Response Body ***");
			log.debug(responseBody);
		}
		method.releaseConnection();
		return responseBody;
	}

	private static String readFileAsString(String filePath)
			throws java.io.IOException {
		byte[] buffer = new byte[(int) new File(filePath).length()];
		FileInputStream f = new FileInputStream(filePath);
		f.read(buffer);
		return new String(buffer);
	}

	// public void writeDomToFile(File pomFile, Document dom)
	// throws MojoExecutionException {
	// writeDomToFile(pomFile.toString(), dom);
	// }

	// public void writeDomToFile(String pomFile, Document dom)
	// throws MojoExecutionException {
	// FileWriter w = null;
	// try {
	// w = new FileWriter(pomFile);
	// w.write(domToString(dom));
	// } catch (IOException e) {
	// throw new MojoExecutionException("Error updating file " + pomFile,
	// e);
	// } finally {
	// if (w != null) {
	// try {
	// w.close();
	// } catch (IOException e) {
	// // ignore
	// }
	// }
	//
	// }
	// }

	public void writeToFile(File file, String string)
			throws MojoExecutionException {
		FileWriter w = null;
		try {
			w = new FileWriter(file);
			w.write(string);
		} catch (IOException e) {
			throw new MojoExecutionException("Error updating file " + file, e);
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

	// private String domToString(Document dom) {
	// StringBuffer s = new StringBuffer();
	// Node project = dom.selectSingleNode("/project");
	// s.append("<project\n");
	// s.append(nodeAttributesToString(project));
	// s.append(">");
	// s.append(childNodesToString(project));
	// s.append("\n</project>\n");
	// return s.toString();
	// }

	// public String nodeAttributesToString(Node aNode) {
	// StringBuffer s = new StringBuffer();
	// NamedNodeMap nnm = aNode.getAttributes();
	// for (int j = 0; j < nnm.getLength(); j++) {
	// Node n = nnm.item(j);
	// s.append(" " + n.getNodeName() + "=\"" + n.getTextContent() + "\"");
	// }
	// return s.toString();
	// }

	// public String childNodesToString(Node aNode) {
	// final StringBuffer s = new StringBuffer();
	// final NodeList nl = aNode.getChildNodes();
	// for (int j = 0; j < nl.getLength(); j++) {
	// Node n = nl.item(j);
	// if (n.getNodeName() != "#text") {
	// s.append("<" + n.getNodeName() + nodeAttributesToString(n)
	// + ">");
	// if (n.hasChildNodes()) {
	// s.append(childNodesToString(n));
	// } else {
	// s.append(n.getTextContent());
	// }
	// s.append("</" + n.getNodeName() + ">");
	// } else {
	// if (!n.getTextContent().replaceAll("[\n\r ]+", "").equals("")) {
	// s.append(n.getTextContent());
	// }
	// }
	// }
	// return s.toString();
	// }

	private static void checkResult(int i) throws IOException {
		if (i / 100 != 2)
			throw new IOException();
	}

}
