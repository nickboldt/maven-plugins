To use this plugin in your own project:

1. build it w/ `mvn3 clean install`
2. Add it to your pom.xml with this:

	<build>
		<plugins>
			<plugin>
				<groupId>org.jboss.maven.plugin</groupId>
				<artifactId>hudson-job-publisher-plugin</artifactId>
				<version>0.0.1-SNAPSHOT</version>
				<executions>
					<execution>
						<phase>validate</phase>
						<goals>
							<goal>run</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<!-- more output w/ verbose; default false -->
					<verbose>true</verbose>

					<!-- server and connection details -->
					<hudsonURL>http://localhost:8080/</hudsonURL>
					<!-- <hudsonURL>http://hudson.qa.jboss.com/hudson/</hudsonURL> -->
					<username>admin</username>
					<password>Che5tnuT#tR33</password>

					<!-- default true: existing jobs will be overwritten; set false to throw 
						an error if job exists -->
					<replaceExistingJob>true</replaceExistingJob>

					<!-- job configuration: one buildURL -->
					<buildURL>http://svn.jboss.org/repos/jbosstools/branches/jbosstools-3.2.0.Beta2/build</buildURL>
					<!-- then many identically configured components -->
					<components>archives, as, birt, bpel, bpmn, cdi, common,
						deltacloud, esb, examples, flow, freemarker, gwt, hibernatetools,
						jbpm, jmx, jsf, jst, maven, modeshape, portlet, profiler, runtime,
						seam, smooks, struts, tptp, usage, vpe, ws</components>
					<componentJobNameSuffix>-stable-branch</componentJobNameSuffix>
					<!-- then some special-case components (not in JBT tree) -->
					<properties>
						<property>
							<name>jbosstools-pi4soa-stable-branch</name>
							<value>https://pi4soa.svn.sourceforge.net/svnroot/pi4soa/branches/pi4soa-3.1.x</value>
						</property>
						<property>
							<name>jbosstools-teiid-designer-stable-branch</name>
							<value>http://anonsvn.jboss.org/repos/tdesigner/branches/7.1</value>
						</property>
						<property>
							<name>jbosstools-savara-stable-branch</name>
							<value>http://anonsvn.jboss.org/repos/savara/branches/1.1.x</value>
						</property>
					</properties>
				</configuration>
			</plugin>
		</plugins>
	</build>
