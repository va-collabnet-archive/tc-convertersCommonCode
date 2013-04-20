package gov.va.oia.terminology.converters.sharedUtils;

import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Descriptions;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Refsets;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Relations;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.BPT_Skip;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.Property;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.PropertyType;
import gov.va.oia.terminology.converters.sharedUtils.propertyTypes.ValuePropertyPair;
import gov.va.oia.terminology.converters.sharedUtils.stats.ConverterUUID;
import gov.va.oia.terminology.converters.sharedUtils.stats.LoadStats;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EIdentifierLong;
import org.ihtsdo.etypes.EIdentifierString;
import org.ihtsdo.etypes.EIdentifierUuid;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

/**
 * Various constants and methods for building up workbench EConcepts.
 * 
 * A much nicer interface to use than trek... takes care of all of the boilerplate stuff.
 * 
 * @author Daniel Armbrust
 */

public class EConceptUtility
{
	public static enum DescriptionType{FSN, SYNONYM, DEFINITION};
	public static final UUID isARelUuid_;
	
	//What a pain.  WB constants are defined in such a way that they throw exceptions.  Sigh.
	static
	{
		try
		{
			isARelUuid_ = ArchitectonicAuxiliary.Concept.IS_A_REL.getPrimoridalUid();
		}
		catch (Exception e)
		{
			throw new RuntimeException("GAK!");
		}
	}
	
