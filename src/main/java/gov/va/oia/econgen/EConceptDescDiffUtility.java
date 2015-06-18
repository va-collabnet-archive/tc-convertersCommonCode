package gov.va.oia.econgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

public class EConceptDescDiffUtility extends EConceptDiffUtility implements EConceptDiffI {
	public Object diff(Object oldComps, Object newComps) {
		try {
			if ((oldComps != null) && (newComps != null)) {
				List<TkDescription> oldDescs = (List<TkDescription>)oldComps;
				List<TkDescription> newDescs = (List<TkDescription>)newComps;
				
				List<TkDescription> diffDescs = new ArrayList<TkDescription>();
				
				// Handle Retired Descs (don't need to retire the descs' descs I think)
				diffDescs.addAll(retireDescs(oldDescs, newDescs));
				
				// Handle New Descs (don't need to do more than add I think)
				diffDescs.addAll(createDescs(oldDescs, newDescs));
				
				diffDescs.addAll(updateExisting(oldDescs, newDescs));
	
				return diffDescs;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	private List<TkDescription> retireDescs(List<TkDescription> oldDescs, List<TkDescription> newDescs) {
		List<TkDescription> retiredList = new ArrayList<TkDescription>();

		for (TkDescription oldDesc : oldDescs) {
			boolean matchFound = false;

			for (TkDescription newDesc : newDescs) {
				if (oldDesc.getPrimordialComponentUuid().equals(newDesc.getPrimordialComponentUuid())) {
					matchFound = true;
					break;
				}
			}

			if (!matchFound) {
				TkDescription retDesc = retireDesc(oldDesc);
				retiredList.add(retDesc);
				conceptChangeFound = true;
			}
		}

		return retiredList;
	}

	private List<TkDescription> createDescs(List<TkDescription> oldDescs, List<TkDescription> newDescs) {
		
		List<TkDescription> diffDescs = new ArrayList<TkDescription>();
		
		for (TkDescription newDesc : newDescs) {
			
			boolean matchFound = false;
			for (TkDescription oldDesc : oldDescs) {
				if (newDesc.getPrimordialComponentUuid().equals(oldDesc.getPrimordialComponentUuid())) {
					matchFound = true;
					break;
				}
			}
			
			if (!matchFound) {
				conceptChangeFound = true;
				diffDescs.add(newDesc);
			}
		}		
		
		return diffDescs;
	}

	private Collection<? extends TkDescription> updateExisting(
			List<TkDescription> oldDescs, List<TkDescription> newDescs) {
		List<TkDescription> diffDescs = new ArrayList<TkDescription>();

		for (TkDescription newDesc : newDescs) {
			for (TkDescription oldDesc : oldDescs) {
				if (newDesc.getPrimordialComponentUuid().equals(oldDesc.getPrimordialComponentUuid())) {
					if (newDesc.getPrimordialComponentUuid().equals(UUID.fromString("745a3d40-b679-3182-b4e1-a44459c26052"))) {
						int a = 1;
					}
					TkDescriptionRevision rev = new TkDescriptionRevision();
					boolean changeFound = false;

					if ((oldDesc.initialCaseSignificant != newDesc.initialCaseSignificant) ||
						(!oldDesc.lang.equals(newDesc.lang)) ||
						(!oldDesc.text.equals(newDesc.text)) ||
						(!oldDesc.typeUuid.equals(newDesc.typeUuid)) ||
						(!oldDesc.statusUuid.equals(newDesc.statusUuid)) ||
						(!oldDesc.authorUuid.equals(newDesc.authorUuid)) ||
						(!oldDesc.moduleUuid.equals(newDesc.moduleUuid)) ||
						(!oldDesc.pathUuid.equals(newDesc.pathUuid))) 
					{
						if (!oldDesc.getStatusUuid().equals(newDesc.getStatusUuid()) && 
							 newDesc.getStatusUuid().equals(inactiveStatus)) {
							TkDescription retDesc = retireDesc(oldDesc);
							diffDescs.add(retDesc);
							conceptChangeFound = true;
						} else {
							rev.setLang(newDesc.lang);
							rev.initialCaseSignificant = newDesc.initialCaseSignificant;
							rev.setText(newDesc.text);
							rev.setTypeUuid(newDesc.typeUuid);
							
							rev.statusUuid = newDesc.statusUuid;
							rev.time = newDesc.time;
							rev.authorUuid = newDesc.authorUuid;
							rev.moduleUuid = newDesc.moduleUuid;
							rev.pathUuid = newDesc.pathUuid;
	
							if (oldDesc.getRevisionList() == null) {
								oldDesc.revisions = new ArrayList<TkDescriptionRevision>(); 
							}
	
							oldDesc.getRevisionList().add(rev);
	
							changeFound = true;
							conceptChangeFound = true;
						}
						
	
						List<TkIdentifier> ids = handleIds(oldDesc.additionalIds, newDesc.additionalIds);
						if (ids.size() > 0) {
							oldDesc.setAdditionalIdComponents(ids);
						}
						
						List<TkRefexAbstractMember<?>> refsets = handleRefsets(oldDesc.annotations, newDesc.annotations);
						if (refsets.size() > 0) {
							oldDesc.annotations = refsets;
						}
	
						if (ids.size() > 0 || refsets.size() > 0 || changeFound) {
							diffDescs.add(oldDesc);
						}
					}
					
					break;
				}
			}
		}	
		
		return diffDescs;
	}

}
