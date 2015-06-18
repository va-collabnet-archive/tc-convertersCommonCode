package gov.va.oia.econgen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.etypes.EConcept;

/**
 * Goal which loads an EConcept.jbin file into a bdb.
 *
 * @goal diff-econcepts
 *
 * @phase process-sources
 */

public class DiffEConMojo extends AbstractMojo {
	/**
	 * ConceptSpec for the view paths to base the export on.
	 *
	 * @parameter
	 */
	private File oldJBin;


	/**
	 * ConceptSpec for the view paths to base the export on.
	 *
	 * @parameter
	 */
	private File newJBin;

	private static final String DIR = "C:\\Users\\jefron\\Desktop\\DiffECon\\";
	private static final String conceptFILENAME = "conceptList.txt";
	private static final String diffFILENAME = "changedConcepts.txt";
	private static final String csFILENAME = "changedConcepts";

    private static Writer conceptWriter = null; 
    private static Writer diffWriter = null; 
    private static DataOutputStream csWriter = null; 

    private static final int CHANGE = 2;
    private static final int RETIRE = 1;
    private static final int NEW = 0;
    private static final String DIR_TYPE = "ALL";
//    private static final int CHANGECOUNT = 25;
    private static final Set<UUID> testCons = new HashSet<UUID>();

    DateFormat dateFormat = new SimpleDateFormat("MM-dd HH-mm-ss");
	private FileOutputStream csFile;


	private Date date;
    
    public void execute() throws MojoExecutionException {
    	// QUESTION: Retire time, not MyTime but Vesion, no?

    	
    	/*testCons.add(UUID.fromString("a0e27ae9-8438-31b2-9172-540f742fe13c")); // G New Desc on "Moderate (severity modifier) = moderately" 
    	testCons.add(UUID.fromString("54998f38-c876-37ff-89fb-9820cbad513f")); // G x Retire Desc "Visual impairment of both eyes" (two UUIDs)
    	
    	testCons.add(UUID.fromString("a25d0914-3dfc-3a11-bd61-03f3b604cf08")); // RETIRED STATED & INFERRED, (not just one )x Retire Rel "Keratometry Steep Power ***is*** Corneal curvature refracting power

    	testCons.add(UUID.fromString("b1db7fac-b952-3e56-8a78-923d5835bdf4")); // RETIRED COn & RELS, (what about NEW ISA) x Retiure Con "Secondary anemia (disorder)"
    	testCons.add(UUID.fromString("55e18a6d-14a6-3154-8cad-6a6098dc7fec")); // x (null) newConcept: Patient declines smoking cessation information (situation)

    	
    	
    	
    	testCons.add(UUID.fromString("0bc72bfd-5fbf-40c8-9c8f-f70454b298c3")); // New Rel on "Inadequate dietary intake of pantothenic acid ***is*** Inadequate pantothenic acid intake (finding)"
*/    	
        try {
        	setupOutput();

//	    	OldEConceptMaker oldMaker = new OldEConceptMaker();
//	    	NewEConceptMaker newMaker = new NewEConceptMaker();
//        	List<EConcept> oldList = oldMaker.createNewEConceptList();
//	    	List<EConcept> newList = newMaker.createNewEConceptList();
	    	
        	List<EConcept> oldList = readJBin(oldJBin);
        	List<EConcept> newList = readJBin(newJBin);
        	
	    	writeMakers(oldList, newList);
	    	
	    	Map<Integer, List<EConcept>> changesetList = diff(oldList, newList);
	    	
	    	writeDiff(changesetList);
        } catch (IOException | ClassNotFoundException e) {
        	e.printStackTrace();
        }
    }


	private List<EConcept> readJBin(File jbin) throws ClassNotFoundException, IOException {
		FileInputStream jbinFile = new FileInputStream (jbin);
		DataInput jbinReader = new DataInputStream(new BufferedInputStream(jbinFile));
		
		List<EConcept> inputList = new ArrayList<EConcept>();
		
        try {
            while (true) {
                EConcept eConcept = new EConcept(jbinReader);
            	if (testCons.isEmpty() || testCons.contains(eConcept.getPrimordialUuid())) {
            		inputList.add(eConcept);
            	}
            }
        } catch (EOFException e) {
        	jbinFile.close();
        }
		return inputList;
	}


	private void setupOutput() throws IOException {
        date = new Date();
//        String parentFolder = new String(DIR + "\\Analysis\\" + "\\" + dateFormat.format(date) + " - "+ DIR_TYPE + "-" + CHANGECOUNT + "\\");
        String parentFolder = new String(DIR + "\\Analysis\\" + "\\" + dateFormat.format(date) + " - "+ DIR_TYPE +  "\\");
        File f = new File(parentFolder);
        f.mkdirs();
                    
    	conceptWriter = new FileWriter(parentFolder + conceptFILENAME);
    	diffWriter = new FileWriter(parentFolder + diffFILENAME);		
    	csFile = new FileOutputStream(new File(parentFolder + csFILENAME + "-" + dateFormat.format(date) + ".eccs"));
    	csWriter = new DataOutputStream(new BufferedOutputStream(csFile));
                
	}


