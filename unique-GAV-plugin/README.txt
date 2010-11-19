To use this plugin in your own project:

1. build it w/ `mvn3 clean install`
2. Add it to your pom.xml with this:

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

and

<dependencies>
	<dependency>
		<groupId>org.apache.maven</groupId>
		<artifactId>maven-plugin-api</artifactId>
		<version>2.0</version>
	</dependency>
</dependencies>
            