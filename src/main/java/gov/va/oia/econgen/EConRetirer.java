package gov.va.oia.econgen;

import java.util.ArrayList;

import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

public class EConRetirer extends EConceptChangeSetUtility {

	public void retireCon(EConcept oldCon) {
		try {
		oldCon.getConceptAttributes().revisions = new ArrayList<TkConceptAttributesRevision>();
		TkConceptAttributesRevision attrRev = new TkConceptAttributesRevision();

		attrRev.statusUuid = inactiveStatus;
		attrRev.time = importTime;
		attrRev.authorUuid = oldCon.conceptAttributes.authorUuid;
		attrRev.moduleUuid = oldCon.conceptAttributes.moduleUuid;
		attrRev.pathUuid = oldCon.conceptAttributes.pathUuid;
		
		oldCon.getConceptAttributes().revisions.add(attrRev);
		
		retireRefsets(oldCon.getConceptAttributes().annotations);
		
		for (TkDescription desc : oldCon.getDescriptions()) {
			retireDesc(desc);
		}
			
		for (TkRelationship desc : oldCon.getRelationships()) {
			retireRel(desc);
		}
		} catch (Exception e) {
			int a = 2;
		}
	}

	
}
