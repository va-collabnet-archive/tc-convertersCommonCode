package gov.va.oia.terminology.converters.sharedUtils;

import gov.va.oia.terminology.converters.sharedUtils.stats.ConverterUUID;
import java.io.DataOutputStream;
import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class ConverterBaseMojo extends AbstractMojo
{
	/**
	 * Location to write the output file
	 */
	@Parameter( required = true, defaultValue = "${project.build.directory}" )
	protected File outputDirectory;

	/**
	 * Location of the input source file(s).  May be a file or a directory, depending on the specific loader.
	 * Usually a directory.
	 */
	@Parameter (required = true)
	protected File inputFileLocation;

	/**
	 * Loader version number
	 */
	@Parameter (required = true, defaultValue = "${loader.version}")
	protected String loaderVersion;

	/**
	 * Converter result version number
	 */
	@Parameter (required = true, defaultValue = "${project.version}")
	protected String converterResultVersion;
	
	/**
	 * Set '-DskipUUIDDebug' on the command line, to disable the in memory UUID Debug map.
	 */
	@Parameter (required = false, defaultValue = "${skipUUIDDebug}")
	private String createDebugUUIDMap;
	
	protected DataOutputStream dos_;
	protected EConceptUtility conceptUtility_;
	
	public void execute() throws MojoExecutionException
	{
		ConverterUUID.disableUUIDMap_ = ((createDebugUUIDMap == null || createDebugUUIDMap.length() == 0) ? false : Boolean.parseBoolean(createDebugUUIDMap));
		if (ConverterUUID.disableUUIDMap_)
		{
			ConsoleUtil.println("The UUID Debug map is disabled - this also prevents duplicate ID detection");
		}
		
		// Set up the output
		if (!outputDirectory.exists())
		{
			outputDirectory.mkdirs();
		}
	}
}