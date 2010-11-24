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
				<!-- more output w/ verbose; default false (no output == nothing to fix) -->
				<verbose>true</verbose>

				<!-- server and connection details -->				
				<hudsonURL>http://localhost:8080/</hudsonURL>
				<username>admin</username>
				<password>none</password>
				
				<!-- if true, existing jobs will be overwritten; set false to throw an error if job exists -->
				<replaceExistingJob>false</replaceExistingJob>

            </configuration>
	    </plugin>
    </plugins>
</build>
