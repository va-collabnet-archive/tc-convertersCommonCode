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
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

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

    private static EConceptAttrDiffUtility attrUtil = new EConceptAttrDiffUtility();
    private static EConceptDescDiffUtility descUtil = new EConceptDescDiffUtility();
    private static EConceptRelDiffUtility relUtil = new EConceptRelDiffUtility();
    private static EConceptRelDiffUtility util = new EConceptRelDiffUtility();
	
    DateFormat dateFormat = new SimpleDateFormat("MM-dd HH-mm-ss");
	private FileOutputStream csFile;


	private Date date;
    
    public void execute() throws MojoExecutionException {
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
        		inputList.add(eConcept);
            }
        } catch (EOFException e) {
        	jbinFile.close();
        }
		return inputList;
	}


	private void setupOutput() throws IOException {
        date = new Date();
        String parentFolder = new String(DIR + "\\Analysis\\" + "\\" + dateFormat.format(date) + "-ALL\\");
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
			i = 1;
			List<EConcept> list = changesetList.get(key);

			if (key == CHANGE) {
				type = "CHANGED ";
			} else if (key == RETIRE) {
				type = "RETIRED ";
			} else {
				type = "NEW ";
			}
			diffWriter.write("\t\t\t**** " + type + " CONCEPTS ****");

			for (EConcept c : list) {
				try {
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

    	// Find existing
	   	for (EConcept oldCon : oldList) {
    		for (EConcept newCon : newList) {
    			if (oldCon.getPrimordialUuid().equals(newCon.getPrimordialUuid())) {
    		    	EConcept diffCon = diff(oldCon, newCon);
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
		EConceptDiffUtility retireUtil = new EConceptDiffUtility();
		
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

	private static EConcept diff(EConcept oldCon, EConcept newCon) {
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
