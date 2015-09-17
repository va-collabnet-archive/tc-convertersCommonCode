package gov.va.oia.econgen;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
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

class EConceptDiffUtility  {
	UUID inactiveStatus = UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724");
	private List<TkRefexAbstractMember<?>> oldLangRefexSet = new ArrayList<TkRefexAbstractMember<?>>();
	private List<TkRefexAbstractMember<?>> newLangRefexSet = new ArrayList<TkRefexAbstractMember<?>>();
	
	static long newImportDate;
	static boolean conceptChangeFound = false;
	static List<TkRefexAbstractMember<?>> emptyAnnotations = new ArrayList<TkRefexAbstractMember<?>>();


	protected List<TkIdentifier> handleIds(List<TkIdentifier> oldIds, List<TkIdentifier> newIds) {
		List<TkIdentifier> diffIds = new ArrayList<TkIdentifier>();
		List<TkIdentifier> newIdsFound = new ArrayList<TkIdentifier>();
		
		if (newIds == null || newIds.size() == 0) {
			for (TkIdentifier oldId : oldIds) {
				oldId.setStatusUuid(inactiveStatus);
				diffIds.add(oldId);
				conceptChangeFound = true;
			}
		} else {
			if (oldIds != null) {
				for (TkIdentifier oldId : oldIds) {
					boolean matchFound = false;
					
					for (TkIdentifier newId : newIds) {
						if (oldId.equals(newId)) {
							matchFound = true;
							break;
						}
					}
					
					if (!matchFound) {
						newIdsFound.add(oldId);
						conceptChangeFound = true;
					}	
				}
				
				diffIds.addAll(newIdsFound);
			}
		}
		
		return diffIds;
	}



