package gov.va.oia.econgen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

public class OldEConceptMaker extends EConceptMaker {
    protected long myTime = 2234567890L;

	public OldEConceptMaker() {
		
	}
	
	List<EConcept> createNewEConceptList() {
		List<EConcept> newConList = new ArrayList<EConcept>();
		
		EConcept testConcept = createNewEConcept(UUID.fromString("f5daba09-7feb-37f0-8d6d-c3cadfc7f724"));
		EConcept testConcept2 = createNewEConcept(UUID.fromString("c5daba09-7feb-37f0-8d6d-c3cadfc7f724"));
		
		newConList.add(testConcept);
		newConList.add(testConcept2);
		
	    return newConList;
	}

	EConcept createNewEConcept(UUID id) {
	    EConcept testConcept = new EConcept();
	    testConcept.primordialUuid = id;
	    testConcept.conceptAttributes = new EConceptAttributes();
	    testConcept.conceptAttributes.primordialUuid = id;
	    
	    testConcept.annotationIndexStyleRefex = false;
	    testConcept.annotationStyleRefex = false;
	    
	    testConcept.descriptions = new ArrayList<TkDescription>();
	    testConcept.relationships = new ArrayList<TkRelationship>();

	    addOtherConAttr(testConcept.conceptAttributes);
	    addDescs(testConcept);
	    addRels(testConcept);
	    testConcept.refsetMembers = addRefsets(id, OLD);

	    return testConcept;
	}


	private void addOtherConAttr(TkConceptAttributes conceptAttributes) {
        conceptAttributes.defined = false;
        conceptAttributes.revisions = null;
        conceptAttributes.additionalIds = null;
        conceptAttributes.pathUuid = new UUID(4, 5);
        conceptAttributes.statusUuid = new UUID(8, 9);
        conceptAttributes.time = myTime;
	   
        conceptAttributes.additionalIds = addAdditionatlIds(1, 2);
        conceptAttributes.annotations = addRefsets(conceptAttributes.getPrimordialComponentUuid(), OLD);
	}

    
	private void addDescs(EConcept testConcept) {
    	EDescription desc = new EDescription();
        desc.primordialUuid = UUID.fromString("ffdaba09-7feb-37f0-8d6d-c3cadfc7f724");

        desc.conceptUuid  = testConcept.getPrimordialUuid();
        
        desc.initialCaseSignificant = false;
        desc.lang = "en";
        desc.text = "hello world";          
        desc.typeUuid = new UUID(13, 14);
        desc.pathUuid = new UUID(4, 5);
        desc.statusUuid = new UUID(8, 9);
        desc.time = myTime;

        desc.additionalIds = addAdditionatlIds(1, 2);
        desc.annotations = addRefsets(desc.getPrimordialComponentUuid(), OLD);

        /*
        desc.revisions = new ArrayList<TkDescriptionRevision>();
        EDescriptionRevision edv = new EDescriptionRevision();
        edv.initialCaseSignificant = true;
        edv.lang = "en-uk";
        edv.text = "hello world 2";
        edv.typeUuid  = new UUID(13, 14);
        edv.pathUuid = new UUID(4, 5);
        edv.statusUuid = new UUID(8, 9);
        edv.time = myTime;
        desc.revisions.add(edv);
        */
      
        testConcept.descriptions.add(desc);
	}
	
	private void addRels(EConcept testConcept) {
	    ERelationship rel = new ERelationship();
	    rel.c1Uuid = testConcept.getPrimordialUuid();
	    rel.c2Uuid = new UUID(41, 52);
	    rel.characteristicUuid = new UUID(42, 53);
	    rel.refinabilityUuid = new UUID(43, 54);
	    rel.relGroup = 22; 
	    rel.typeUuid = new UUID(44, 55);
	    rel.pathUuid = new UUID(45, 56);
	    rel.statusUuid = new UUID(86, 97);
	    rel.time = myTime;
	    rel.primordialUuid = new UUID(20, 30);
	    rel.primordialUuid = UUID.fromString("fffaba09-7feb-37f0-8d6d-c3cadfc7f724");
        
        rel.additionalIds = addAdditionatlIds(1, 2);
        rel.annotations = addRefsets(rel.getPrimordialComponentUuid(), OLD);

        /*
	    rel.revisions = new ArrayList<TkRelationshipRevision>();
	    ERelationshipRevision erv = new ERelationshipRevision();
	    erv.characteristicUuid  = new UUID(861, 947);
	    erv.refinabilityUuid  = new UUID(586, 937);
	    erv.group = 3; 
	    erv.typeUuid  = new UUID(846, 957);
	    erv.pathUuid = new UUID(425, 526);
	    erv.statusUuid = new UUID(846, 967);
	    erv.time = myTime;
	    rel.revisions.add(erv);
	    */
	    testConcept.relationships.add(rel);
	}

}
