package gov.va.oia.econgen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierLong;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;

public class EConceptMaker {
	static final int NEW = 0;
	static final int OLD = 0;


	 List<TkIdentifier> addAdditionatlIds(int id, int id2) {
		 List<TkIdentifier> additionalIds = new ArrayList<TkIdentifier>();
		additionalIds.add(makeId(id));
		additionalIds.add(makeId(id2));
		
		return additionalIds;		
	}



	 TkIdentifierLong makeId(int id) {
        TkIdentifierLong retId = null;
		retId = new TkIdentifierLong();
		retId.setDenotation((long)id);
//			retId.setAuthorityNid(id);
//			retId.setStatusAtPositionNid(id);
        
        return retId; 
    }
	
	 List<TkRefexAbstractMember<?>> addRefsets(UUID refComp, int useCase) {
		List<TkRefexAbstractMember<?>> members = new ArrayList<TkRefexAbstractMember<?>>();
		 
		TkRefexIntMember refsetMember = new TkRefexIntMember();
		refsetMember.setPrimordialComponentUuid(new UUID(88, 8888));
		refsetMember.setComponentUuid(refComp);  // ComponentUuid and refsetUuid seem like they are reversed at first glance, but this is right.
		refsetMember.setRefsetUuid(new UUID(8888, 888888));
		refsetMember.setInt1(8888);
		
		members.add(refsetMember);

		TkRefsetStrMember refsetMember2 = new TkRefsetStrMember();
		if (useCase == OLD) {
			refsetMember2.setPrimordialComponentUuid(new UUID(99, 9999));
		} else {
			refsetMember2.setPrimordialComponentUuid(new UUID(90, 9900));
		}
		refsetMember2.setComponentUuid(refComp);  // ComponentUuid and refsetUuid seem like they are reversed at first glance, but this is right.
		refsetMember2.setRefsetUuid(new UUID(9999, 999999));
		
		if (useCase == OLD) {
			refsetMember2.setString1("AAA");
		} else {
			refsetMember2.setString1("BBB");
		}
		
		members.add(refsetMember2);
		
		return members;
	 }
	 
	 
		/*	private void addRefsets(EConcept testConcept) {
	    testConcept.refsetMembers = new ArrayList<TkRefexAbstractMember<?>>();
	    ERefsetCidIntMember cidIntMember = new ERefsetCidIntMember();
	    cidIntMember.uuid1 = new UUID(4386, 5497);
	    cidIntMember.int1 = 33;
	    cidIntMember.refsetUuid = new UUID(14386, 65497);
	    cidIntMember.componentUuid = new UUID(64386, 75497);
	    cidIntMember.pathUuid = new UUID(4350, 5469);
	    cidIntMember.statusUuid = new UUID(5386, 4497);
	    cidIntMember.time = this.myTime;
	    cidIntMember.primordialUuid = new UUID(320, 230);
	    testConcept.refsetMembers.add(cidIntMember);
	    cidIntMember.revisions = new ArrayList<TkRefexUuidIntRevision>();
	    ERefsetCidIntRevision rciv = new ERefsetCidIntRevision();
	    rciv.uuid1 = new UUID(114386, 656497);
	    rciv.int1 = 99;
	    rciv.pathUuid = new UUID(4350, 5469);
	    rciv.statusUuid = new UUID(5386, 4497);
	    rciv.time = this.myTime;
	    cidIntMember.revisions.add(rciv);
	}
*/

	 
	 
}
