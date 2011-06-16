
To use this plugin in your own project:

1. Add it to your pom.xml with this:

	<build>
		<plugins>
			<plugin>
				<groupId>org.jboss.maven.plugin</groupId>
				<artifactId>hudson-job-publisher-plugin</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<executions>
					<execution>
						<phase>install</phase>
						<goals>
							<goal>run</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
<!-- 
To be able to connect to server, must first import certificate or you may get this error:

	javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target

AS USER (with Firefox):

Browse to https://hudson.qa.jboss.com/hudson & accept the cert.

	Edit > Preferences > Advanced > Encryption > View Certificates > find hudson cert > Export to file /tmp/hudson.qa.jboss.com.cert

AS ROOT (default password is "changeit"):

	# /opt/sun-java2-6.0/jre/bin/keytool -list -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts | grep hudson
	# /opt/sun-java2-6.0/jre/bin/keytool -import -alias hudson.qa -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts -file /tmp/hudson.qa.jboss.com.cert
	# /opt/sun-java2-6.0/jre/bin/keytool -list -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts | grep hudson

To run, make sure that JAVA_HOME is set to the path where you imported the cert, eg.:

	$ export JAVA_HOME=/opt/sun-java2-6.0/; mvn clean install
-->
					<!-- more output w/ verbose; default false -->
					<verbose>true</verbose>

					<!-- server and connection details -->
					<hudsonURL>http://localhost:8080/</hudsonURL>
					<!-- <hudsonURL>https://hudson.qa.jboss.com/hudson/</hudsonURL> -->
					<username>SET USERNAME HERE</username>
					<password>SET PASSWORD HERE</password>

					<!-- default true: existing jobs will be overwritten; set false to throw 
						an error if job exists -->
					<replaceExistingJob>false</replaceExistingJob>
					
					<!-- local file path to use as template when publishing jobs -->
					<jobTemplateFile>config.xml</jobTemplateFile>

					<!-- job configuration: one buildURL -->
					<buildURL>http://anonsvn.jboss.org/repos/jbosstools/trunk/build</buildURL>
					<branchOrTag>branches/jbosstools-3.3.0.M2</branchOrTag>
					<!-- then many identically configured components -->
					<componentJobNamePrefix>jbosstools-3.3_trunk.component--</componentJobNamePrefix>
					<!--
					<components>jmx, archives, as, birt, bpel, cdi, common, deltacloud
						esb, examples, forge, flow, freemarker, hibernatetools,
						jbpm, jmx, jsf, jst, maven, modeshape, portlet, profiler, runtime,
						seam, smooks, struts, usage, vpe, ws</components>
					-->
					<components>TESTING</components>
					<componentJobNameSuffix>_trunk</componentJobNameSuffix>
					<!--  if provide a suffix2, we'll attempt to copy jobs using the branchOrTag replacement for trunk -->
					<componentJobNameSuffix2>_stable_branch</componentJobNameSuffix2>
					<!-- then some special-case components (not in JBT tree) -->
					<properties>
						<property>
							<name>jbosstools-teiid-designer-7.5_stable_branch</name>
							<value>http://anonsvn.jboss.org/repos/tdesigner/branches/7.5.0.M2</value>
						</property>
					</properties>
				</configuration>
			</plugin>
		</plugins>
	</build>

2. To run, make sure that JAVA_HOME is set to the path where you imported the cert, eg.:

	$ export JAVA_HOME=/opt/sun-java2-6.0/; mvn clean install
 	
