package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.PropertyType;

/**
 * @author Daniel Armbrust
 * 
 */
public class BPT_Relations extends PropertyType
{
	public BPT_Relations(String terminologyName)
	{
		super("Relation Types", terminologyName + " Relation Type");
	}
	
	public BPT_Relations(String propertyTypeDescription, String terminologyName)
	{
		super(propertyTypeDescription, terminologyName + " Relation Type");
	}
}
