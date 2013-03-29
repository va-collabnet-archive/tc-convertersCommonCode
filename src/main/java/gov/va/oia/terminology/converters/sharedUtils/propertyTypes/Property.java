package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import java.util.UUID;

public class Property
{
	private String sourcePropertyName_;
	private String sourcePropertyDescription_;
	private boolean useSrcDescriptionForFSN_ = false;
	private boolean isDisabled_ = false;
	private PropertyType owner_;
	private UUID propertyUUID = null;

	protected Property(PropertyType owner, String sourcePropertyName, String sourcePropertyDescription, boolean disabled, boolean useSrcDescriptionForFSN)
	{
		this.owner_ = owner;
		this.sourcePropertyName_ = sourcePropertyName;
		if (sourcePropertyName.equals(sourcePropertyDescription))
		{
			this.sourcePropertyDescription_ = null;
		}
		else
		{
			this.sourcePropertyDescription_ = sourcePropertyDescription;
		}
		this.isDisabled_ = disabled;
		this.useSrcDescriptionForFSN_ = useSrcDescriptionForFSN;
	}

	public Property(PropertyType owner, String sourcePropertyName, String sourcePropertyDescription, boolean disabled)
	{
		this(owner, sourcePropertyName, sourcePropertyDescription, disabled, false);
	}

	public Property(PropertyType owner, String sourcePropertyName, String sourcePropertyDescription)
	{
		this(owner, sourcePropertyName, sourcePropertyDescription, false, false);
	}

	public Property(PropertyType owner, String sourcePropertyName)
	{
		this(owner, sourcePropertyName, sourcePropertyName, false, false);
	}

	public String getSourcePropertyName()
	{
		return sourcePropertyName_;
	}

	public String getSourcePropertyDescription()
	{
		return sourcePropertyDescription_;
	}

	/**
	 * Normally, the sourcePropertyName lines up with what we are reading from, and it is also used as the FSN value in the Workbench.
	 * The sourcePropertyDescription is added as a synonym if it is different.
	 * 
	 * Sometimes a loader prefers to use the sourcePropertyDescription as a FSN, and the sourcePropertyName as a synonym.
	 * Set this to true to enable that flip.
	 */
	public void setUseDescriptionAsFSN(boolean useSourcePropertyDescriptionAsFSN)
	{
		this.useSrcDescriptionForFSN_ = useSourcePropertyDescriptionAsFSN;
	}

	protected void setOwner(PropertyType owner)
	{
		this.owner_ = owner;
	}

	public boolean getUseSrcDescriptionForFSN()
	{
		return useSrcDescriptionForFSN_;
	}

	public UUID getUUID()
	{
		if (propertyUUID == null)
		{
			propertyUUID = owner_.getPropertyUUID(this.sourcePropertyName_);
		}
		return propertyUUID;
	}

	public boolean isDisabled()
	{
		return isDisabled_;
	}

	public PropertyType getPropertyType()
	{
		return owner_;
	}

}
