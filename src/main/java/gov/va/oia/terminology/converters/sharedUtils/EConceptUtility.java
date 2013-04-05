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
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
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
	public final UUID definitionUuid_ = UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09");  //TODO find constant
	public final UUID fullySpecifiedNameUuid_ = SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getUuids()[0];
	public final UUID descriptionAcceptableUuid_ = SnomedMetadataRf2.ACCEPTABLE_RF2.getUuids()[0];
	public final UUID descriptionPreferredUuid_ = SnomedMetadataRf2.PREFERRED_RF2.getUuids()[0];
	public final UUID usEnRefsetUuid_ = SnomedMetadataRf2.US_ENGLISH_REFSET_RF2.getUuids()[0];
	public final UUID definingCharacteristicUuid_ = SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids()[0];
	public final UUID notRefinableUuid = ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getPrimoridalUid();
	public final UUID moduleUuid_ = TkRevision.unspecifiedModuleUuid;
	public final UUID refsetMemberTypeUuid_ = RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid();
	public final String VA_REFSET_NAME = "VA Refsets";
//	public final UUID pathOriginRefSet = UUID.fromString("1239b874-41b4-32a1-981f-88b448829b4b");  //TODO find a constant for this
//	public final UUID pathRelease = UUID.fromString("88f89cc0-1d94-34a4-85ed-aa1949079314");  //TODO find constant for this
	public final UUID VA_REFSET_UUID = ConverterUUID.nameUUIDFromBytes(("gov.va.med.term.refset." + VA_REFSET_NAME).getBytes());
//	public final UUID workbenchAuxilary = TermAux.WB_AUX_PATH.getUuids()[0];

	private final String lang_ = "en";
