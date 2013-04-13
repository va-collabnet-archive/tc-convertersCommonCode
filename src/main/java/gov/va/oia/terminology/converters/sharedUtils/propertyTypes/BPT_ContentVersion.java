package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

/**
 * Invented property with special handling for root node in workbench.
 * 
 * @author Daniel Armbrust
 */
public class BPT_ContentVersion extends PropertyType
{
	public enum BaseContentVersion
	{
		RELEASE("Release"), LOADER_VERSION("Loader Version");

		private Property property;

		private BaseContentVersion(String niceName)
		{
			// Don't know the owner yet - will be autofilled when we add this to the parent, below.
			property = new Property(null, niceName);
		}

		public Property getProperty()
		{
			return property;
		}
	}

	public BPT_ContentVersion()
	{
		super("Content Version");
		for (BaseContentVersion cv : BaseContentVersion.values())
		{
			addProperty(cv.getProperty());
		}
	}
}
