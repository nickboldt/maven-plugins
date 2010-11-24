To use this plugin in your own project:

1. build it w/ `mvn3 clean install`
2. Add it to your pom.xml with this:

<build>
    <plugins>
	    <plugin>
            <groupId>org.jboss.maven.plugin</groupId>
            <artifactId>site-aggregator-plugin</artifactId>
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

				<!-- jobs to aggregate -->
				<jobFolders>tests,common,jmx</jobFolders>
				
				<!-- base staging dir from which to source artifacts and metadata -->
				<sourceDirectory>http://download.jboss.org/jbosstools/builds/staging/</sourceDirectory>
            </configuration>
	    </plugin>
    </plugins>
</build>
