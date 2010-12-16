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
import org.dom4j.io.SAXReader;

/**
 * Given some basic params, publish a Hudson job to a given server
 * 
 * @goal run
 * 
 * @phase validate
 * 
 */
public class HudsonJobScheduleCheckerMojo extends AbstractMojo {

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

	// TODO: set as parameter
	public String JOB_URL_SUFFIX =
	// "view/DevStudio_Trunk/";
	// "view/DevStudio_Stable_Branch/";
	"view/SAVARA/";

	// TODO: set as parameter
	public String JOBNAME_PATTERN =
	// "jbosstools-3.2_trunk.component--";
	"jbosstools-.+_trunk.*|devstudio-.+_trunk.*";

	private static final String INDENT = "   ";
	public static final String JOB = "Job: ";

	public void execute() throws MojoExecutionException {
		// run against a local or remote URL
		setHudsonURL(hudsonURL);
		if (verbose) {
			getLog().info("Hudson URL: " + hudsonURL);
		}

		try {
			getLog().info(
					listJobsOnSecureServer(hudsonURL + JOB_URL_SUFFIX
							+ "api/xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// dom.selectSingleNode("/project/scm/locations/hudson.scm.SubversionSCM_-ModuleLocation[1]/remote")
	public String listJobsOnSecureServer(String url) throws Exception {
		HttpClient client = getHttpClient(username, password);

		HttpMethod method = new GetMethod(url);
		client.executeMethod(method);
		checkResult(method.getStatusCode());

		StringBuilder sb = new StringBuilder("\n");

		if (verbose) {
			getLog().info("Jobs URL: " + url);
		}
		Document dom = new SAXReader().read(method.getResponseBodyAsStream());
		// scan through the job list and print its status
		int i = 0;
		for (Element job : (List<Element>) dom.getRootElement().elements("job")) {
			if (!job.elementText("name").toString()
					.replaceAll(JOBNAME_PATTERN, "")
					.equals(job.elementText("name").toString())) {
				i++;
				sb.append(String.format("\n[%03d] " + JOB + "%s (%s)", i,
						job.elementText("url"), job.elementText("color"))
						+ "\n");
				sb.append("\n");
				sb.append(getJobConfig(client, job.elementText("name")
						.toString()));
				sb.append("\n");
				sb.append(getJobDetail(client, job.elementText("name")
						.toString()));
				sb.append("-----\n");
			}
		}
		return sb.toString();
	}

	public String getJobConfig(HttpClient client, String name) throws Exception {
		StringBuilder sb = new StringBuilder();
		HttpMethod method = new GetMethod(hudsonURL + JOB_URL_SUFFIX + "job/"
				+ name + "/config.xml");
		if (verbose) {
			getLog().info(
					"Config: " + hudsonURL + "job/" + name + "/config.xml");

		}
		client.executeMethod(method);
		checkResult(method.getStatusCode());
		Document dom = new SAXReader().read(method.getResponseBodyAsStream());

		// /project/triggers/*/spec/text
		for (Element trigger : (List<Element>) dom
				.selectNodes("/project/triggers/*")) {
			sb.append(String.format(INDENT + "%s: %s", trigger.getName()
					.replaceAll("hudson.triggers.", ""), trigger
					.elementText("spec"))
					+ "\n");
		}

		// /project/builders/hudson.tasks.Maven/mavenName/text
		// TODO: instead of just reporting this, CHANGE it to maven-3.0.1 and
		// submit back to server
		sb.append(INDENT);
		for (Element trigger : (List<Element>) dom
				.selectNodes("/project/builders/hudson.tasks.Maven")) {
			sb.append(trigger.elementText("mavenName") + INDENT);
		}
		sb.append("\n");

		// /project/publishers/hudson.tasks.BuildTrigger/childProjects/text
		for (Element trigger : (List<Element>) dom
				.selectNodes("/project/publishers/hudson.tasks.BuildTrigger")) {
			sb.append(String.format(INDENT + "Downstream: %s",
					trigger.elementText("childProjects"))
					+ "\n");
		}

		return sb.toString();
	}

	public String getJobDetail(HttpClient client, String name) throws Exception {
		StringBuilder sb = new StringBuilder();
		HttpMethod method = new GetMethod(hudsonURL + JOB_URL_SUFFIX + "job/"
				+ name + "/api/xml?depth=1");
		if (verbose) {
			getLog().info(
					"Detail: " + hudsonURL + JOB_URL_SUFFIX + "job/" + name
							+ "/api/xml?depth=1");
		}
		client.executeMethod(method);
		checkResult(method.getStatusCode());
		Document dom = new SAXReader().read(method.getResponseBodyAsStream());

		sb.append(INDENT
				+ (dom.selectSingleNode("/freeStyleProject/buildable")
						.getText().equals("true") ? "++ Enabled ++"
						: "-- Disabled --") + "\n");

		for (Element node : (List<Element>) dom
				.selectNodes("/freeStyleProject/build")) {
			// /freeStyleProject/build*/action/cause/shortDescription/text
			// /freeStyleProject/build*/number/text
			// /freeStyleProject/build*/result/text
			for (Element inode : (List<Element>) node
					.selectNodes("action/cause")) {
				sb.append(String.format(INDENT + "Build %s (%s): %s",
						node.elementText("number"), node.elementText("result"),
						inode.elementText("shortDescription")) + "\n");
			}

			// /freeStyleProject/build*/changeSet/revision*/module/text
			// /freeStyleProject/build*/changeSet/revision*/revision/text
			for (Element inode : (List<Element>) node
					.selectNodes("changeSet/revision")) {

				// make a valid SVN URL out of the rev and module
				if (inode.elementText("module").indexOf("svn") > 0) {
					sb.append(String.format(
							INDENT + INDENT + "%s",
							inode.elementText("module").replaceAll(
									"(trunk|branches|tags)",
									"!svn/bc/" + inode.elementText("revision")
											+ "/$1"))
							+ "\n");
				} else {
					sb.append(String.format(INDENT + INDENT + "%s @ %s",
							inode.elementText("module"),
							inode.elementText("revision"))
							+ "\n");

				}
			}
		}

		return sb.toString();
	}

	public HttpClient getHttpClient(String username, String password) {
		HttpClient client = new HttpClient();
		// establish a connection within 5 seconds
		client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(5000);

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

	private static void checkResult(int i) throws IOException {
		if (i / 100 != 2)
			throw new IOException();
	}

}
