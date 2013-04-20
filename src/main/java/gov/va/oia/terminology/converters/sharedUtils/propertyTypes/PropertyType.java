package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import gov.va.oia.terminology.converters.sharedUtils.stats.ConverterUUID;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract base class to help in mapping code system property types into the workbench data model.
 * 
 * The main purpose of this structure is to keep the UUID generation sane across the various
 * places where UUIDs are needed in the workbench.
 * 
 * @author Daniel Armbrust
 */

public abstract class PropertyType
{
	protected static int srcVersion_ = 1;
	private UUID propertyTypeUUID = null;
	private String propertyTypeDescription_;
	private String propertyTypeReferenceSetName_;
	private UUID propertyTypeReferenceSetUUID;

	private Map<String, Property> properties_;

	public static void setSourceVersion(int version)
	{
		srcVersion_ = version;
	}
	
	protected PropertyType(String propertyTypeDescription)
	{
		this.properties_ = new HashMap<String, Property>();
		this.propertyTypeDescription_ = propertyTypeDescription;
		propertyTypeReferenceSetName_ = null;
		propertyTypeReferenceSetUUID = null;
	}

	protected PropertyType(String propertyTypeDescription, String propertyTypeRefSetName)
	{
		this.properties_ = new HashMap<String, Property>();
		this.propertyTypeDescription_ = propertyTypeDescription;
		propertyTypeReferenceSetName_ = propertyTypeRefSetName;
		propertyTypeReferenceSetUUID = ConverterUUID.createNamespaceUUIDFromString(propertyTypeReferenceSetName_);
		ConverterUUID.removeMapping(propertyTypeReferenceSetUUID);  //disable dupe detection for this one (at least, don't let this trigger it)
	}

	public UUID getPropertyTypeUUID()
	{
		if (propertyTypeUUID == null)
		{
			propertyTypeUUID = ConverterUUID.createNamespaceUUIDFromString(propertyTypeDescription_);
		}
		return propertyTypeUUID;
	}

	public String getPropertyTypeDescription()
	{
		return propertyTypeDescription_;
	}

	protected UUID getPropertyUUID(String propertyName)
	{
		return ConverterUUID.createNamespaceUUIDFromString(propertyTypeDescription_ + ":" + propertyName);
	}

	public Property getProperty(String propertyName)
	{
		return properties_.get(propertyName);
	}

	public Set<String> getPropertyNames()
	{
		return properties_.keySet();
	}

	public Collection<Property> getProperties()
	{
		return properties_.values();
	}

	public boolean containsProperty(String propertyName)
	{
		return properties_.containsKey(propertyName);
	}

	public Property addProperty(Property property)
	{
		property.setOwner(this);
		properties_.put(property.getSourcePropertyNameFSN(), property);
		return property;
	}

	public Property addProperty(String propertyNameFSN)
	{
		return addProperty(propertyNameFSN, propertyNameFSN, null, false);
	}
	
	public Property addProperty(String propertyNameFSN, int propertySubType)
	{
		return addProperty(propertyNameFSN, propertyNameFSN, null, false, propertySubType);
	}

	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition)
	{
		return addProperty(sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyDefinition, false);
	}
	
	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, boolean disabled)
	{
		return addProperty(sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyDefinition, disabled, -1);
	}

	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, boolean disabled, int propertySubType)
	{
		Property property = new Property(this, sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyDefinition, disabled, propertySubType);
		properties_.put(sourcePropertyNameFSN, property);
		return property;
	}

	/**
	 * Only adds the property if the version of the data file falls between min and max, inclusive.
	 * pass 0 in min or max to specify no min or no max, respectively
	 */
	public Property addProperty(String propertyNameFSN, int minVersion, int maxVersion)
	{
		return addProperty(propertyNameFSN, propertyNameFSN, null, minVersion, maxVersion, false);
	}
	
	/**
	 * Only adds the property if the version of the data file falls between min and max, inclusive.
	 * pass 0 in min or max to specify no min or no max, respectively
	 */
	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, 
			int minVersion, int maxVersion, boolean disabled)
	{
		return addProperty(sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyDefinition, minVersion, maxVersion, disabled, -1);
	}

	/**
	 * Only adds the property if the version of the data file falls between min and max, inclusive.
	 * pass 0 in min or max to specify no min or no max, respectively
	 */
	public Property addProperty(String sourcePropertyNameFSN, String sourcePropertyPreferredName, String sourcePropertyDefinition, 
			int minVersion, int maxVersion, boolean disabled, int propertySubType)
	{
		if ((minVersion != 0 && srcVersion_ < minVersion) || (maxVersion != 0 && srcVersion_ > maxVersion))
		{
			return null;
		}
		return addProperty(sourcePropertyNameFSN, sourcePropertyPreferredName, sourcePropertyDefinition, disabled, propertySubType);
	}
	
	public UUID getPropertyTypeReferenceSetUUID()
	{
		return propertyTypeReferenceSetUUID;
	}
	
	public String getPropertyTypeReferenceSetName()
	{
		return propertyTypeReferenceSetName_;
	}
}
