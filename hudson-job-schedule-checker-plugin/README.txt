To be able to connect to server, must first import certificate or you may get this error:

	javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target

AS USER (with Firefox):

Browse to https://hudson.qa.jboss.com/hudson & accept the cert.

	Edit > Preferences > Advanced > Encryption > View Certificates > find hudson cert > Export to file /tmp/hudson.qa.jboss.com

AS ROOT (default password is "changeit"):

	# /opt/sun-java2-6.0/jre/bin/keytool -list -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts | grep hudson
	# /opt/sun-java2-6.0/jre/bin/keytool -import -alias hudson.qa -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts -file /tmp/hudson.qa.jboss.com
	# /opt/sun-java2-6.0/jre/bin/keytool -list -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts | grep hudson

-----

Now, to use this plugin in your own project:

1. Add it to your pom.xml with this:

	<build>
		<plugins>
			<plugin>
				<groupId>org.jboss.maven.plugin</groupId>
				<artifactId>hudson-job-schedule-checker-plugin</artifactId>
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
					<!-- more output w/ verbose; default false -->
					<verbose>true</verbose>

					<!-- server and connection details -->
					<!-- <hudsonURL>https://hudson.qa.jboss.com/hudson/</hudsonURL> -->
					<hudsonURL>http://localhost:8080/</hudsonURL>
					<username>SET USERNAME HERE</username>
					<password>SET PASSWORD HERE</password>

				</configuration>
			</plugin>
		</plugins>
	</build>

2. To run, make sure that JAVA_HOME is set to the path where you imported the cert, eg.:

	$ export JAVA_HOME=/opt/sun-java2-6.0/; mvn clean install
 	