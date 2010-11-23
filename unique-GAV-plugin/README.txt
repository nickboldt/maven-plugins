To use this plugin in your own project:

1. First, build it:

mvn3 clean install

2. Next, add it to your pom.xml with this:

<build>
    <plugins>
	    <plugin>
            <groupId>org.jboss.maven.plugin</groupId>
            <artifactId>unique-GAV-plugin</artifactId>
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

				<!-- three types of output to check for; default only errors=true -->
				<doInfo>true</doInfo>
				<doWarn>true</doWarn>
				<doError>true</doError>

				<!-- where to check; default this folder (".") -->
				<sourceDirectory>.</sourceDirectory>
            </configuration>
	    </plugin>
    </plugins>
</build>

3. run it as part of a build (mvn3 clean install), or just do this to run the plugin w/o compiling or installing output:

 mvn3 validate -P GAV,local.target

