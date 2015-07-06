package gov.va.oia.econgen;

import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;

public class EConceptAttrDiffUtility extends EConceptDiffUtility implements EConceptDiffI {
	
	public Object diff(Object oldComps, Object newComps) {
		TkConceptAttributes oldAttr = (TkConceptAttributes)oldComps;
		TkConceptAttributes newAttr = (TkConceptAttributes)newComps;
		
		try {
			// create component
			if ((oldAttr != null) && (newAttr != null)) {
				TkConceptAttributesRevision rev = new TkConceptAttributesRevision();
				boolean changeFound = false;

				if ((oldAttr.defined != newAttr.defined) ||
					(!oldAttr.statusUuid.equals(newAttr.statusUuid)) ||
					(!oldAttr.moduleUuid.equals(newAttr.moduleUuid)) ||
					(!oldAttr.authorUuid.equals(newAttr.authorUuid)) ||
					(!oldAttr.pathUuid.equals(newAttr.pathUuid)))
				{
					rev.defined = newAttr.defined;

					rev.statusUuid = newAttr.statusUuid;
					rev.time = newAttr.time;
					rev.authorUuid = newAttr.authorUuid;
					rev.moduleUuid = newAttr.moduleUuid;
					rev.pathUuid = newAttr.pathUuid;

					if (oldAttr.getRevisionList() == null) {
						oldAttr.revisions = new ArrayList<TkConceptAttributesRevision>(); 
					}
					
					oldAttr.getRevisionList().add(rev);
					changeFound = true;
					conceptChangeFound = true;
				}
				
				List<TkIdentifier> ids = handleIds(oldAttr.additionalIds, newAttr.additionalIds);
				if (ids.size() > 0) {
					oldAttr.setAdditionalIdComponents(ids);
				}
				
				List<TkRefexAbstractMember<?>> refsets = handleRefsets(oldAttr.annotations, newAttr.annotations);
				if (refsets.size() > 0) {
					oldAttr.annotations = refsets;
				}

				if (ids.size() > 0 || refsets.size() > 0 || changeFound) {
					return oldAttr;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
