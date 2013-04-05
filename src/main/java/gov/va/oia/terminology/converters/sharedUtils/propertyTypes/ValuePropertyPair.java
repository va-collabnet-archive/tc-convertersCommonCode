package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import java.util.UUID;

public class ValuePropertyPair implements Comparable<ValuePropertyPair>
{
	private Property property_;
	private String value_;
	private Boolean valueDisabled_ = null;  //used for overriding the property default with instance data
	private UUID descriptionUUID_;
	
	
	public ValuePropertyPair(String value, UUID descriptionUUID, Property property)
	{
		value_ = value;
		property_ = property;
		descriptionUUID_ = descriptionUUID;
	}
	
	public ValuePropertyPair(String value, Property property)
	{
		value_ = value;
		property_ = property;
		descriptionUUID_ = null;
	}
	
	public Property getProperty()
	{
		return property_;
	}

	public String getValue()
	{
		return value_;
	}
	
	public UUID getUUID()
	{
		return descriptionUUID_;
	}
	
	public void setDisabled(boolean disabled)
	{
		valueDisabled_ = disabled;
	}
	
	/**
	 * Should this description instance be disabled, taking into account local override (if set) and falling back to property default.
	 * @return
	 */
	public boolean isDisabled()
	{
		if (valueDisabled_ != null)
		{
			return valueDisabled_;
		}
		else
		{
			return property_.isDisabled();
		}
	}

	@Override
	public int compareTo(ValuePropertyPair o)
	{
		int result = property_.getPropertyType().getClass().getName().compareTo(o.property_.getPropertyType().getClass().getName());
		if (result == 0)
		{
			result = property_.getPropertySubType() - o.property_.getPropertySubType();
			if (result == 0)
			{
				result = property_.getSourcePropertyNameFSN().compareTo(o.property_.getSourcePropertyNameFSN());
				if (result == 0)
				{
					result = value_.compareTo(o.value_);
				}
			}
		}
		return result;
	}
}
