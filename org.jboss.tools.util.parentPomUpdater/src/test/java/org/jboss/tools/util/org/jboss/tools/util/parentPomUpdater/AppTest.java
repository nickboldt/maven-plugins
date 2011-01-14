package org.jboss.tools.util.org.jboss.tools.util.parentPomUpdater;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.dom4j.DocumentException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AppTest {

	private File tempDir;

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());
		System.out
				.println("======================================================================");
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir);
		System.out
				.println("======================================================================");
	}

	// @Test
	// public void testUsage() throws IOException {
	// this test will fail the build because it exits with status = 1
	// App.main(new String[] {});
	// Assert.assertTrue(true);
	// }

	@Test
	public void testExecute() throws IOException, DocumentException {
		App.main(new String[] { "/home/nboldt/tru/jmx",
				"org.jboss.tools:org.jboss.tools.parent.pom:0.0.1-SNAPSHOT",
				"org.jboss.tools:org.jboss.tools.parent.pom:0.0.2-SNAPSHOT" });
		Assert.assertTrue(true);
	}

	private File createTempDir(String prefix) throws IOException {
		File directory = File.createTempFile(prefix, "");
		if (directory.delete()) {
			directory.mkdirs();
			return directory;
		} else {
			throw new IOException("Could not create temp directory at: "
					+ directory.getAbsolutePath());
		}
	}

	public void execute() throws Exception {
	}

}
