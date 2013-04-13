package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;



/**
 * Fields to treat as descriptions
 * 
 * @author Daniel Armbrust
 *
 */
public class BPT_Descriptions extends PropertyType
{
	//These values can be used as the starting point for establishing the hierarchy of synonym types.
	//Descriptions are typically sorted (ascending) by the propertySubType values.
	//The lowest number found will be used as the FSN.
	//The next higher number will be used as the 'preferred' synonym.
	//The next higher number will be used as the 'acceptable' synonym - continuing until the value is above the description threshold.
	//Then, the first found description will be the 'preferred' description - the rest will be 'acceptable'.
	
	public static final int FSN = 0;
	public static final int SYNONYM = 20;
	public static final int DEFINITION = 40;

	public BPT_Descriptions(String terminologyName)
	{
		super("Description Types", terminologyName + " Description Type");
	}
}
