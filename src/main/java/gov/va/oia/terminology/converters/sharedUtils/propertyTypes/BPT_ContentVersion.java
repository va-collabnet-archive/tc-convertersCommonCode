package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

/**
 * Invented property with special handling for root node in workbench.
 * 
 * @author Daniel Armbrust
 */
public class BPT_ContentVersion extends PropertyType
{
	public Property RELEASE;
	public Property LOADER_VERSION;

	public BPT_ContentVersion()
	{
		super("Content Version");
		indexRefsetMembers = true;
		RELEASE = addProperty("Release");
		LOADER_VERSION = addProperty("Loader Version");
	}
}