	private void writeDiff(Map<Integer, List<EConcept>> changesetList) throws IOException {
		int i = 1;
		String type = null;
		
		for (Integer key : changesetList.keySet()) {
			if (key == CHANGE) {
				type = "Changed ";
				diffWriter.write("\t\t\t**** CHANGED CONCEPTS ****");
			} else if (key == RETIRE) {
				type = "Retired ";
				diffWriter.write("\n\n\n\n\t\t\t**** RETIRED CONCEPTS ****");
			} else {
				type = "New ";
				diffWriter.write("\n\n\n\n\t\t\t**** NEW CONCEPTS ****");
			}
			
			i = 1;
			int count = 0;
			Long timestamp = new Long(System.currentTimeMillis());
			List<EConcept> list = changesetList.get(key);
			for (EConcept c : list) {
				try {
//					if (!c.getPrimordialUuid().equals(UUID.fromString("80521df5-239d-3a1f-b751-1bf8d71a671e"))) {
//						continue;
//					}
				
					diffWriter.write("\n\n\t\t\t---- " + type + "Concept #" + i++ + "   " + c.getPrimordialUuid() + " ----");
					diffWriter.write(c.toString());

					csWriter.writeLong(date.getTime());
					c.writeExternal(csWriter);
				} catch (Exception e) {
					System.out.println(c);
					throw e;
				}
				
			}

			
		}
		
		csWriter.close();
		diffWriter.close();
	}


	private void writeMakers(List<EConcept> oldList, List<EConcept> newList) throws IOException {
		conceptWriter.write("\t\t\t**** OLD LIST ****");
		int i = 1;
		for (EConcept c : oldList) {
			conceptWriter.write("\n\n\t\t\t---- Old Concept #" + i++ + "   " + c.getPrimordialUuid() + " ----");
			conceptWriter.write(c.toString());
		}
		
		conceptWriter.write("\n\n\n\n\t\t\t**** NEW LIST ****");
		i = 1;
		for (EConcept c : newList) {
			conceptWriter.write("\n\n\t\t\t---- New Concept #" + i++ + "   " + c.getPrimordialUuid() + " ----");
			conceptWriter.write(c.toString());
		}
		
		conceptWriter.close();
	}


	private Map<Integer, List<EConcept>> diff(List<EConcept> oldList, List<EConcept> newList) {
		Map<Integer, List<EConcept>> changesetList = new HashMap<Integer, List<EConcept>>();
		
    	Set<UUID> matchedSet = new HashSet<UUID>();

    	List<EConcept> newSet = new ArrayList<EConcept>();
    	List<EConcept> newSet2 = new ArrayList<EConcept>();
    	List<EConcept> newSet3 = new ArrayList<EConcept>();
    	changesetList.put(RETIRE, newSet2);
    	changesetList.put(NEW, newSet3);
    	changesetList.put(CHANGE, newSet);

    	int count = 0;
    	// Find existing
   	for (EConcept oldCon : oldList) {
//    		if (!oldCon.getPrimordialUuid().equals(UUID.fromString("dc1f1e05-f107-3039-98ae-fa25e4a93868"))) {
//    			continue;
//    		}
    		for (EConcept newCon : newList) {
    			if (oldCon.getPrimordialUuid().equals(newCon.getPrimordialUuid())) {
    				count++;
    		    	EConcept diffCon = EConDiffer.diff(oldCon, newCon);
//    		    	if (count < CHANGECOUNT && diffCon != null) {
    		    	if (diffCon != null) {
	    		    	changesetList.get(CHANGE).add(diffCon); 
    		    	}
    		    	matchedSet.add(oldCon.getPrimordialUuid());
    		    	break;
    			}
    		}
    	}


	
	    	// Retire oldCons not in oldList
	    	// Note: SNOMED CT should handle this itself... so really just for other terms
	    	EConRetirer retireUtil = new EConRetirer();
	    	
	    	for (EConcept oldCon : oldList) {
	    		if (!matchedSet.contains(oldCon.getPrimordialUuid())) {
	    			retireUtil.retireCon(oldCon);
			    	changesetList.get(RETIRE).add(oldCon);
	    		}
	    	}
	    	
	    	// Add newCons not in newList
	    	for (EConcept newCon : newList) {
	    		if (!matchedSet.contains(newCon.getPrimordialUuid())) {
			    	changesetList.get(NEW).add(newCon);
	    		}
	    	}
    
		return changesetList;
	}
}