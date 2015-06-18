package gov.va.oia.econgen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

class EConceptDiffUtility extends EConceptChangeSetUtility {
	UUID inactiveStatus = UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724");

	static boolean conceptChangeFound = false;
	
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
						TkRefexAbstractMember<?> diffRefset = newMember;
						diffRefset.annotations = handleRefsets(oldMember.annotations, newMember.annotations);
					}
				}
			}		
		}
	}
	
	private void newMembers(List<TkRefexAbstractMember<?>> oldRefsets,
			List<TkRefexAbstractMember<?>> newRefsets,
			List<TkRefexAbstractMember<?>> diffRefsets) {
		
		if (newRefsets != null) {
			if (oldRefsets == null) {
				diffRefsets.addAll(newRefsets);
			} else {
				for (TkRefexAbstractMember<?> newComp : newRefsets) {
					
					boolean matchFound = false;
					for (TkRefexAbstractMember<?> oldComp : oldRefsets) {
						if (!newComp.getPrimordialComponentUuid().equals(oldComp.getPrimordialComponentUuid())) {
							matchFound = true;
							break;
						}
					}
					
					if (!matchFound) {
						conceptChangeFound = true;
						diffRefsets.add(newComp);
					}
				}		
			}
		}
	}

	private void retireRefsets(List<TkRefexAbstractMember<?>> oldRefsets,
			List<TkRefexAbstractMember<?>> newRefsets,
			List<TkRefexAbstractMember<?>> diffRefsets) {
		List<TkRefexAbstractMember<?>> retRefsets = getRetiredRefsets(oldRefsets, newRefsets);

		retireRefsets(retRefsets);
		
		diffRefsets.addAll(retRefsets);
	}

	private List<TkRefexAbstractMember<?>> getRetiredRefsets(List<TkRefexAbstractMember<?>> oldCompList, List<TkRefexAbstractMember<?>> newCompList) {
		List<TkRefexAbstractMember<?>> retiredList = new ArrayList<TkRefexAbstractMember<?>>();
		
		boolean matchFound = false;
		
		if (oldCompList != null) {
			for (TkRefexAbstractMember<?> oldComp : oldCompList) {
				if (newCompList == null) {
					retiredList.addAll(oldCompList);
				} else {
					for (TkRefexAbstractMember<?> newComp : newCompList) {
						if (oldComp.getPrimordialComponentUuid().equals(newComp.getPrimordialComponentUuid())) {
							matchFound = true;
							break;
						}
					}
		
					if (!matchFound) {
						conceptChangeFound = true;
						retiredList.add(oldComp);
					}
				}
			}
		}
		
		return retiredList;
	}

}
