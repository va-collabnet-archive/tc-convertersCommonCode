package gov.va.oia.terminology.converters.sharedUtils.propertyTypes;

import org.ihtsdo.etypes.EConcept;

public interface ConceptCreationNotificationListener
{
	public void conceptCreated(Property property, EConcept concept);
}
