package org.jboss.tycho.plugins.p2.mirror;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.WriterStreamConsumer;
import org.sonatype.tycho.equinox.EquinoxRuntimeLocator;
import org.sonatype.tycho.plugins.p2.publisher.AbstractP2Mojo;

public abstract class AbstractMirrorMojo
    extends AbstractP2Mojo
{
    /** @component */
    private EquinoxRuntimeLocator equinoxLocator;

    /**
     * Kill the forked process after a certain number of seconds. If set to 0, wait forever for the process, never
     * timing out.
     * 
     * @parameter expression="${p2.timeout}" default-value="0"
     */
    private int forkedProcessTimeoutInSeconds;

    protected void executeMirrorApplication( String mirrorApplicationName, String[] args )
        throws Exception
    {
        try
        {
            Commandline cli = createOSGiCommandline( mirrorApplicationName );
            cli.setWorkingDirectory( getProject().getBasedir() );
            cli.addArguments( args );
            try
            {
                int result = executeCommandline( cli, forkedProcessTimeoutInSeconds );
                if ( result != 0 )
                {
                    throw new MojoFailureException( "P2 mirror return code was " + result );
                }
            }
            catch ( CommandLineException cle )
            {
                throw new MojoExecutionException( "P2 mirror failed to be executed ", cle );
            }
        }
        catch ( IOException ioe )
        {
            throw new MojoExecutionException( "Unable to execute the mirror app", ioe );
        }
    }

    private int executeCommandline( Commandline cli, int timeout )
        throws CommandLineException
    {
        getLog().info( "Command line:\n\t" + cli.toString() );
        return CommandLineUtils.executeCommandLine( cli, new DefaultConsumer(),
                                                    new WriterStreamConsumer( new OutputStreamWriter( System.err ) ),
                                                    timeout );
    }

    static Commandline createOSGiCommandline( String applicationId, File equinoxLauncher )
    {
        Commandline cli = new Commandline();

        String executable = System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + "java";
        cli.setExecutable( executable );
        cli.addArguments( new String[] { "-jar", equinoxLauncher.getAbsolutePath() } );
        cli.addArguments( new String[] { "-application", applicationId } );
        cli.addArguments( new String[] { "-nosplash" } );
        cli.addArguments( new String[] { "-consoleLog" } );
        return cli;
    }

    private Commandline createOSGiCommandline( String applicationId ) throws Exception
    {
        File equinoxLauncher = getEquinoxLauncher( equinoxLocator );
        return createOSGiCommandline( applicationId, equinoxLauncher );
    }

    private static File getEquinoxLauncher( EquinoxRuntimeLocator equinoxLocator ) throws Exception
    {
        File p2location = equinoxLocator.getRuntimeLocations().get(0);
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir( p2location );
        ds.setIncludes( new String[] { "plugins/org.eclipse.equinox.launcher_*.jar" } );
        ds.scan();
        String[] includedFiles = ds.getIncludedFiles();
        if ( includedFiles == null || includedFiles.length != 1 )
        {
            throw new IllegalStateException( "Can't locate org.eclipse.equinox.launcher bundle in " + p2location );
        }
        return new File( p2location, includedFiles[0] );
    }

}
