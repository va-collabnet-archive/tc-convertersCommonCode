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
	private String uuidRoot_;

	private Map<String, Property> properties_;

	public static void setSourceVersion(int version)
	{
		srcVersion_ = version;
	}

	protected PropertyType(String propertyTypeDescription, String uuidRoot)
	{
		this.properties_ = new HashMap<String, Property>();
		this.propertyTypeDescription_ = propertyTypeDescription;
		this.uuidRoot_ = uuidRoot;
	}

	public UUID getPropertyTypeUUID()
	{
		if (propertyTypeUUID == null)
		{
			propertyTypeUUID = ConverterUUID.nameUUIDFromBytes((uuidRoot_ + propertyTypeDescription_).getBytes());
		}
		return propertyTypeUUID;
	}

	public String getPropertyTypeDescription()
	{
		return propertyTypeDescription_;
	}

	protected UUID getPropertyUUID(String propertyName)
	{
		return ConverterUUID.nameUUIDFromBytes((uuidRoot_ + propertyTypeDescription_ + ":" + propertyName).getBytes());
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
		properties_.put(property.getSourcePropertyName(), property);
		return property;
	}

	public Property addProperty(String propertyName)
	{
		return addProperty(propertyName, propertyName, false);
	}

	public Property addProperty(String sourcePropertyName, String sourcePropertyDescription)
	{
		return addProperty(sourcePropertyName, sourcePropertyDescription, false);
	}

	public Property addProperty(String sourcePropertyName, String sourcePropertyDescription, boolean disabled)
	{
		Property property = new Property(this, sourcePropertyName, sourcePropertyDescription, disabled);
		properties_.put(sourcePropertyName, property);
		return property;
	}

	/**
	 * Only adds the property if the version of the data file falls between min and max, inclusive.
	 * pass 0 in min or max to specify no min or no max, respectively
	 */
	public Property addProperty(String propertyName, int minVersion, int maxVersion)
	{
		return addProperty(propertyName, propertyName, minVersion, maxVersion, false);
	}

	/**
	 * Only adds the property if the version of the data file falls between min and max, inclusive.
	 * pass 0 in min or max to specify no min or no max, respectively
	 */
	public Property addProperty(String sourcePropertyName, String sourcePropertyDescription, int minVersion, int maxVersion, boolean disabled)
	{
		if ((minVersion != 0 && srcVersion_ < minVersion) || (maxVersion != 0 && srcVersion_ > maxVersion))
		{
			return null;
		}
		return addProperty(sourcePropertyName, sourcePropertyDescription, disabled);
	}
}
