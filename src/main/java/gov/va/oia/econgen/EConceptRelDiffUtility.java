package gov.va.oia.econgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

public class EConceptRelDiffUtility extends EConceptDiffUtility implements EConceptDiffI{
//	private final UUID  INFERRED_RELATIONSHIP = UUID.fromString("1290e6ba-48d0-31d2-8d62-e133373c63f5");

	public Object diff(Object oldComps, Object newComps) {
		try {
			if ((oldComps != null) && (newComps != null)) {
				List<TkRelationship> oldRels = (List<TkRelationship>)oldComps;
				List<TkRelationship> newRels = (List<TkRelationship>)newComps;
				
				List<TkRelationship> diffRels = new ArrayList<TkRelationship>();
		
				// Handle Retired Rels (don't need to retire the rels' rels I think)
				diffRels.addAll(retireRels(oldRels, newRels));
				
				// Handle New Rels (don't need to do more than add I think)
				diffRels.addAll(createRels(oldRels, newRels));
				
				diffRels.addAll(updateExisting(oldRels, newRels));
	
				return diffRels;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private List<TkRelationship> retireRels(List<TkRelationship> oldRels, List<TkRelationship> newRels) {
		List<TkRelationship> retiredList = new ArrayList<TkRelationship>();

		
		for (TkRelationship oldRel : oldRels) {
			boolean matchFound = false;
					
			for (TkRelationship newRel : newRels) {
				if (oldRel.getPrimordialComponentUuid().equals(newRel.getPrimordialComponentUuid())) {
					matchFound = true;
					break;
				}
			}

			if (!matchFound) {
				TkRelationship retRel = retireRel(oldRel);
				retiredList.add(retRel);
				conceptChangeFound = true;
			}
		}
		
		return retiredList;
	}

	private List<TkRelationship> createRels(List<TkRelationship> oldRels, List<TkRelationship> newRels) {
		
		List<TkRelationship> diffRels = new ArrayList<TkRelationship>();
		for (TkRelationship newRel : newRels) {

			boolean matchFound = false;
			for (TkRelationship oldRel : oldRels) {
				if (newRel.getPrimordialComponentUuid().equals(oldRel.getPrimordialComponentUuid())) {
					matchFound = true;
					break;
				}
			}
			
			if (!matchFound) {
				diffRels.add(newRel);
				conceptChangeFound = true;
			}
		}		
		
		return diffRels;
	}

	private Collection<? extends TkRelationship> updateExisting(
			List<TkRelationship> oldRels, List<TkRelationship> newRels) {
		List<TkRelationship> diffRels = new ArrayList<TkRelationship>();

		for (TkRelationship newRel : newRels) {
			for (TkRelationship oldRel : oldRels) {
				if (newRel.getPrimordialComponentUuid().equals(oldRel.getPrimordialComponentUuid())) {
					TkRelationshipRevision rev = new TkRelationshipRevision();

					boolean changeFound = false;
					if ((!oldRel.getCharacteristicUuid().equals(newRel.getCharacteristicUuid())) ||
					    (!oldRel.getRefinabilityUuid().equals(newRel.getRefinabilityUuid())) ||
					    (!oldRel.getTypeUuid().equals(newRel.getTypeUuid())) ||
					    (oldRel.getRelationshipGroup() != newRel.getRelationshipGroup()) ||
				    	(!oldRel.getStatusUuid().equals(newRel.getStatusUuid())) ||
					    (!oldRel.getAuthorUuid().equals(newRel.getAuthorUuid())) ||
					    (!oldRel.getModuleUuid().equals(newRel.getModuleUuid())) ||
					    (!oldRel.getPathUuid().equals(newRel.getPathUuid()))) 
		    		{
						if (!oldRel.getStatusUuid().equals(newRel.getStatusUuid()) &&
							 newRel.getStatusUuid().equals(inactiveStatus)) {
							TkRelationship retRel = retireRel(oldRel);
							conceptChangeFound = true;
							diffRels.add(retRel);
						} else {
							rev.setCharacteristicUuid(newRel.getCharacteristicUuid());
							rev.setRefinabilityUuid(newRel.getRefinabilityUuid());
							rev.setTypeUuid(newRel.getTypeUuid());
							rev.setRelGroup(newRel.getRelationshipGroup());
	
							rev.statusUuid = newRel.statusUuid;
							rev.time = newRel.time;
							rev.authorUuid = newRel.authorUuid;
							rev.moduleUuid = newRel.moduleUuid;
							rev.pathUuid = newRel.pathUuid;
	
							if (oldRel.getRevisionList() == null) {
								oldRel.revisions = new ArrayList<TkRelationshipRevision>(); 
							}
		
							oldRel.getRevisionList().add(rev);
	
							changeFound  = true;
							conceptChangeFound = true;
	
							List<TkIdentifier> ids = handleIds(oldRel.additionalIds, newRel.additionalIds);
							if (ids.size() > 0) {
								oldRel.setAdditionalIdComponents(ids);
							}
		
							List<TkRefexAbstractMember<?>> refsets = handleRefsets(oldRel.annotations, newRel.annotations);
							if (refsets.size() > 0) {
								oldRel.annotations = refsets;
							}
		
							
							if (ids.size() > 0 || refsets.size() > 0 || changeFound) {
								diffRels.add(oldRel);
							}
			    		}
		    		}
					
					break;
				}
			}
		}	
		
		return diffRels;
	}

}
