package gov.va.oia.econgen;

import java.util.List;
import java.util.UUID;

import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

public class EConDiffer {
	static EConceptAttrDiffUtility attrUtil = new EConceptAttrDiffUtility();
	static EConceptDescDiffUtility descUtil = new EConceptDescDiffUtility();
	static EConceptRelDiffUtility relUtil = new EConceptRelDiffUtility();
	static EConceptRelDiffUtility util = new EConceptRelDiffUtility();
	
	// Only tests changes within a con... all compTypes modified 
	// No New and no Retired
	public static EConcept diff(EConcept oldCon, EConcept newCon) {
		EConcept diffCon = new EConcept();
		
		if (oldCon.getPrimordialUuid().equals(newCon.getPrimordialUuid())) {
			if (oldCon.getPrimordialUuid().equals(UUID.fromString("80521df5-239d-3a1f-b751-1bf8d71a671e"))) {
				int a = 3;
			}
			EConceptDiffUtility.conceptChangeFound = false;
			
			diffCon.setPrimordialUuid(oldCon.getPrimordialUuid());

			if (oldCon.annotationIndexStyleRefex != newCon.annotationIndexStyleRefex) {
				diffCon.annotationIndexStyleRefex = newCon.annotationIndexStyleRefex;
				EConceptDiffUtility.conceptChangeFound = true;
			}
			
			if (oldCon.annotationStyleRefex != newCon.annotationStyleRefex) {
				diffCon.annotationStyleRefex = newCon.annotationStyleRefex;
				EConceptDiffUtility.conceptChangeFound = true;
			}
		
			diffCon.conceptAttributes = (TkConceptAttributes)attrUtil.diff(oldCon.conceptAttributes, newCon.conceptAttributes);
			diffCon.descriptions = (List<TkDescription>)descUtil.diff(oldCon.descriptions, newCon.descriptions);
			diffCon.relationships = (List<TkRelationship>)relUtil.diff(oldCon.relationships, newCon.relationships);
			diffCon.refsetMembers = util.handleRefsets(oldCon.refsetMembers, newCon.refsetMembers);
		}
		
		if (EConceptDiffUtility.conceptChangeFound == false) {
			return null;
		}
		return diffCon;
	}
}
