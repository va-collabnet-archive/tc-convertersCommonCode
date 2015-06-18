package gov.va.oia.econgen;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_member.TkRefexRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

public class EConceptChangeSetUtility {
	UUID inactiveStatus = UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724");
	static long importTime = (new Date()).getTime();
	
	public void retireCon(EConcept oldCon) {
		oldCon.getConceptAttributes().revisions = new ArrayList<TkConceptAttributesRevision>();
		TkConceptAttributesRevision attrRev = new TkConceptAttributesRevision();
		attrRev.setStatusUuid(inactiveStatus);
			
	}
	
	protected void retireRefsets(List<TkRefexAbstractMember<?>> refsets) {
		if (refsets != null) {
			for (TkRefexAbstractMember refset : refsets) {
				TkRevision rev = null;

				switch (refset.getType()) {
					case MEMBER: 
						rev = new TkRefexRevision();
						break;
					
					case CID: 
						rev = new TkRefexUuidRevision();
						((TkRefexUuidRevision)rev).setUuid1(((TkRefexUuidMember)refset).getUuid1());
						break;
						
					case STR: 
						rev = new TkRefsetStrRevision();
						((TkRefsetStrRevision)rev).setString1(((TkRefsetStrMember)refset).getString1());
						break;
						
					case INT: 
						rev = new TkRefexIntRevision();
						((TkRefexIntRevision)rev).setInt1(((TkRefexIntMember)refset).getInt1());
						break;
						
					case CID_INT: 
						rev = new TkRefexUuidIntRevision();
						((TkRefexUuidIntRevision)rev).setUuid1(((TkRefexUuidIntMember)refset).getUuid1());
						((TkRefexUuidIntRevision)rev).setInt1(((TkRefexUuidIntMember)refset).getInt1());
						break;
						
					default:
						Logger.getLogger(EConceptChangeSetUtility.class.getName()).log(Level.SEVERE, "Have unhandled Refset Type for making revisions:" + refset.getType());
						break;
				}

				
				rev.statusUuid = inactiveStatus;
				rev.time = importTime;
				rev.authorUuid = refset.authorUuid;
				rev.moduleUuid = refset.moduleUuid;
				rev.pathUuid = refset.pathUuid;

				if (refset.getRevisionList() == null) {
					refset.revisions = new ArrayList<TkRefexAbstractMember>(); 
				}
		
				refset.getRevisionList().add(rev);
				
				retireRefsets(refset.getAnnotations());
			}
		}
	}
/*	
	protected void retireDesc(TkDescription oldDesc) {
		TkDescriptionRevision rev = new TkDescriptionRevision();
		
		rev.statusUuid = inactiveStatus;
		rev.time = importTime;
		rev.authorUuid = oldDesc.authorUuid;
		rev.moduleUuid = oldDesc.moduleUuid;
		rev.pathUuid = oldDesc.pathUuid;
		rev.initialCaseSignificant = oldDesc.initialCaseSignificant;
		rev.lang = oldDesc.lang;
		rev.text = oldDesc.text;
		rev.typeUuid = oldDesc.typeUuid;
		
		if (oldDesc.getRevisionList() == null) {
			oldDesc.revisions = new ArrayList<TkDescriptionRevision>(); 
		}
		oldDesc.getRevisionList().add(rev);
		
		retireRefsets(oldDesc.getAnnotations());

	}
*/
	
	
	protected TkDescription retireDesc(TkDescription oldDesc) {
		TkDescription retDesc = new TkDescription();
		
		retDesc.primordialUuid = oldDesc.primordialUuid;
		retDesc.conceptUuid = oldDesc.conceptUuid;
		retDesc.statusUuid = inactiveStatus;
		retDesc.time = importTime;
		retDesc.authorUuid = oldDesc.authorUuid;
		retDesc.moduleUuid = oldDesc.moduleUuid;
		retDesc.pathUuid = oldDesc.pathUuid;
		retDesc.initialCaseSignificant = oldDesc.initialCaseSignificant;
		retDesc.lang = oldDesc.lang;
		retDesc.text = oldDesc.text;
		retDesc.typeUuid = oldDesc.typeUuid;
		
//		retireRefsets(retDesc, oldDesc.getAnnotations());

		return retDesc;
	}

	protected TkRelationship retireRel(TkRelationship oldRel) {
		TkRelationship retRel = new TkRelationship();
		
		retRel.primordialUuid = oldRel.primordialUuid;
		retRel.statusUuid = inactiveStatus;
		retRel.time = importTime;
		retRel.authorUuid = oldRel.authorUuid;
		retRel.moduleUuid = oldRel.moduleUuid;
		retRel.pathUuid = oldRel.pathUuid;
		retRel.characteristicUuid = oldRel.characteristicUuid;
		retRel.relGroup = oldRel.relGroup;
		retRel.refinabilityUuid = oldRel.refinabilityUuid;
		retRel.typeUuid = oldRel.typeUuid;
		retRel.c1Uuid= oldRel.c1Uuid;
		retRel.c2Uuid = oldRel.c2Uuid;
		retRel.typeUuid = oldRel.typeUuid;
		retRel.typeUuid = oldRel.typeUuid;
		
		return retRel;
//		retireRefsets(oldRel.getAnnotations());
	}



/*	protected void retireRel(TkRelationship oldRel) {
		TkRelationshipRevision rev = new TkRelationshipRevision();
		
		rev.statusUuid = inactiveStatus;
		rev.time = importTime;
		rev.authorUuid = oldRel.authorUuid;
		rev.moduleUuid = oldRel.moduleUuid;
		rev.pathUuid = oldRel.pathUuid;
		rev.characteristicUuid = oldRel.characteristicUuid;
		rev.group = oldRel.relGroup;
		rev.refinabilityUuid = oldRel.refinabilityUuid;
		rev.typeUuid = oldRel.typeUuid;
		
		
		if (oldRel.getRevisionList() == null) {
			oldRel.revisions = new ArrayList<TkRelationshipRevision>(); 
		}
		oldRel.getRevisionList().add(rev);
		
		retireRefsets(oldRel.getAnnotations());
	}

*/}
