package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import java.util.UUID;

/**
 * Fields to treat as refsets
 * 
 * @author Daniel Armbrust
 * 
 */
public class BPT_Refsets extends PropertyType
{
	private UUID refsetIdentityParent;

	/**
	 * @param refsetIdentityParent A second UUID to set as a parent to this concept, typically
	 *            ConceptConstants.REFSET.getUuids()[0] or a child of it
	 */
	public BPT_Refsets(UUID refsetIdentityParent)
	{
		super("Refsets");
		this.refsetIdentityParent = refsetIdentityParent;
	}

	/**
	 * A second UUID to set as a parent to this concept, typically ConceptConstants.REFSET.getUuids()[0] or a child of it
	 */
	public UUID getRefsetIdentityParent()
	{
		return this.refsetIdentityParent;
	}
}