//	private UUID pathUuid_ = workbenchAuxilary;  //start with this.
	private UUID pathUuid_ = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getPrimoridalUid();

	// Used for making unique UUIDs
	private int relUnique_ = 0;
	private int stringAnnotationUnique_ = 0;
	private int uuidAnnotationUnique_ = 0;
	private int descUnique_ = 0;
	private int refsetMemberUnique_ = 0;

	private LoadStats ls_ = new LoadStats();

	private String uuidRoot_;

	/**
	 * Creates and stores the path concept.
	 */
	public EConceptUtility(String uuidRoot, String pathName, DataOutputStream dos) throws Exception
	{
		this.uuidRoot_ = uuidRoot;
		//Start out creating our path concept, by hanging it under path/release
		//Note, this concept gets created on WorkbenchAuxiliary path.
		//TODO figure out this path non-sense
//		EConcept c = createConcept(pathName, pathRelease);
//		addUuidAnnotation(c.getConceptAttributes(), pathUuid_, pathOriginRefSet);  //mark it as a path, extending from WorkbenchAux
//		c.writeExternal(dos);
//		pathUuid_ = c.getPrimordialUuid();  //Now change the path to our new path concept
		ConverterUUID.addMapping("isA", isARelUuid_);
		ConverterUUID.addMapping("Synonym", synonymUuid_);
		ConverterUUID.addMapping("Fully Specified Name", fullySpecifiedNameUuid_);
		ConverterUUID.addMapping("US English Refset", usEnRefsetUuid_);
	}

	/**
	 * Create a concept, automatically setting as many fields as possible (adds a description, calculates
	 * the UUID, status current, etc)
	 */
	public EConcept createConcept(String preferredDescription)
	{
		return createConcept(ConverterUUID.nameUUIDFromBytes((uuidRoot_ + preferredDescription).getBytes()), preferredDescription);
	}

	/**
	 * Create a concept, automatically setting as many fields as possible (adds a description, calculates
	 * the UUID, status current, etc)
	 */
	public EConcept createConcept(String conceptPrimordialUuidOrigin, String preferredDescription)
	{
		return createConcept(ConverterUUID.nameUUIDFromBytes((uuidRoot_ + conceptPrimordialUuidOrigin).getBytes()), preferredDescription);
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
	public EConcept createVARefsetRootConcept()
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
	
//	/**
//	 * Add a workbench official "Fully Specified Name".  Convenience method for adding a description of type FSN
//	 */
//	public TkDescription addFullySpecifiedName(EConcept eConcept, String fullySpecifiedName, UUID sourceDescriptionTypeUUID, UUID sourceDescriptionRefsetUUID, boolean retired)
//	{
//		return addDescription(eConcept,	fullySpecifiedName, DescriptionType.FSN, true, sourceDescriptionTypeUUID, sourceDescriptionRefsetUUID, retired);
//	}
	
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
				ConverterUUID.nameUUIDFromBytes((uuidRoot_ + "descr:" + descUnique_++).getBytes()) : descriptionPrimordialUUID);
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
				ConverterUUID.nameUUIDFromBytes((uuidRoot_ + "stringAnnotation:" + stringAnnotationUnique_++).getBytes()), annotationValue, refsetUuid, retired, null);
	}

	/**
	 * Generated the UUID, uses the concept time
	 */
	public TkRefsetStrMember addStringAnnotation(TkComponent<?> component, String annotationValue, UUID refsetUuid, boolean retired)
	{
		return addStringAnnotation(component, ConverterUUID.nameUUIDFromBytes((uuidRoot_ + "stringAnnotation:" + stringAnnotationUnique_++).getBytes()), annotationValue,
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
	 * Generates the UUID, uses the concept time
	 */
	public TkRefexUuidMember addConceptAnnotation(EConcept eConcept, EConcept annotationValue, UUID refsetUuid)
	{
		return addUuidAnnotation(eConcept.getConceptAttributes(), annotationValue.getPrimordialUuid(), refsetUuid);
	}

	/**
	 * Generates the UUID, uses the component time
	 * 
	 * @param valueConcept - if value is null, it uses RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid()
	 */
	public TkRefexUuidMember addUuidAnnotation(TkComponent<?> component, UUID valueConcept, UUID refsetUuid)
	{
		return addUuidAnnotation(component, ConverterUUID.nameUUIDFromBytes((uuidRoot_ + "uuidAnnotation:" + uuidAnnotationUnique_++).getBytes()), valueConcept,
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
		conceptRefexMember.setUuid1(valueConcept == null ? refsetMemberTypeUuid_ : valueConcept);
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
	 */
	public TkRefexUuidMember addRefsetMember(EConcept refsetConcept, UUID targetUuid, boolean active, Long time)
	{
		return addRefsetMember(refsetConcept, targetUuid, ConverterUUID.nameUUIDFromBytes((uuidRoot_ + "refsetItem:" + refsetMemberUnique_++).getBytes()), active, time);
	}

	/**
	 * @param time = if null, set to refsetConcept time
	 */
	public TkRefexUuidMember addRefsetMember(EConcept refsetConcept, UUID targetUuid, UUID refsetMemberPrimordial, boolean active, Long time)
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
		refsetMember.setUuid1(refsetMemberTypeUuid_);
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
		return addRelationship(eConcept, ConverterUUID.nameUUIDFromBytes((uuidRoot_ + "rel:" + relUnique_++).getBytes()), targetUuid, null, null);
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
		return addRelationship(eConcept, ConverterUUID.nameUUIDFromBytes((uuidRoot_ + "rel:" + relUnique_++).getBytes()), targetUuid, relTypeUuid, time);
	}
	
	/**
	 * This rel add method handles the advanced cases where a rel type 'foo' is actually being loaded as "is_a" (or some other arbitrary type)
	 * it makes the swap, and adds the second value as a UUID annotation on the created relationship. 
	 */
	public TkRelationship addRelationship(EConcept eConcept, UUID targetUuid, Property p, Long time)
	{
		if (p.getWBTypeUUID() == null)
		{
			return addRelationship(eConcept, ConverterUUID.nameUUIDFromBytes((uuidRoot_ + "rel:" + relUnique_++).getBytes()), 
					targetUuid, p.getUUID(), null, null, time);
		}
		else
		{
			return addRelationship(eConcept, ConverterUUID.nameUUIDFromBytes((uuidRoot_ + "rel:" + relUnique_++).getBytes()), 
					targetUuid, p.getWBTypeUUID(), p.getUUID(), p.getPropertyType().getPropertyTypeReferenceSetUUID(), time);
		}
	}
	
	/**
	 * Add a relationship. The source of the relationship is assumed to be the specified concept.
	 * 
	 * @param relTypeUuid - is optional - if not provided, the default value of IS_A_REL is used.
	 * @param time - if null, now is used
	 */
	public TkRelationship addRelationship(EConcept eConcept, UUID relPrimordialUuid, UUID targetUuid, UUID relTypeUuid, Long time)
	{
		return addRelationship(eConcept,  relPrimordialUuid, targetUuid, relTypeUuid, null, null, time);
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
		object.setPathUuid(pathUuid_);
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
		return createAndStoreMetaDataConcept(ConverterUUID.nameUUIDFromBytes((uuidRoot_ + name).getBytes()), name, null, null, relParentPrimordial, null, dos);
	}

	/**
	 * Utility method to build and store a metadata concept.
	 */
	public EConcept createAndStoreMetaDataConcept(UUID primordial, String name, UUID relParentPrimordial, DataOutputStream dos) throws Exception
	{
		return createAndStoreMetaDataConcept(primordial, name, null, null, relParentPrimordial, null, dos);
	}

	/**
	 * Utility method to build and store a metadata concept.
	 * 
	 * @param secondParent - optional
	 */
	public EConcept createAndStoreMetaDataConcept(UUID primordial, String fsnName, String preferredName, String definition, 
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
		concept.writeExternal(dos);
		return concept;
	}

	public void loadMetaDataItems(PropertyType propertyType, UUID parentPrimordial, DataOutputStream dos) throws Exception
	{
		ArrayList<PropertyType> propertyTypes = new ArrayList<PropertyType>();
		propertyTypes.add(propertyType);
		loadMetaDataItems(propertyTypes, parentPrimordial, dos);
	}

	/**
	 * Create metadata EConcepts from the PropertyType structure
	 */
	public void loadMetaDataItems(Collection<PropertyType> propertyTypes, UUID parentPrimordial, DataOutputStream dos) throws Exception
	{
		for (PropertyType pt : propertyTypes)
		{
			if (pt instanceof BPT_Skip || pt.getProperties().size() == 0)
			{
				continue;
			}
			createAndStoreMetaDataConcept(pt.getPropertyTypeUUID(), pt.getPropertyTypeDescription(), parentPrimordial, dos);
			UUID secondParent = null;
			if (pt instanceof BPT_Refsets)
			{
				secondParent = ((BPT_Refsets) pt).getRefsetIdentityParent();
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
				createAndStoreMetaDataConcept(p.getUUID(), p.getSourcePropertyNameFSN(), p.getSourcePropertyPreferredName(), p.getSourcePropertyDefinition(), 
						pt.getPropertyTypeUUID(), secondParent, dos);
			}
		}
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
		createAndStoreMetaDataConcept(uuid, refsetSynonymName + " (foundation metadata concept)", refsetSynonymName, null,
				UUID.fromString("7e38cd2d-6f1a-3a81-be0b-21e6090573c2"), null, dos);
		
		//Now create the terminology specific refset type as a child
		createAndStoreMetaDataConcept(pt.getPropertyTypeReferenceSetUUID(), pt.getPropertyTypeReferenceSetName(), uuid, dos);
		
		//Finally, create the Reference set attribute children that we will put the actual properties under
		//Create the concept under "Reference set attribute (foundation metadata concept)"  7e52203e-8a35-3121-b2e7-b783b34d97f2
		uuid = Type5UuidFactory.get(refsetValueParentSynonynmName + " (foundation metadata concept)");
		createAndStoreMetaDataConcept(uuid, refsetValueParentSynonynmName + " (foundation metadata concept)", refsetValueParentSynonynmName, null, 
				UUID.fromString("7e52203e-8a35-3121-b2e7-b783b34d97f2"), null, dos).getPrimordialUuid();
		
		//Now create the terminology specific refset type as a child - very similar to above, but since this isn't the refset concept, just an organization
		//concept, I add an 's' to make it plural, and use a different UUID (calculated from the new plural)
		return createAndStoreMetaDataConcept(pt.getPropertyTypeReferenceSetName() + "s", uuid, dos).getPrimordialUuid();
	}
}
