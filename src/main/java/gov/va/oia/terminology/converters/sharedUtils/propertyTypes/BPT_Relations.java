package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.PropertyType;

/**
 * @author Daniel Armbrust
 * 
 */
public class BPT_Relations extends PropertyType
{
	public BPT_Relations(String uuidRoot, String terminologyName)
	{
		super("Relation Types", terminologyName + " Relation Type", uuidRoot);
	}
}
