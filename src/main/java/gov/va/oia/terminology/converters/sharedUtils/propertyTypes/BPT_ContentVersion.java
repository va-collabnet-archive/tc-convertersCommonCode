package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.PropertyType;


/**
 * Invented property with special handling for root node in workbench.
 * @author Daniel Armbrust
 */
public class BPT_ContentVersion extends PropertyType
{
	public BPT_ContentVersion(String uuidRoot)
	{
		super("Content Version", uuidRoot);
	}
}