	List<TkRefexAbstractMember<?>>  handleRefsets(List<TkRefexAbstractMember<?>> oldRefsets, List<TkRefexAbstractMember<?>> newRefsets) {
		try {
			List<TkRefexAbstractMember<?>> diffRefsets = new ArrayList<TkRefexAbstractMember<?>>();
	
			/*
			if(newRefsets != null) {
	 			Iterator<TkRefexAbstractMember<?>> itr = newRefsets.iterator();
	 
			
				while (itr.hasNext()) {
					TkRefexAbstractMember<?> member = itr.next();
					
					// Language Refset
					if (!member.getRefexUuid().equals(UUID.fromString("a02c685c-df26-5c8e-8c45-4c64ac589f62"))) {
						oldLangRefexSet.add(member);
						itr.remove();
					}
				}
			}
			
			if(oldRefsets != null) {
				Iterator<TkRefexAbstractMember<?>> itr = oldRefsets.iterator();
				
				while (itr.hasNext()) {
					TkRefexAbstractMember<?> member = itr.next();
	
					// Language Refset
					if (!itr.next().getRefexUuid().equals(UUID.fromString("a02c685c-df26-5c8e-8c45-4c64ac589f62"))) {
						newLangRefexSet.add(member);
	
						itr.remove();
					}
				}
			}			
			*/
			// Handle Retired Refsets (don't need to retire the annots' annots I think)
			retireRefsets(oldRefsets, newRefsets, diffRefsets);
			
			// Handle New Refsets (don't need to do more than add I think)
			newMembers(oldRefsets, newRefsets, diffRefsets);
			
			// Handle existing Refsets (need to handle recursively I think)
			existingMembers(oldRefsets, newRefsets, diffRefsets);
	
			return diffRefsets;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;

	}

	
	private void existingMembers(List<TkRefexAbstractMember<?>> oldRefsets,
			List<TkRefexAbstractMember<?>> newRefsets,
			List<TkRefexAbstractMember<?>> diffRefsets) {
		
		if (newRefsets != null && oldRefsets != null) {
			for (TkRefexAbstractMember<?> newMember : newRefsets) {
				for (TkRefexAbstractMember<?> oldMember : oldRefsets) {
					if ((newMember.getPrimordialComponentUuid().equals(oldMember.getPrimordialComponentUuid())) &&
						(!newMember.equals(oldMember))) {
						conceptChangeFound = true;
						
						TkRefexAbstractMember<?> diffRefset = diffMember(oldMember, newMember);
						diffRefset.annotations = handleRefsets(oldMember.annotations, newMember.annotations);
						diffRefsets.add(diffRefset);
					}
				}
			}		
		}
	}
	
	private TkRefexAbstractMember diffMember(
			TkRefexAbstractMember oldMember,
			TkRefexAbstractMember newMember)
	{
		TkRevision rev = null;

		try {
			switch (newMember.getType()) {
				case MEMBER: 
					rev = new TkRefexRevision();
					break;
				
				case CID: 
					rev = new TkRefexUuidRevision();
					((TkRefexUuidRevision)rev).setUuid1(((TkRefexUuidMember)newMember).getUuid1());
					break;
					
				case STR: 
					rev = new TkRefsetStrRevision();
					((TkRefsetStrRevision)rev).setString1(((TkRefsetStrMember)newMember).getString1());
					break;
					
				case INT: 
					rev = new TkRefexIntRevision();
					((TkRefexIntRevision)rev).setInt1(((TkRefexIntMember)newMember).getInt1());
					break;
					
				case CID_INT: 
					rev = new TkRefexUuidIntRevision();
					((TkRefexUuidIntRevision)rev).setUuid1(((TkRefexUuidIntMember)newMember).getUuid1());
					((TkRefexUuidIntRevision)rev).setInt1(((TkRefexUuidIntMember)newMember).getInt1());
					break;
					
				default:
					String errStr = "Have unhandled Refset Type for making revisions:" + newMember.getType();
					Logger.getLogger(EConceptDiffUtility.class.getName()).log(Level.SEVERE, errStr);
					throw new Exception(errStr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		rev.statusUuid = newMember.statusUuid;
		rev.time = newMember.time;
		rev.authorUuid = newMember.authorUuid;
		rev.moduleUuid = newMember.moduleUuid;
		rev.pathUuid = newMember.pathUuid;

		if (oldMember.getRevisionList() == null) {
			oldMember.revisions = new ArrayList<TkRefexAbstractMember>(); 
		}

		oldMember.annotations = handleRefsets(oldMember.getAnnotations(), newMember.getAnnotations());

		oldMember.getRevisionList().add(rev);
			

		return oldMember;
	}

	private void newMembers(List<TkRefexAbstractMember<?>> oldRefsets,
			List<TkRefexAbstractMember<?>> newRefsets,
			List<TkRefexAbstractMember<?>> diffRefsets) {
		
		if (newRefsets != null) {
			if (oldRefsets == null) {
				diffRefsets.addAll(newRefsets);
			} else {
				for (TkRefexAbstractMember<?> newMember : newRefsets) {
					
					boolean matchFound = false;
					for (TkRefexAbstractMember<?> oldMember : oldRefsets) {
						if (newMember.getPrimordialComponentUuid().equals(oldMember.getPrimordialComponentUuid())) {
							matchFound = true;
							break;
						}
					}
					
					if (!matchFound) {
						conceptChangeFound = true;
						diffRefsets.add(newMember);
					}
				}		
			}
		}
	}

	private void retireRefsets(List<TkRefexAbstractMember<?>> oldRefsets,
			List<TkRefexAbstractMember<?>> newRefsets,
			List<TkRefexAbstractMember<?>> diffRefsets) {
		List<TkRefexAbstractMember<?>> retRefsets = getRetiredRefsets(oldRefsets, newRefsets);

		retireRefsets(retRefsets, newImportDate);
		
		diffRefsets.addAll(retRefsets);
	}

	private List<TkRefexAbstractMember<?>> getRetiredRefsets(List<TkRefexAbstractMember<?>> oldCompList, List<TkRefexAbstractMember<?>> newCompList) {
		List<TkRefexAbstractMember<?>> retiredList = new ArrayList<TkRefexAbstractMember<?>>();
		
		boolean matchFound = false;
		
		if (oldCompList != null) {
			for (TkRefexAbstractMember<?> oldMember : oldCompList) {
				if (newCompList == null) {
					retiredList.addAll(oldCompList);
				} else {
					for (TkRefexAbstractMember<?> newMember : newCompList) {
						if (oldMember.getPrimordialComponentUuid().equals(newMember.getPrimordialComponentUuid())) {
							matchFound = true;
							break;
						}
					}
		
					if (!matchFound) {
						conceptChangeFound = true;
						retiredList.add(oldMember);
					}
				}
			}
		}
		
		return retiredList;
	}

	protected void retireRefsets(List<TkRefexAbstractMember<?>> refsets, long retireTime) {
		if (refsets != null) {
			for (TkRefexAbstractMember refset : refsets) {
				TkRevision rev = null;

				try {
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
							String errStr = "Have unhandled Refset Type for making revisions:" + refset.getType();
							Logger.getLogger(EConceptDiffUtility.class.getName()).log(Level.SEVERE, errStr);
							throw new Exception(errStr);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
				rev.statusUuid = inactiveStatus;
				rev.time = newImportDate;
				rev.authorUuid = refset.authorUuid;
				rev.moduleUuid = refset.moduleUuid;
				rev.pathUuid = refset.pathUuid;

				if (refset.getRevisionList() == null) {
					refset.revisions = new ArrayList<TkRefexAbstractMember>(); 
				}
		
				refset.annotations = handleRefsets(refset.getAnnotations(), emptyAnnotations);

				refset.getRevisionList().add(rev);
				
			}
		}
	}
	
	protected TkDescription retireDesc(TkDescription oldDesc, long retireTime) {
		TkDescription retDesc = new TkDescription();
		
		retDesc.primordialUuid = oldDesc.primordialUuid;
		retDesc.conceptUuid = oldDesc.conceptUuid;
		retDesc.statusUuid = inactiveStatus;
		retDesc.time = retireTime;
		retDesc.authorUuid = oldDesc.authorUuid;
		retDesc.moduleUuid = oldDesc.moduleUuid;
		retDesc.pathUuid = oldDesc.pathUuid;
		retDesc.initialCaseSignificant = oldDesc.initialCaseSignificant;
		retDesc.lang = oldDesc.lang;
		retDesc.text = oldDesc.text;
		retDesc.typeUuid = oldDesc.typeUuid;
		
		retireRefsets(retDesc.annotations, retireTime);

		return retDesc;
	}

	protected TkRelationship retireRel(TkRelationship oldRel, long retireTime) {
		TkRelationship retRel = new TkRelationship();
		
		retRel.primordialUuid = oldRel.primordialUuid;
		retRel.statusUuid = inactiveStatus;
		retRel.time = retireTime;
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
		
		retireRefsets(retRel.annotations, retireTime);

		return retRel;
	}

	public void retireCon(EConcept oldCon, long retireTime) {
		oldCon.getConceptAttributes().revisions = new ArrayList<TkConceptAttributesRevision>();
		TkConceptAttributesRevision attrRev = new TkConceptAttributesRevision();

		attrRev.statusUuid = inactiveStatus;
		attrRev.time = retireTime;
		attrRev.authorUuid = oldCon.conceptAttributes.authorUuid;
		attrRev.moduleUuid = oldCon.conceptAttributes.moduleUuid;
		attrRev.pathUuid = oldCon.conceptAttributes.pathUuid;
		
		oldCon.getConceptAttributes().revisions.add(attrRev);
		
		retireRefsets(oldCon.getConceptAttributes().annotations, retireTime);
		
		for (TkDescription desc : oldCon.getDescriptions()) {
			retireDesc(desc, retireTime);
		}
			
		for (TkRelationship desc : oldCon.getRelationships()) {
			retireRel(desc, retireTime);
		}
	}
	
	public List<TkRefexAbstractMember<?>> getOldLangRefexSet() {
		return oldLangRefexSet;
	}
	public List<TkRefexAbstractMember<?>> getNewLangRefexSet() {
		return newLangRefexSet;
	}



	public long getNewImportDate() {
		return newImportDate;
	}
	
	public void setNewImportDate(int year, int month, int date) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, date, 0, 0);  

		newImportDate = c.getTimeInMillis();
	}


}
