package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import java.util.ArrayList;
import java.util.UUID;
import org.ihtsdo.etypes.EConcept;

public class Property
{
	private String sourcePropertyNameFSN_;
	private String sourcePropertyPreferredName_;
	private String sourcePropertyDefinition_;
	private boolean isDisabled_ = false;
	private int propertySubType_ = Integer.MAX_VALUE;  //Used for subtypes of descriptions, at the moment - FSN, synonym, etc.
	private PropertyType owner_;
	private UUID propertyUUID = null;
	private UUID useWBPropertyTypeInstead = null;  //see comments in setter
	
	private ArrayList<ConceptCreationNotificationListener> listeners_ = new ArrayList<>(1);

	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, boolean disabled, int propertySubType)
	{
		this.owner_ = owner;
		this.sourcePropertyNameFSN_ = sourcePropertyNameFSN;
		if (sourcePropertyNameFSN.equals(sourcePropertyPreferredName))
		{
			this.sourcePropertyPreferredName_ = null;
		}
		else
		{
			this.sourcePropertyPreferredName_ = sourcePropertyPreferredName;
		}
		this.sourcePropertyDefinition_ = sourcePropertyDefinition;
		this.isDisabled_ = disabled;
		this.propertySubType_ = propertySubType;
	}

	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, int propertySubType)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, null, false, propertySubType);
	}
	
	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName, boolean disabled)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, null, disabled, Integer.MAX_VALUE);
	}

	public Property(PropertyType owner, String sourcePropertyNameFSN, String sourcePropertyPreferredName)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyPreferredName, null, false, Integer.MAX_VALUE);
	}

	public Property(PropertyType owner, String sourcePropertyNameFSN)
	{
		this(owner, sourcePropertyNameFSN, sourcePropertyNameFSN, null, false, Integer.MAX_VALUE);
	}
	
	/**
	 * owner must be set via the set method after using this constructor!
	 */
	public Property(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, UUID wbRelType)
	{
		this(null, sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyDefinition, false, Integer.MAX_VALUE);
		setWBPropertyType(wbRelType);
	}

	public String getSourcePropertyNameFSN()
	{
		return sourcePropertyNameFSN_;
	}

	public String getSourcePropertyPreferredName()
	{
		return sourcePropertyPreferredName_;
	}
	
	public String getSourcePropertyDefinition()
	{
		return sourcePropertyDefinition_;
	}

	/**
	 * Normally, we just create the relation names as specified.  However, some, we map to 
	 * other existing WB relationships, and put the source rel name on as an extension - for example
	 * To enable the map case, set this (and use the appropriate addRelationship method)
	 */
	public void setWBPropertyType(UUID wbRelType)
	{
		this.useWBPropertyTypeInstead = wbRelType;
	}
	
	public UUID getWBTypeUUID()
	{
		return useWBPropertyTypeInstead;
	}

	protected void setOwner(PropertyType owner)
	{
		this.owner_ = owner;
	}

	public UUID getUUID()
	{
		if (propertyUUID == null)
		{
			propertyUUID = owner_.getPropertyUUID(this.sourcePropertyNameFSN_);
		}
		return propertyUUID;
	}

	public boolean isDisabled()
	{
		return isDisabled_;
	}
	
	public void setPropertySubType(int value)
	{
		this.propertySubType_ = value;
	}
	
	public int getPropertySubType()
	{
		return propertySubType_;
	}
	
	public PropertyType getPropertyType()
	{
		return owner_;
	}
	
	/**
	 * Mechanism to allow registration for notification when the corresponding eConcept has been created for this property.
	 * Callback occurs before the eConcept is written.  Useful for adding additional attributes to the eConcept.
	 * @param listener
	 */
	public void registerConceptCreationListener(ConceptCreationNotificationListener listener)
	{
		listeners_.add(listener);
	}
	
	/**
	 * This is called just before a metadata concept is written when the typical loadMetaDataItems(...) sequence is used in the eConceptUtility.  
	 * 
	 * Any the created concept will be passed to any registered listeners before the concept is written.
	 * @param concept
	 */
	public void conceptCreated(EConcept concept)
	{
		for (ConceptCreationNotificationListener ccn : listeners_)
		{
			ccn.conceptCreated(this, concept);
		}
	}
}
