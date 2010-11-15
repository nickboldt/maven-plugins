package org.jboss.tycho.plugins.p2.mirror;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MirrorMojoUnitTest {
	private File tempDir;

    private String sourceDirectory;

    private String targetDirectory;

	@Before
	public void setUp() throws IOException {
		tempDir = createTempDir(getClass().getSimpleName());
        sourceDirectory = tempDir.toString() + "source";
        targetDirectory = tempDir.toString() + "target";
	}

	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(tempDir);
	}

	@Test
	public void testMirror() throws Exception {
		MirrorMojo mm = new MirrorMojo();

		FileUtils.copyDirectory(new File("src/main/resources/jbds_preview_update_site"), new File(sourceDirectory));
		mm.setSourceDirectory(sourceDirectory);
		mm.setTargetDirectory(targetDirectory);
		mm.execute();
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

}