	public final UUID authorUuid_ = ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid();
	public final UUID statusCurrentUuid_ = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0];
	public final UUID statusRetiredUuid_ = SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids()[0];
	public final UUID synonymUuid_ = SnomedMetadataRf2.SYNONYM_RF2.getUuids()[0];
	public final UUID definitionUuid_ = UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09");  //TODO find constant for this "Definition (core metadata concept)" 
	public final UUID fullySpecifiedNameUuid_ = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()[0];
	public final UUID descriptionAcceptableUuid_ = SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids()[0];
	public final UUID descriptionPreferredUuid_ = SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0];
	public final UUID usEnRefsetUuid_ = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getUuids()[0];
	public final UUID definingCharacteristicUuid_ = SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()[0];
	public final UUID notRefinableUuid = ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getPrimoridalUid();
	public final UUID moduleUuid_ = TkRevision.unspecifiedModuleUuid;
	public final UUID refsetMemberTypeNormalMemberUuid_ = RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid();
	public final String VA_REFSET_NAME = "VA Refsets";
	public final UUID pathOriginRefSetUUID_ = UUID.fromString("1239b874-41b4-32a1-981f-88b448829b4b");  //TODO find a constant for this  "Path origin reference set"
	public final UUID pathRefSetUUID_ = UUID.fromString("fd9d47b7-c0a4-3eea-b3ab-2b5a3f9e888f");  //TODO find a constant for this "Path reference set"
	public final UUID pathUUID_ = ArchitectonicAuxiliary.Concept.PATH.getPrimoridalUid();
	public final UUID pathReleaseUUID_ =  ArchitectonicAuxiliary.Concept.RELEASE.getPrimoridalUid();
	public final UUID SNOMEDCoreUUID_ = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getPrimoridalUid();
	public final UUID VA_REFSET_UUID = ConverterUUID.createNamespaceUUIDFromString(null, "gov.va.med.term.refset." + VA_REFSET_NAME);
	public final UUID workbenchAuxilary = TermAux.WB_AUX_PATH.getUuids()[0];

	private final String lang_ = "en";
	private UUID terminologyPathUUID_ = workbenchAuxilary;  //start with this.

	// Used for making unique UUIDs
	private int relUnique_ = 0;
	private int stringAnnotationUnique_ = 0;
	private int uuidAnnotationUnique_ = 0;
	private int descUnique_ = 0;
	private int refsetMemberUnique_ = 0;

	private LoadStats ls_ = new LoadStats();

	/**
	 * Creates and stores the path concept.
	 */
	public EConceptUtility(String namespaceSeed, String pathName, DataOutputStream dos) throws Exception
	{
		ConverterUUID.addMapping("isA", isARelUuid_);
		ConverterUUID.addMapping("Synonym", synonymUuid_);
		ConverterUUID.addMapping("Fully Specified Name", fullySpecifiedNameUuid_);
		ConverterUUID.addMapping("US English Refset", usEnRefsetUuid_);
		ConverterUUID.addMapping("Path reference set", pathRefSetUUID_);
		ConverterUUID.addMapping("Path origin reference set", pathOriginRefSetUUID_);
		
		UUID namespace = ConverterUUID.createNamespaceUUIDFromString(null, namespaceSeed);
		ConverterUUID.configureNamespace(namespace);
		
		
//Note - Path per terminology code still doesn't work the way that we would like - the below code works - but results 
// in a workbench where you cannot view multiple terminologies at once, which is rather undesireable.  So disabled for now, 
// until Keith answers more questions about terminology-per-path
		
		
//		//Start our creating our path concept, by hanging it under path/release
//		//Note, this concept gets created on WorkbenchAuxiliary path.
//		EConcept c = createConcept(pathName, SNOMEDCoreUUID_);
//		c.writeExternal(dos);
//		
//		//Add it to the pathOriginRefSet, done on workbenchAux path.
//		EConcept pathOriginRefsetConcept = createConcept(pathOriginRefSetUUID_);
//		//Max value will be displayed as 'latest'.  Why on earth we are using an int for a time value, I have no idea.
//		addRefsetMember(pathOriginRefsetConcept, c.getPrimordialUuid(), SNOMEDCoreUUID_, Integer.MAX_VALUE, true, null);
//		pathOriginRefsetConcept.writeExternal(dos);
//		
//		//Also, add it to the pathRefset.  Also done on WorkbenchAux path.
//		EConcept pathRefsetConcept = createConcept(pathRefSetUUID_);
//		addRefsetMember(pathRefsetConcept, pathUUID_, c.getPrimordialUuid(), true, null);
//		pathRefsetConcept.writeExternal(dos);
//		
//		terminologyPathUUID_ = c.getPrimordialUuid();  //Now change the path to our new path concept
	}

	/**
	 * Create a concept, automatically setting as many fields as possible (adds a description, calculates
	 * the UUID, status current, etc)
	 */
	public EConcept createConcept(String preferredDescription)
	{
		return createConcept(ConverterUUID.createNamespaceUUIDFromString(preferredDescription), preferredDescription);
	}

	/**
	 * Create a concept, link it to a parent via is_a, setting as many fields as possible automatically.
	 */
	public EConcept createConcept(String name, UUID parentConceptPrimordial)
	{
		EConcept concept = createConcept(name);
		addRelationship(concept, parentConceptPrimordial);
		return concept;
	}

	/**
	 * Create a concept, link it to a parent via is_a, setting as many fields as possible automatically.
	 */
	public EConcept createConcept(UUID primordial, String name, UUID relParentPrimordial)
	{
		EConcept concept = createConcept(primordial, name);
		addRelationship(concept, relParentPrimordial);
		return concept;
	}
	
	public EConcept createConcept(UUID conceptPrimordialUuid)
	{
		return createConcept(conceptPrimordialUuid, (Long)null, statusCurrentUuid_);
	}

	/**
	 * Create a concept, automatically setting as many fields as possible (adds a description (en US)
	 * status current, etc
	 */
	public EConcept createConcept(UUID conceptPrimordialUuid, String preferredDescription)
	{
		return createConcept(conceptPrimordialUuid, preferredDescription, null, statusCurrentUuid_);
	}

	/**
	 * Create a concept, automatically setting as many fields as possible (adds a description (en US))
	 * 
	 * @param time - set to now if null
	 */
	public EConcept createConcept(UUID conceptPrimordialUuid, String preferredDescription, Long time, UUID status)
	{
		EConcept eConcept = createConcept(conceptPrimordialUuid, time, statusCurrentUuid_);
		addFullySpecifiedName(eConcept, preferredDescription);
		return eConcept;
	}

	/**
	 * Just create a concept and the nested conceptAttributes.
	 * 
	 * @param conceptPrimordialUuid
	 * @param time - if null, set to now
	 * @param status - if null, set to current
	 * @return
	 */
	public EConcept createConcept(UUID conceptPrimordialUuid, Long time, UUID status)
	{
		EConcept eConcept = new EConcept();
		eConcept.setPrimordialUuid(conceptPrimordialUuid);
		EConceptAttributes conceptAttributes = new EConceptAttributes();
		conceptAttributes.setDefined(false);
		conceptAttributes.setPrimordialComponentUuid(conceptPrimordialUuid);
		setRevisionAttributes(conceptAttributes, status, time);
		eConcept.setConceptAttributes(conceptAttributes);
		ls_.addConcept();
		return eConcept;
	}

	/**
	 * Create a concept with a UUID set from "gov.va.refset.VA Refsets" (VA_REFSET_UUID) and a name of "VA Refsets" (VA_REFSET_NAME)
	 * nested under ConceptConstants.REFSET
	 */
	private EConcept createVARefsetRootConcept()
	{
		return createConcept(VA_REFSET_UUID, VA_REFSET_NAME, ConceptConstants.REFSET.getUuids()[0]);
	}

	/**
	 * Add a workbench official "Fully Specified Name".  Convenience method for adding a description of type FSN
	 */
	public TkDescription addFullySpecifiedName(EConcept eConcept, String fullySpecifiedName)
	{
		return addDescription(eConcept, fullySpecifiedName, DescriptionType.FSN, true, null, null, false);
	}
	
	
	/**
	 * Add a batch of WB descriptions, following WB rules in always generating a FSN (picking the value based on the propertySubType order). 
	 * And then adding other types as specified by the propertySubType value, setting preferred / acceptable according to their ranking. 
	 */
	public List<TkDescription> addDescriptions(EConcept eConcept, List<? extends ValuePropertyPair> descriptions)
	{
		ArrayList<TkDescription> result = new ArrayList<>(descriptions.size());
		Collections.sort(descriptions);
		
		boolean haveFSN = false;
		boolean havePreferredSynonym = false;
		boolean havePreferredDefinition = false;
		for (ValuePropertyPair vpp : descriptions)
		{
			DescriptionType descriptionType = null;
			boolean preferred;
			
			if (!haveFSN)
			{
				descriptionType = DescriptionType.FSN;
				preferred = true;
				haveFSN = true;
			}
			else
			{
				if (vpp.getProperty().getPropertySubType() < BPT_Descriptions.SYNONYM)
				{
					descriptionType = DescriptionType.FSN;
					preferred = false;  //true case is handled above
				}
				else if (vpp.getProperty().getPropertySubType() >= BPT_Descriptions.SYNONYM && 
						(vpp.getProperty().getPropertySubType() < BPT_Descriptions.DEFINITION || vpp.getProperty().getPropertySubType() == Integer.MAX_VALUE))
				{
					descriptionType = DescriptionType.SYNONYM;
					if (!havePreferredSynonym)
					{
						preferred = true;
						havePreferredSynonym = true;
					}
					else
					{
						preferred = false;
					}
				}
				else if (vpp.getProperty().getPropertySubType() >= BPT_Descriptions.DEFINITION)
				{
					descriptionType = DescriptionType.DEFINITION;
					if (!havePreferredDefinition)
					{
						preferred = true;
						havePreferredDefinition = true;
					}
					else
					{
						preferred = false;
					}
				}
				else
				{
					throw new RuntimeException("Unexpected error");
				}
			}
			
			if (!(vpp.getProperty().getPropertyType() instanceof BPT_Descriptions))
			{
				throw new RuntimeException("This method requires properties that have a parent that are an instance of BPT_Descriptions");
			}
			BPT_Descriptions descPropertyType = (BPT_Descriptions) vpp.getProperty().getPropertyType();
			
			result.add(addDescription(eConcept, vpp.getUUID(), vpp.getValue(), descriptionType, preferred, vpp.getProperty().getUUID(), 
					descPropertyType.getPropertyTypeReferenceSetUUID(), vpp.isDisabled()));
		}
		
		return result;
	}
	
	/**
	 * Add a description to the concept.
	 * 
	 * @param time - if null, set to the time on the concept.
	 */
	public TkDescription addDescription(EConcept eConcept, String descriptionValue, DescriptionType wbDescriptionType, 
			boolean preferred, UUID sourceDescriptionTypeUUID, UUID sourceDescriptionRefsetUUID, boolean retired)
	{
		return addDescription(eConcept, null, descriptionValue, wbDescriptionType, preferred, sourceDescriptionTypeUUID, sourceDescriptionRefsetUUID, retired);
	}

	/**
	 * Add a description to the concept.
	 * 
	 * @param time - if null, set to the time on the concept.
	 */
	public TkDescription addDescription(EConcept eConcept, UUID descriptionPrimordialUUID, String descriptionValue, DescriptionType wbDescriptionType, 
			boolean preferred, UUID sourceDescriptionTypeUUID, UUID sourceDescriptionRefsetUUID, boolean retired)
	{
		List<TkDescription> descriptions = eConcept.getDescriptions();
		if (descriptions == null)
		{
			descriptions = new ArrayList<TkDescription>();
			eConcept.setDescriptions(descriptions);
		}
		TkDescription description = new TkDescription();
		description.setConceptUuid(eConcept.getPrimordialUuid());
		description.setLang(lang_);
		description.setPrimordialComponentUuid(descriptionPrimordialUUID == null ? 
				ConverterUUID.createNamespaceUUIDFromString("descr:" + descUnique_++) : descriptionPrimordialUUID);
		UUID descriptionTypeUuid = null;
		if (DescriptionType.FSN == wbDescriptionType)
		{
			descriptionTypeUuid = fullySpecifiedNameUuid_;
		}
		else if (DescriptionType.SYNONYM == wbDescriptionType)
		{
			descriptionTypeUuid = synonymUuid_;
		}
		else if (DescriptionType.DEFINITION == wbDescriptionType)
		{
			descriptionTypeUuid = definitionUuid_;
		}
		else
		{
			throw new RuntimeException("Unsupported descriptiontype '" + wbDescriptionType + "'");
		}
		description.setTypeUuid(descriptionTypeUuid);
		description.setText(descriptionValue);
		setRevisionAttributes(description, (retired ? statusRetiredUuid_ : statusCurrentUuid_), eConcept.getConceptAttributes().getTime());

		descriptions.add(description);
		//Add the en-us info
		addUuidAnnotation(description, (preferred ? descriptionPreferredUuid_ : descriptionAcceptableUuid_), usEnRefsetUuid_);
		
		if (sourceDescriptionTypeUUID != null && sourceDescriptionRefsetUUID != null)
		{
			addUuidAnnotation(description, sourceDescriptionTypeUUID, sourceDescriptionRefsetUUID);
		}
		
		ls_.addDescription(sourceDescriptionTypeUUID == null ? wbDescriptionType.name() : wbDescriptionType.name() + ":" + getOriginStringForUuid(sourceDescriptionTypeUUID));
		return description;
	}

	public TkIdentifier addAdditionalIds(EConcept eConcept, Object id, UUID idTypeUuid, boolean retired)
	{
		if (id != null)
		{
			List<TkIdentifier> additionalIds = eConcept.getConceptAttributes().getAdditionalIdComponents();
			if (additionalIds == null)
			{
				additionalIds = new ArrayList<TkIdentifier>();
				eConcept.getConceptAttributes().setAdditionalIdComponents(additionalIds);
			}

			// create the identifier and add it to the additional ids list
			TkIdentifier cid;
			if (id instanceof String)
			{
				cid = new EIdentifierString();
			}
			else if (id instanceof Long)
			{
				cid = new EIdentifierLong();
			}
			else if (id instanceof UUID)
			{
				cid = new EIdentifierUuid();
			}
			else
			{
				throw new RuntimeException("Unsupported identifier type - must be String, Long or UUID");
			}
			additionalIds.add(cid);

			// populate the type
			cid.setAuthorityUuid(idTypeUuid);

			// populate the actual value of the identifier
			cid.setDenotation(id);

			setRevisionAttributes(cid, (retired ? statusRetiredUuid_ : statusCurrentUuid_), eConcept.getConceptAttributes().getTime());

			ls_.addConceptId(getOriginStringForUuid(idTypeUuid));
			return cid;
		}
		return null;
	}

	public TkIdentifier addAdditionalIds(TkComponent<?> component, Object id, UUID idTypeUuid)
	{
		if (id != null)
		{
			List<TkIdentifier> additionalIds = component.getAdditionalIdComponents();
			if (additionalIds == null)
			{
				additionalIds = new ArrayList<TkIdentifier>();
				component.setAdditionalIdComponents(additionalIds);
			}

			// create the identifier and add it to the additional ids list
			TkIdentifier cid;
			if (id instanceof String)
			{
				cid = new EIdentifierString();
			}
			else if (id instanceof Long)
			{
				cid = new EIdentifierLong();
			}
			else if (id instanceof UUID)
			{
				cid = new EIdentifierUuid();
			}
			else
			{
				throw new RuntimeException("Unsupported identifier type - must be String, Long or UUID");
			}
			additionalIds.add(cid);

			// populate the type
			cid.setAuthorityUuid(idTypeUuid);

			// populate the actual value of the identifier
			cid.setDenotation(id);

			setRevisionAttributes(cid, statusCurrentUuid_, component.getTime());

			String label;
			if (component instanceof TkDescription)
			{
				label = "Description";
			}
			else
			{
				label = component.getClass().getSimpleName();
			}

			ls_.addComponentId(label, getOriginStringForUuid(idTypeUuid));
			return cid;
		}
		return null;
	}

	/**
	 * Generated the UUID, uses the concept time
	 */
	public TkRefsetStrMember addStringAnnotation(EConcept eConcept, String annotationValue, UUID refsetUuid, boolean retired)
	{
		return addStringAnnotation(eConcept.getConceptAttributes(),
				ConverterUUID.createNamespaceUUIDFromString("stringAnnotation:" + stringAnnotationUnique_++), annotationValue, refsetUuid, retired, null);
	}

	/**
	 * Generated the UUID, uses the concept time
	 */
	public TkRefsetStrMember addStringAnnotation(TkComponent<?> component, String annotationValue, UUID refsetUuid, boolean retired)
	{
		return addStringAnnotation(component, ConverterUUID.createNamespaceUUIDFromString("stringAnnotation:" + stringAnnotationUnique_++), annotationValue,
				refsetUuid, retired, null);
	}

	/**
	 * @param time - if null, uses the component time.
	 */
	public TkRefsetStrMember addStringAnnotation(TkComponent<?> component, UUID annotationPrimordialUuid, String value, UUID refsetUuid, boolean retired, Long time)
	{
		List<TkRefexAbstractMember<?>> annotations = component.getAnnotations();

		if (annotations == null)
		{
			annotations = new ArrayList<TkRefexAbstractMember<?>>();
			component.setAnnotations(annotations);
		}

		if (value != null)
		{
			TkRefsetStrMember strRefexMember = new TkRefsetStrMember();

			strRefexMember.setComponentUuid(component.getPrimordialComponentUuid());
			strRefexMember.setString1(value);
			strRefexMember.setPrimordialComponentUuid(annotationPrimordialUuid);
			strRefexMember.setRefsetUuid(refsetUuid);
			setRevisionAttributes(strRefexMember, (retired ? statusRetiredUuid_ : statusCurrentUuid_), (time == null ? component.getTime() : time));
			annotations.add(strRefexMember);

			annotationLoadStats(component, refsetUuid);
			return strRefexMember;
		}
		return null;
	}

	/**
	 * Generates the UUID, uses the component time
	 * 
	 * @param valueConcept - if value is null, it uses RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid()
	 */
	public TkRefexUuidMember addUuidAnnotation(TkComponent<?> component, UUID valueConcept, UUID refsetUuid)
	{
		return addUuidAnnotation(component, ConverterUUID.createNamespaceUUIDFromString("uuidAnnotation:" + uuidAnnotationUnique_++), valueConcept,
				refsetUuid, false, null);
	}

	/**
	 * @param time - If time is null, uses the component time.
	 * @param valueConcept - if value is null, it uses RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid()
	 */
	public TkRefexUuidMember addUuidAnnotation(TkComponent<?> component, UUID annotationPrimordialUuid, UUID valueConcept, UUID refsetUuid, boolean retired, Long time)
	{
		List<TkRefexAbstractMember<?>> annotations = component.getAnnotations();

		if (annotations == null)
		{
			annotations = new ArrayList<TkRefexAbstractMember<?>>();
			component.setAnnotations(annotations);
		}

		TkRefexUuidMember conceptRefexMember = new TkRefexUuidMember();

		conceptRefexMember.setComponentUuid(component.getPrimordialComponentUuid());
		conceptRefexMember.setPrimordialComponentUuid(annotationPrimordialUuid);
		conceptRefexMember.setUuid1(valueConcept == null ? refsetMemberTypeNormalMemberUuid_ : valueConcept);
		conceptRefexMember.setRefsetUuid(refsetUuid);
		setRevisionAttributes(conceptRefexMember, (retired ? statusRetiredUuid_ : statusCurrentUuid_), (time == null ? component.getTime() : time));

		annotations.add(conceptRefexMember);

		annotationLoadStats(component, refsetUuid);
		return conceptRefexMember;
	}

	private void annotationLoadStats(TkComponent<?> component, UUID refsetUuid)
	{
		if (component instanceof TkConceptAttributes)
		{
			ls_.addAnnotation("Concept", getOriginStringForUuid(refsetUuid));
		}
		else if (component instanceof TkDescription)
		{
			ls_.addAnnotation("Description", getOriginStringForUuid(refsetUuid));
		}
		else if (component instanceof TkRelationship)
		{
			ls_.addAnnotation(getOriginStringForUuid(((TkRelationship) component).getTypeUuid()), getOriginStringForUuid(refsetUuid));
		}
		else if (component instanceof TkRefsetStrMember)
		{
			ls_.addAnnotation(getOriginStringForUuid(((TkRefsetStrMember) component).getRefexUuid()), getOriginStringForUuid(refsetUuid));
		}
		else
		{
			ls_.addAnnotation(getOriginStringForUuid(component.getPrimordialComponentUuid()), getOriginStringForUuid(refsetUuid));
		}
	}

	/**
	 * @param time = if null, set to refsetConcept time
	 * @param refsetMemberType - if null, is set to "normal member"
	 */
	public TkRefexUuidMember addRefsetMember(EConcept refsetConcept, UUID targetUuid, UUID refsetMemberType, boolean active, Long time)
	{
		return addRefsetMember(refsetConcept, targetUuid, refsetMemberType, ConverterUUID.createNamespaceUUIDFromString("refsetItem:" + refsetMemberUnique_++), active, time);
	}

	/**
	 * @param time = if null, set to refsetConcept time
	 * @param refsetMemberType - if null, is set to "normal member"
	 */
	public TkRefexUuidMember addRefsetMember(EConcept refsetConcept, UUID targetUuid, UUID refsetMemberType, UUID refsetMemberPrimordial, boolean active, Long time)
	{
		List<TkRefexAbstractMember<?>> refsetMembers = refsetConcept.getRefsetMembers();
		if (refsetMembers == null)
		{
			refsetMembers = new ArrayList<TkRefexAbstractMember<?>>();
			refsetConcept.setRefsetMembers(refsetMembers);
		}
		
		TkRefexUuidMember refsetMember = new TkRefexUuidMember();
		refsetMember.setPrimordialComponentUuid(refsetMemberPrimordial);
		refsetMember.setComponentUuid(targetUuid);  // ComponentUuid and refsetUuid seem like they are reversed at first glance, but this is right.
		refsetMember.setRefsetUuid(refsetConcept.getPrimordialUuid());
		refsetMember.setUuid1(refsetMemberType == null ? refsetMemberTypeNormalMemberUuid_ : refsetMemberType);
		setRevisionAttributes(refsetMember, (active ? statusCurrentUuid_ : statusRetiredUuid_), (time == null ? refsetConcept.getConceptAttributes().getTime() : time));
		refsetMembers.add(refsetMember);

		ls_.addRefsetMember(getOriginStringForUuid(refsetConcept.getPrimordialUuid()));
		
		return refsetMember;
	}
	
	/**
	 * @param time = if null, set to refsetConcept time
	 * @param refsetMemberType - if null, is set to "normal member"
	 */
	private TkRefexUuidIntMember addRefsetMember(EConcept refsetConcept, UUID targetUuid, UUID refsetMemberType, int refsetMemberIntValue, boolean active, Long time)
	{
		List<TkRefexAbstractMember<?>> refsetMembers = refsetConcept.getRefsetMembers();
		if (refsetMembers == null)
		{
			refsetMembers = new ArrayList<TkRefexAbstractMember<?>>();
			refsetConcept.setRefsetMembers(refsetMembers);
		}
		
		TkRefexUuidIntMember refsetMember = new TkRefexUuidIntMember();
		
		refsetMember.setPrimordialComponentUuid(ConverterUUID.createNamespaceUUIDFromString("refsetItem:" + refsetMemberUnique_++));
		refsetMember.setComponentUuid(targetUuid);  // ComponentUuid and refsetUuid seem like they are reversed at first glance, but this is right.
		refsetMember.setRefsetUuid(refsetConcept.getPrimordialUuid());
		refsetMember.setUuid1(refsetMemberType == null ? refsetMemberTypeNormalMemberUuid_ : refsetMemberType);
		refsetMember.setInt1(refsetMemberIntValue);
		setRevisionAttributes(refsetMember, (active ? statusCurrentUuid_ : statusRetiredUuid_), (time == null ? refsetConcept.getConceptAttributes().getTime() : time));
		refsetMembers.add(refsetMember);

		ls_.addRefsetMember(getOriginStringForUuid(refsetConcept.getPrimordialUuid()));
		
		return refsetMember;
	}

	/**
	 * Add an IS_A_REL relationship, with the time set to now.
	 */
	public TkRelationship addRelationship(EConcept eConcept, UUID targetUuid)
	{
		return addRelationship(eConcept, ConverterUUID.createNamespaceUUIDFromString("rel:" + relUnique_++), targetUuid, null, null, null, null);
	}

	/**
	 * Add a relationship. The source of the relationship is assumed to be the specified concept. The UUID of the
	 * relationship is generated.
	 * 
	 * @param relTypeUuid - is optional - if not provided, the default value of IS_A_REL is used.
	 * @param time - if null, now is used
	 */
	public TkRelationship addRelationship(EConcept eConcept, UUID targetUuid, UUID relTypeUuid, Long time)
	{
		return addRelationship(eConcept, ConverterUUID.createNamespaceUUIDFromString("rel:" + relUnique_++), targetUuid, relTypeUuid, null, null, time);
	}
	
	/**
	 * This rel add method handles the advanced cases where a rel type 'foo' is actually being loaded as "is_a" (or some other arbitrary type)
	 * it makes the swap, and adds the second value as a UUID annotation on the created relationship. 
	 */
	public TkRelationship addRelationship(EConcept eConcept, UUID targetUuid, Property p, Long time)
	{
		if (p.getWBTypeUUID() == null)
		{
			return addRelationship(eConcept, ConverterUUID.createNamespaceUUIDFromString("rel:" + relUnique_++), 
					targetUuid, p.getUUID(), null, null, time);
		}
		else
		{
			return addRelationship(eConcept, ConverterUUID.createNamespaceUUIDFromString("rel:" + relUnique_++), 
					targetUuid, p.getWBTypeUUID(), p.getUUID(), p.getPropertyType().getPropertyTypeReferenceSetUUID(), time);
		}
	}
	
	/**
	 * Add a relationship. The source of the relationship is assumed to be the specified concept.
	 * 
	 * @param relTypeUuid - is optional - if not provided, the default value of IS_A_REL is used.
	 * @param time - if null, now is used
	 */
	public TkRelationship addRelationship(EConcept eConcept, UUID relPrimordialUuid, UUID targetUuid, UUID relTypeUuid, 
			UUID sourceRelTypeUUID, UUID sourceRelRefsetUUID, Long time)
	{
		List<TkRelationship> relationships = eConcept.getRelationships();
		if (relationships == null)
		{
			relationships = new ArrayList<TkRelationship>();
			eConcept.setRelationships(relationships);
		}

		TkRelationship rel = new TkRelationship();
		rel.setPrimordialComponentUuid(relPrimordialUuid);
		rel.setC1Uuid(eConcept.getPrimordialUuid());
		rel.setTypeUuid(relTypeUuid == null ? isARelUuid_ : relTypeUuid);
		rel.setC2Uuid(targetUuid);
		rel.setCharacteristicUuid(definingCharacteristicUuid_);
		rel.setRefinabilityUuid(notRefinableUuid);
		rel.setRelGroup(0);
		setRevisionAttributes(rel, null, time);

		relationships.add(rel);
		
		if (sourceRelTypeUUID != null && sourceRelRefsetUUID != null)
		{
			addUuidAnnotation(rel, sourceRelTypeUUID, sourceRelRefsetUUID);
			ls_.addRelationship(getOriginStringForUuid(relTypeUuid) + ":" + getOriginStringForUuid(sourceRelTypeUUID));
		}
		else
		{
			ls_.addRelationship(getOriginStringForUuid(relTypeUuid == null ? isARelUuid_ : relTypeUuid));
		}
		return rel;
	}

	/**
	 * Set up all the boilerplate stuff.
	 * 
	 * @param object - The object to do the setting to
	 * @param statusUuid - Uuid or null (for current)
	 * @param time - time or null (for current)
	 */
	private void setRevisionAttributes(TkRevision object, UUID statusUuid, Long time)
	{
		object.setAuthorUuid(authorUuid_);
		object.setModuleUuid(moduleUuid_);
		object.setPathUuid(terminologyPathUUID_);
		object.setStatusUuid(statusUuid == null ? statusCurrentUuid_ : statusUuid);
		object.setTime(time == null ? System.currentTimeMillis() : time.longValue());
	}

	private String getOriginStringForUuid(UUID uuid)
	{
		String temp = ConverterUUID.getUUIDCreationString(uuid);
		if (temp != null)
		{
			String[] parts = temp.split(":");
			if (parts != null && parts.length > 0)
			{
				return parts[parts.length - 1];
			}
			return temp;
		}
		return "Unknown";
	}

	public LoadStats getLoadStats()
	{
		return ls_;
	}

	public void clearLoadStats()
	{
		ls_ = new LoadStats();
	}

	/**
	 * Utility method to build and store a metadata concept.
	 */
	public EConcept createAndStoreMetaDataConcept(String name, UUID relParentPrimordial, DataOutputStream dos) throws Exception
	{
		return createMetaDataConcept(ConverterUUID.createNamespaceUUIDFromString(name), name, null, null, relParentPrimordial, null, dos);
	}

	/**
	 * Utility method to build and store a metadata concept.
	 */
	public EConcept createAndStoreMetaDataConcept(UUID primordial, String name, UUID relParentPrimordial, DataOutputStream dos) throws Exception
	{
		return createMetaDataConcept(primordial, name, null, null, relParentPrimordial, null, dos);
	}

	/**
	 * Utility method to build and store a metadata concept.
	 * @param dos - optional - does not store when not provided
	 * @param secondParent - optional
	 */
	public EConcept createMetaDataConcept(UUID primordial, String fsnName, String preferredName, String definition, 
			UUID relParentPrimordial, UUID secondParent, DataOutputStream dos)
			throws Exception
	{
		EConcept concept = createConcept(primordial, fsnName);
		addRelationship(concept, relParentPrimordial);
		if (secondParent != null)
		{
			addRelationship(concept, secondParent);
		}
		if (preferredName != null)
		{
			addDescription(concept, preferredName, DescriptionType.SYNONYM, true, null, null, false);
		}
		if (definition != null)
		{
			addDescription(concept, definition, DescriptionType.DEFINITION, true, null, null, false);
		}
		if (dos != null)
		{
			concept.writeExternal(dos);
		}
		return concept;
	}


	/**
	 * Create metadata EConcepts from the PropertyType structure
	 * NOTE - Refset types are not stored!
	 */
	public void loadMetaDataItems(PropertyType propertyType, UUID parentPrimordial, DataOutputStream dos) throws Exception
	{
		ArrayList<PropertyType> propertyTypes = new ArrayList<PropertyType>();
		propertyTypes.add(propertyType);
		loadMetaDataItems(propertyTypes, parentPrimordial, dos);
	}

	/**
	 * Create metadata EConcepts from the PropertyType structure
	 * NOTE - Refset types are not stored!
	 */
	public void loadMetaDataItems(Collection<PropertyType> propertyTypes, UUID parentPrimordial, DataOutputStream dos) throws Exception
	{
		for (PropertyType pt : propertyTypes)
		{
			if (pt instanceof BPT_Skip)
			{
				continue;
			}
			createAndStoreMetaDataConcept(pt.getPropertyTypeUUID(), pt.getPropertyTypeDescription(), parentPrimordial, dos);
			UUID secondParent = null;
			if (pt instanceof BPT_Refsets)
			{
				//Need to create the VA_Refsets concept, and create a terminology refset grouper, then use that as a second parent
				EConcept refsetRoot = createVARefsetRootConcept();
				refsetRoot.writeExternal(dos);
				
				EConcept refsetTermGroup = createConcept(pt.getPropertyTypeReferenceSetName(), refsetRoot.getPrimordialUuid());
				((BPT_Refsets) pt).setRefsetIdentityParent(refsetTermGroup);
				secondParent = refsetTermGroup.getPrimordialUuid(); 
			}
			else if (pt instanceof BPT_Descriptions)
			{
				secondParent = setupWbPropertyMetadata("Description source type reference set", "Description name in source terminology", pt, dos);
			}
			
			else if (pt instanceof BPT_Relations)
			{
				secondParent = setupWbPropertyMetadata("Relation source type reference set", "Relation name in source terminology", pt, dos);
			}
			
			for (Property p : pt.getProperties())
			{
				if (pt instanceof BPT_Refsets)
				{
					//Don't store these  yet.  User must manually store refsets after they have been populated.
					EConcept concept = createMetaDataConcept(p.getUUID(), p.getSourcePropertyNameFSN(), p.getSourcePropertyPreferredName(), p.getSourcePropertyDefinition(), 
							pt.getPropertyTypeUUID(), secondParent, null);
					((BPT_Refsets)pt).setConcept(p, concept);
				}
				else
				{
					createMetaDataConcept(p.getUUID(), p.getSourcePropertyNameFSN(), p.getSourcePropertyPreferredName(), p.getSourcePropertyDefinition(), 
							pt.getPropertyTypeUUID(), secondParent, dos);
				}
				
			}
		}
	}
	
	public void storeRefsetConcepts(BPT_Refsets refsets, DataOutputStream dos) throws IOException
	{
		refsets.getRefsetIdentityParent().writeExternal(dos);
		for (Property p : refsets.getProperties())
		{
			refsets.getConcept(p).writeExternal(dos);
		}
		refsets.clearConcepts();
	}
	
	private UUID setupWbPropertyMetadata(String refsetSynonymName, String refsetValueParentSynonynmName, PropertyType pt, DataOutputStream dos) throws Exception
	{
		if (pt.getPropertyTypeReferenceSetName() == null || pt.getPropertyTypeReferenceSetUUID() == null)
		{
			throw new RuntimeException("Unhandled case!");
		}
		//Create a concept under "Reference set (foundation metadata concept)"  7e38cd2d-6f1a-3a81-be0b-21e6090573c2
		//Now create the description type refset bucket.  UUID should always be the same - not terminology specific.  This should come from the WB, eventually.
		UUID uuid = Type5UuidFactory.get(refsetSynonymName + " (foundation metadata concept)");
		createMetaDataConcept(uuid, refsetSynonymName + " (foundation metadata concept)", refsetSynonymName, null,
				UUID.fromString("7e38cd2d-6f1a-3a81-be0b-21e6090573c2"), null, dos);
		
		//Now create the terminology specific refset type as a child
		createAndStoreMetaDataConcept(pt.getPropertyTypeReferenceSetUUID(), pt.getPropertyTypeReferenceSetName(), uuid, dos);
		
		//Finally, create the Reference set attribute children that we will put the actual properties under
		//Create the concept under "Reference set attribute (foundation metadata concept)"  7e52203e-8a35-3121-b2e7-b783b34d97f2
		uuid = Type5UuidFactory.get(refsetValueParentSynonynmName + " (foundation metadata concept)");
		createMetaDataConcept(uuid, refsetValueParentSynonynmName + " (foundation metadata concept)", refsetValueParentSynonynmName, null, 
				UUID.fromString("7e52203e-8a35-3121-b2e7-b783b34d97f2"), null, dos).getPrimordialUuid();
		
		//Now create the terminology specific refset type as a child - very similar to above, but since this isn't the refset concept, just an organization
		//concept, I add an 's' to make it plural, and use a different UUID (calculated from the new plural)
		return createAndStoreMetaDataConcept(pt.getPropertyTypeReferenceSetName() + "s", uuid, dos).getPrimordialUuid();
	}
}
