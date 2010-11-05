package org.jboss.tycho.plugins.p2.mirror;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MirrorMojoUnitTest {
	private File tempDir;

    private File sourceDirectory;

    private File targetDirectory;

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());
        sourceDirectory = new File( tempDir, "source" );
        sourceDirectory.mkdirs();
        targetDirectory = new File( tempDir, "target" );
        targetDirectory.mkdirs();
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir);
	}

	@Test
	public void testTrue() throws Exception {
		assertTrue(true);
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

}
