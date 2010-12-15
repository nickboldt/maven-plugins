package org.jboss.maven.plugins.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

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
	 * @parameter expression="${replaceExistingJob}" default-value="true"
	 */
	private boolean replaceExistingJob = true;

	public boolean isReplaceExistingJob() {
		return replaceExistingJob;
	}

	public void setReplaceExistingJob(boolean replaceExistingJob) {
		this.replaceExistingJob = replaceExistingJob;
	}

	/**
	 * @parameter expression="${jobTemplateFile}" default-value="config.xml"
	 */
	private String jobTemplateFile = "config.xml";

	public String getJobTemplateFile() {
		return jobTemplateFile;
	}

	public void setJobTemplateFile(String jobTemplateFile) {
		this.jobTemplateFile = jobTemplateFile;
	}

	/**
	 * @parameter expression="${buildURL}"
	 *            default-value="http://svn.jboss.org/repos/jbosstools/trunk/build"
	 */
	private String buildURL = "http://svn.jboss.org/repos/jbosstools/trunk/build";

	public String getBuildURL() {
		return buildURL;
	}

	public void setBuildURL(String buildURL) {
		this.buildURL = buildURL;
	}

	/**
	 * @parameter expression="${properties}"
	 */
	private Properties jobProperties = new Properties();

	public Properties getJobProperties() {
		return jobProperties;
	}

	public void setJobProperties(Properties jobProperties) {
		this.jobProperties = jobProperties;
	}

	/**
	 * @parameter expression="${components}" default-value=""
	 */
	private String components = "";

	public String getComponents() {
		return components;
	}

	public void setComponents(String components) {
		this.components = components;
	}

	/**
	 * @parameter expression="${componentJobNameSuffix}" default-value=""
	 */
	private String componentJobNameSuffix = "";

	public String getComponentJobNameSuffix() {
		return componentJobNameSuffix;
	}

	public void setComponentJobNameSuffix(String componentJobNameSuffix) {
		this.componentJobNameSuffix = componentJobNameSuffix;
	}

	private static final String JOB_ALREADY_EXISTS = "A job already exists with the name ";
	public static final String JOB_NAME = "Job Name: ";
	public static final String JBOSSTOOLS_JOBNAME_PREFIX = "jbosstools-";

	public void execute() throws MojoExecutionException {
		Log log = getLog();

		// run against a local or remote URL
		setHudsonURL(hudsonURL);
		if (verbose) {
			log.info("Hudson URL: " + hudsonURL);
		}

		String xml = jobTemplateFile; // "target/config.xml";

		if (components != null && !components.isEmpty()) {
			String[] componentArray = components.split("[, ]+");
			// System.out.println(componentArray.length + " : " +
			// componentArray);
			for (int i = 0; i < componentArray.length; i++) {
				// add new jobName to sourcesURL mappings
				// System.out.println(componentArray[i] + ", " +
				// componentJobNameSuffix + ", " +
				// buildURL.replaceAll("/build/*$", "/"));
				jobProperties.put(JBOSSTOOLS_JOBNAME_PREFIX + componentArray[i]
						+ componentJobNameSuffix,
						buildURL.replaceAll("/build/*$", "/")
								+ componentArray[i]);
				// System.out.println("Got: " + jobProperties.get("jbosstools-"
				// + componentArray[i] + componentJobNameSuffix));
			}
		}

		// load jobName::sourcesURL mapping from jobProperties
		Enumeration jobNames = jobProperties.propertyNames();
		while (jobNames.hasMoreElements()) {
			String jobName = (String) jobNames.nextElement();
			String sourcesURL = jobProperties.getProperty(jobName);

			updateConfigXML(sourcesURL, jobTemplateFile, xml);

			// delete existing job
			if (replaceExistingJob) {
				deleteJob(xml, jobName, false);
			}
			createOrUpdateJob(xml, jobName);
		}
		if (verbose) {
			try {
				log.info(listJobsOnSecureServer());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void createOrUpdateJob(String xml, String jobName)
			throws MojoExecutionException {
		String[] result = createJob(xml, jobName, !replaceExistingJob);
		// System.out.println(result[0] + "\n" + result[1]);
		if (Integer.parseInt(result[0].trim()) == 400) {
			String error = result[1];
			if (error.indexOf(">" + JOB_ALREADY_EXISTS + "'" + jobName + "'<") != 0) {
				if (replaceExistingJob) {
					updateJob(xml, jobName, false);
				} else {
					getLog().error(
							JOB_ALREADY_EXISTS
									+ "'"
									+ jobName
									+ "'. Set replaceExistingJob = true to overwrite existing jobs.");
					throw new MojoExecutionException(error);
				}
			}
		}
	}

	public void updateConfigXML(String sourcesURL, String xmlTemplate,
			String xml) {
		// replace params above into XML template
		Document dom = null;
		FileWriter w = null;
		try {
			dom = new SAXReader().read(new File(xmlTemplate));
			dom.selectSingleNode(
					"/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[1]/remote")
					.setText(sourcesURL);
			dom.selectSingleNode(
					"/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[2]/remote")
					.setText(buildURL);
			// TODO: add destination folder option to publish.sh
		} catch (DocumentException e) {
			getLog().error("Problem reading XML from " + xmlTemplate);
			e.printStackTrace();
		}

		if (dom != null) {
			try {
				w = new FileWriter(new File(xml));
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

	public Object[] updateJob(String xml, String jobName,
			boolean getErrorMessage) {
		if (verbose)
			getLog().info("Update config.xml for job " + jobName);
		return postXML(xml, hudsonURL + "/job/" + jobName + "/config.xml",
				getErrorMessage);
	}

	public String[] createJob(String xml, String jobName,
			boolean getErrorMessage) {
		if (verbose)
			getLog().info("Create job " + jobName);
		return postXML(xml, hudsonURL + "createItem?name=" + jobName,
				getErrorMessage);
	}

	public String[] deleteJob(String xml, String jobName,
			boolean getErrorMessage) {
		if (verbose)
			getLog().info("Delete job " + jobName);
		return postXML(xml, hudsonURL + "job/" + jobName + "/doDelete",
				getErrorMessage);
	}

	private String getErrorMessage(PostMethod post, String jobName) {
		Log log = getLog();
		// scan through the job list and retrieve error message
		Document dom;
		String error = null;
		try {
			// File tempfile = File.createTempFile("getErrorMessage", "");
			// writeToFile(tempfile, message);
			InputStream is = post.getResponseBodyAsStream();
			dom = new SAXReader().read(is);
			// <p>A job already exists with the name
			// 'jbosstools-bpel'</p>
			// see src/main/resources/400-JobExistsError.html
			for (Element el : (List<Element>) dom
					.selectNodes("/html/body/table[2]/tr/td/h1")) {
				if (el.getTextTrim().equals("Error")) {
					for (Element el2 : (List<Element>) el.getParent()
							.selectNodes("p")) {
						error = el2.getText();
					}
				}
			}
		} catch (DocumentException e) {
			log.error("Error reading from " + jobName);
			// e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			// } catch (MojoExecutionException e) {
			// e.printStackTrace();
		}
		return error;
	}

	private String[] postXML(String xml, String jobURL, boolean getErrorMessage) {
		Log log = getLog();
		int resultCode = -1;
		String resultString = "";
		File configXMLTemplateFile = new File(xml);
		PostMethod post = new PostMethod(jobURL);

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
			resultCode = client.executeMethod(post);
			if (getErrorMessage) {
				resultString = post.getResponseBodyAsString();
				// resultString = getErrorMessage(post, jobURL);
			}
			// if (verbose) {
			// log.info("Response status code: " + resultCode);
			// log.info("Response body: ");
			// log.info(resultString);
			// }
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			post.releaseConnection();
		}
		if (getErrorMessage) {
			return new String[] { resultCode + "", resultString };
		} else {
			return new String[] { resultCode + "", "" };
		}
	}

	public String listJobsOnSecureServer() throws Exception {
		Log log = getLog();
		HttpClient client = getHttpClient(username, password);

		HttpMethod method = new GetMethod(hudsonURL + "api/xml");
		client.executeMethod(method);
		checkResult(method.getStatusCode());

		StringBuilder sb = new StringBuilder();
		Document dom = new SAXReader().read(method.getResponseBodyAsStream());
		// scan through the job list and print its status
		for (Element job : (List<Element>) dom.getRootElement().elements("job")) {
			if (job.elementText("name").toString()
					.indexOf(JBOSSTOOLS_JOBNAME_PREFIX) == 0) {
				sb.append(String.format(JOB_NAME + "%s\tStatus: %s",
						job.elementText("name"), job.elementText("color"))
						+ "\n");
			}
		}
		return sb.toString();
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

	public void listJobsOnInsecureServer() throws Exception {
		Log log = getLog();
		URL url = new URL(hudsonURL + "api/xml");
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
