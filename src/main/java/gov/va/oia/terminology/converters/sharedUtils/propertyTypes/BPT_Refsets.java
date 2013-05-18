package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import java.util.HashMap;
import org.ihtsdo.etypes.EConcept;

/**
 * Fields to treat as refsets
 * 
 * @author Daniel Armbrust
 * 
 */
public class BPT_Refsets extends PropertyType
{
	private HashMap<String, EConcept> conceptMap_;  //We store concepts here, because by their nature, refsets can't be written until they are populated
	//this happens much later in the conversion cycle.
	private EConcept refsetIdentityParent_;  //Typically "Term-name Refsets" under "VA Refsets"

	public BPT_Refsets(String terminologyName)
	{
		super("Refsets", terminologyName + " Refsets");
		conceptMap_ = new HashMap<>();
	}
	
	public EConcept getConcept(String propertyName)
	{
		return getConcept(getProperty(propertyName));
	}
	
	public EConcept getConcept(Property property)
	{
		return conceptMap_.get(property.getSourcePropertyNameFSN());
	}
	
	public void clearConcepts()
	{
		conceptMap_.clear();
	}
	
	public void setRefsetIdentityParent(EConcept refsetIdentityParent)
	{
		refsetIdentityParent_ = refsetIdentityParent; 
	}
	
	public EConcept getRefsetIdentityParent()
	{
		return refsetIdentityParent_;
	}

	@Override
	public void conceptCreated(Property p, EConcept concept)
	{
		super.conceptCreated(p, concept);
		conceptMap_.put(p.getSourcePropertyNameFSN(), concept);
	}
}
