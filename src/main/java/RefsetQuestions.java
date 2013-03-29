import java.io.IOException;
import java.util.UUID;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;

public class RefsetQuestions
{
    public static void main(String[] args) throws IOException, TerminologyException
    {
        /**
         * Refset Member Type 
         */
        {
            //Assume these concepts are properly populated and built
            EConcept contentConcept = new EConcept();
            EConcept refsetContainerConcept = new EConcept();
            
            TkRefexUuidMember refsetMember = new TkRefexUuidMember();
            refsetMember.setPrimordialComponentUuid(UUID.randomUUID());
            refsetMember.setComponentUuid(contentConcept.getPrimordialUuid());  
            refsetMember.setRefsetUuid(refsetContainerConcept.getPrimordialUuid());  
            refsetMember.setUuid1(RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid());
            // Set all the refsetMember stamp attributes here
            refsetContainerConcept.getRefsetMembers().add(refsetMember);
            
            /**
             * Above is what I do today - and it works as far as I know.  
             * 
             * QUESTION:
             * If I followed the e-mail answers properly, I should also be doing this?
             */
            
            contentConcept.setAnnotationIndexStyleRefex(true);
            
            /**
             * QUESTION:
             * I'm not really sure how to do refset to annotation conversion...
             */
            //TkConcept.convertRefex(??);
            
        }
 
        /**
         *************************************************************************************************
         */
        
        /**
         * Refset Annotation Type
         * This works fine when browsing the contentConcept - but I can't seem to browse from the refsetContainerConcept back to the contentConcept.
         * 
         * QUESTION:
         * Unsure if I am supposed to be able to or not?
         */
        {
            //Assume these are properly populated and built
            EConcept contentConcept = new EConcept();
            EConcept refsetContainerConcept = new EConcept();
        
            TkRefexUuidMember conceptRefexMember = new TkRefexUuidMember();
            conceptRefexMember.setComponentUuid(contentConcept.getConceptAttributes().getPrimordialComponentUuid());
            conceptRefexMember.setPrimordialComponentUuid(UUID.randomUUID());
            conceptRefexMember.setUuid1(RefsetAuxiliary.Concept.NORMAL_MEMBER.getPrimoridalUid());
            conceptRefexMember.setRefsetUuid(refsetContainerConcept.getPrimordialUuid());
            // Set all the refsetMember stamp attributes here
            contentConcept.getConceptAttributes().getAnnotations().add(conceptRefexMember);
            
            /**
             * Above is what I do today - If I followed the e-mail answers properly, I should also be doing this?
             */
            
            contentConcept.setAnnotationIndexStyleRefex(true);
            
            /**
             * QUESTION:
             * I'm not really sure how to do refset to annotation conversion...
             */
            
            //TkConcept.convertRefex(??)
        }
    }
}
