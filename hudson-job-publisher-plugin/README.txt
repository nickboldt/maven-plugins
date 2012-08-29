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

Browse to https://jenkins.mw.lab.eng.bos.redhat.com/hudson/ & accept the cert. Log in using kerberos login.

	Edit > Preferences > Advanced > Encryption > View Certificates > find hudson cert > Export to file /tmp/hudson.qa.jboss.com.cert

AS ROOT (default password is "changeit"):

	# /opt/sun-java2-6.0/jre/bin/keytool -list -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts | grep hudson
		# (if you need to replace a cert, delete the old one first)
		# /opt/sun-java2-6.0/jre/bin/keytool -delete -alias hudson.qa -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts
	# /opt/sun-java2-6.0/jre/bin/keytool -import -alias hudson.qa -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts -file /tmp/hudson.qa.jboss.com.cert
	# /opt/sun-java2-6.0/jre/bin/keytool -list -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts | grep hudson

To run, make sure that JAVA_HOME is set to the path where you imported the cert, eg.:

	$ export JAVA_HOME=/opt/sun-java2-6.0/; mvn clean install
-->
					<!-- more output w/ verbose; default: false -->
					<!-- <verbose>true</verbose> -->

					<!-- server and connection details -->
					<hudsonURL>http://localhost:8080/</hudsonURL>
					<!-- <hudsonURL>https://jenkins.mw.lab.eng.bos.redhat.com/hudson/</hudsonURL> -->
					<username>SET USERNAME HERE</username>
					<password>SET PASSWORD HERE</password>

					<!-- path to search for existing jobs (so as to not search entire server) -->
					<viewPath>view/DevStudio/view/DevStudio_Trunk/</viewPath>

					<!-- default: true: existing jobs will be overwritten; set false to throw 
						an error if job exists -->
					<replaceExistingJob>false</replaceExistingJob>

					<!-- local file path to use as template when publishing jobs -->
					<jobTemplateFile>config.xml</jobTemplateFile>

					<!-- job configuration: one buildURL -->
					<buildURL>http://anonsvn.jboss.org/repos/jbosstools/trunk/build/publish</buildURL>
					<branchOrTag>branches/jbosstools-4.0.0.Alpha1</branchOrTag>

					<!-- override values set above to generate one-off special jobs, like for teiid, pi4soa, savara, drools -->
					<!-- <componentJobNamePrefix>jbosstools-</componentJobNamePrefix>
					<components>teiid-designer-7.5</components> -->

					<!-- or, copy aggregate, tests, continuous jobs -->
					<componentJobNamePrefix>jbosstools-</componentJobNamePrefix>
					<components>4.0</components>
					
					<componentJobNameSuffix>_trunk.aggregate</componentJobNameSuffix>
					<componentJobNameSuffix2>_stable_branch.aggregate</componentJobNameSuffix2>

					<componentJobNameSuffix>_trunk.continuous</componentJobNameSuffix>
					<componentJobNameSuffix2>_stable_branch.continuous</componentJobNameSuffix2>
					
					<!-- or, copy JBDS jobs -->
					<componentJobNamePrefix>devstudio-</componentJobNamePrefix>
					<components>6.0</components>
					<componentJobNameSuffix>_trunk.updatesite</componentJobNameSuffix>
					<componentJobNameSuffix2>_stable_branch.updatesite</componentJobNameSuffix2>

					<!-- 
						Use this to generate 48 new jobs for 24 components. 
						If source job (ie., _trunk version) exists, copy from that to _stable_branch; 
						if not, use config.xml template intead.
					    If provide a suffix2, we'll attempt to copy jobs using the branchOrTag replacement for trunk -->
					<componentJobNamePrefix>jbosstools-4.0_trunk.component--</componentJobNamePrefix>
					<components>archives, as, birt, cdi, central, common,
						examples, forge, freemarker, gwt, hibernatetools,
						jmx, jsf, jst, maven, openshift, portlet, runtime,
						seam, struts, tests, usage, vpe, ws</components>
					<componentJobNameSuffix>_trunk</componentJobNameSuffix>
					<componentJobNameSuffix2>_stable_branch</componentJobNameSuffix2>

					<!-- alternatively, can use jobProperties to create jobs from template rather than copying from existing -->
					<!-- 
					<jobProperties>
						<property>
							<name>jbosstools-teiid-designer-7.5_stable_branch</name>
							<value>http://anonsvn.jboss.org/repos/tdesigner/branches/7.5.0.M2</value>
						</property>
					</jobProperties>
					-->

					<!-- last defined value is what's passed to Maven; so to avoid problems, use a default TESTING entry -->
					<componentJobNamePrefix>jbosstools-4.0_trunk.component--</componentJobNamePrefix>
					<components>TESTING</components>
					<componentJobNameSuffix>_trunk</componentJobNameSuffix>
					<componentJobNameSuffix2>_stable_branch</componentJobNameSuffix2>

				</configuration>
			</plugin>
		</plugins>
	</build>

2. To run, make sure that JAVA_HOME is set to the path where you imported the cert, eg.:

	$ export JAVA_HOME=/opt/sun-java2-6.0/; mvn clean install -f pom-publisher.xml
 	
