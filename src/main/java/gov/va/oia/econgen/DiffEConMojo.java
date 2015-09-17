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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_member.TkRefexRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid_int.TkRefexUuidIntRevision;
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
	 *
	 * @parameter
	 * @required
	 */
	private File oldJBin;


	/**
	 *
	 * @parameter
	 * @required
	 */
	private File newJBin;
	
	/**
	 *
	 * @parameter
	 * @required
	 */
	private File sctCoreJBin;
	/**
	 *
	 * @parameter
	 * @required
	 */
	private String outputDirPath;


	
	private static final String conceptFILENAME = "conceptList.txt";
	private static final String diffFILENAME = "changedConcepts.txt";
	private static final String missingRefsetComponentFILENAME = "missingRefsetComponent.txt";
	private static final String componentFILENAME = "components";
	private static final String refsetFILENAME = "refsets";

    private static Writer conceptWriter = null; 
    private static Writer diffWriter = null; 
    private static DataOutputStream componentCSWriter = null; 
    private static DataOutputStream refsetCSWriter = null; 
    private static Writer missingRefsetComponentWriter = null; 

    
    private static EConceptAttrDiffUtility attrUtil = new EConceptAttrDiffUtility();
    private static EConceptDescDiffUtility descUtil = new EConceptDescDiffUtility();
    private static EConceptRelDiffUtility relUtil = new EConceptRelDiffUtility();
    private static EConceptRelDiffUtility util = new EConceptRelDiffUtility();


	private static List<EConcept> consWithRefsetList = new ArrayList<EConcept>();
	private static Set<UUID> seenComponentList = new HashSet<UUID>();
	
    DateFormat dateFormat = new SimpleDateFormat("MM-dd HH-mm-ss");
	private List<EConcept> addedConcepts = new ArrayList<EConcept>();
	private List<EConcept> retiredConcepts = new ArrayList<EConcept>();
	private List<EConcept> changedConcepts = new ArrayList<EConcept>();


	private List<EConcept> previousEConceptList;
	private List<EConcept> currentEConceptList;
	private Date date;


	private final int IMPORT_YEAR = 2015;
	private final int IMPORT_MONTH = 9;
	private final int IMPORT_DATE = 31;


	private EConceptDiffUtility retireUtil;
    
    public void execute() throws MojoExecutionException {
    	setup();
    	
    	identifyChanges(previousEConceptList, currentEConceptList);
    	
    	try {
			writeChangeSet(addedConcepts, componentCSWriter, "ADDED");
	    	writeChangeSet(retiredConcepts, componentCSWriter, "RETIRED");
	    	writeChangeSet(changedConcepts, componentCSWriter, "CHANGED");
    	
			trimRefsetList();
	    	writeChangeSet(consWithRefsetList, refsetCSWriter, "REFSET MEMBERS");

	    	refsetCSWriter.close();
			componentCSWriter.close();
			diffWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
			
    	
    }

	private void trimRefsetList() throws IOException {
		if (sctCoreJBin != null) {
			try {
				List<EConcept> sctCoreList = readJBin(sctCoreJBin);
				
				for (EConcept c : sctCoreList) {
					updateSeenComponentList(c, c);
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		int i = 1;
		for (EConcept c : consWithRefsetList) {
			System.out.println("\nStart with Size: " + c.getRefsetMembers().size());
			Iterator<TkRefexAbstractMember<?>> iter = c.getRefsetMembers().iterator();
			
			while (iter.hasNext()) {
				boolean remove = false;
				TkRefexAbstractMember<?> rm = iter.next();
				
				try {
					if (!seenComponentList.contains(rm.getComponentUuid())) {
						missingRefsetComponentWriter.write("\n" + i + ": Removing CID with member UUID: " + rm.getPrimordialComponentUuid() + " due to missing Reference Component " + (rm.getComponentUuid()));
						remove  = true;
					}

					switch (rm.getType()) {
						case MEMBER: 
							if (!seenComponentList.contains(((TkRefexUuidMember)rm).getUuid1())) {
								missingRefsetComponentWriter.write("\n" + i + ": Removing MEMBER with member UUID: " + rm.getPrimordialComponentUuid() + " due to missing " + ((TkRefexUuidMember)rm).getUuid1());
								remove = true;
							}
							break;
						
						case CID: 
							if (!seenComponentList.contains(((TkRefexUuidMember)rm).getUuid1())) {
								missingRefsetComponentWriter.write("\n" + i + ": Removing CID with member UUID: " + rm.getPrimordialComponentUuid() + " due to missing " + ((TkRefexUuidMember)rm).getUuid1());
								remove = true;
							}
							break;
							
						case STR: 
							if (!seenComponentList.contains(((TkRefsetStrMember)rm).getString1())) {
								missingRefsetComponentWriter.write("\n" + i + ": Removing STR with member UUID: " + rm.getPrimordialComponentUuid() + " due to missing " + ((TkRefsetStrMember)rm).getString1());
								remove = true;
							}
							break;
							
						case INT: 
							if (!seenComponentList.contains(((TkRefexIntMember)rm).getInt1())) {
								missingRefsetComponentWriter.write("\n" + i + ": Removing INT with member UUID: " + rm.getPrimordialComponentUuid() + " due to missing " + ((TkRefexIntMember)rm).getInt1());
								remove = true;
							}
							break;
							
						case CID_INT: 
							if (!seenComponentList.contains(((TkRefexUuidIntMember)rm).getInt1()) &&
								!seenComponentList.contains(((TkRefexUuidIntMember)rm).getUuid1())) {
								missingRefsetComponentWriter.write("\n" + i + ": Removing CID_INT with member UUID: " + rm.getPrimordialComponentUuid() + " due to missing BOTH " + ((TkRefexUuidIntMember)rm).getInt1() + " AND " + ((TkRefexUuidIntMember)rm).getUuid1());
								remove = true;
							} else if (!seenComponentList.contains(((TkRefexUuidIntMember)rm).getInt1())) {
								missingRefsetComponentWriter.write("\n" + i + ": Removing CID_INT with member UUID: " + rm.getPrimordialComponentUuid() + " due to missing only INT" + ((TkRefexUuidIntMember)rm).getInt1());
								remove = true;
							} else if (!seenComponentList.contains(((TkRefexUuidIntMember)rm).getUuid1())) {
								missingRefsetComponentWriter.write("\n" + i + ": Removing CID_INT with member UUID: " + rm.getPrimordialComponentUuid() + " due to missing only UUID" + ((TkRefexUuidIntMember)rm).getUuid1());
								remove = true;
							}
							break;
							
						default:
							String errStr = "Have unhandled Refset Type for making revisions:" + rm.getType();
							Logger.getLogger(EConceptDiffUtility.class.getName()).log(Level.SEVERE, errStr);
							throw new Exception(errStr);
					}
					
					if (remove) {
						i++;
						iter.remove();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("End with Size: " + c.getRefsetMembers().size());
		}
		
		missingRefsetComponentWriter.close();
	}

	private void setup() {
        try {
        	setupOutputFiles();
	    	
	    	previousEConceptList = readJBin(oldJBin);
	    	currentEConceptList = readJBin(newJBin);
	    	
	    	writeMakers(previousEConceptList, currentEConceptList);
	    	
			retireUtil = new EConceptDiffUtility();
			retireUtil.setNewImportDate(IMPORT_YEAR, IMPORT_MONTH, IMPORT_DATE);

        } catch (IOException | ClassNotFoundException e) {
        	e.printStackTrace();
        }
	}

	private void setupOutputFiles() throws IOException {
        date = new Date();
        File f = new File(outputDirPath);
        f.mkdirs();
                    
    	conceptWriter = new FileWriter(outputDirPath + conceptFILENAME);
    	diffWriter = new FileWriter(outputDirPath + diffFILENAME);		
    	missingRefsetComponentWriter = new FileWriter(outputDirPath + missingRefsetComponentFILENAME);		
    	
    	FileOutputStream componentCSFile = new FileOutputStream(new File(outputDirPath + componentFILENAME + ".eccs"));
    	componentCSWriter = new DataOutputStream(new BufferedOutputStream(componentCSFile));
       
        FileOutputStream refsetCSFile = new FileOutputStream(new File(outputDirPath + refsetFILENAME + "-" + ".eccs"));
    	refsetCSWriter = new DataOutputStream(new BufferedOutputStream(refsetCSFile));
                
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



	private void writeChangeSet(List<EConcept> changesetList, DataOutputStream writer, String type) throws IOException {
		int i = 1;

		diffWriter.write("\n\n\n\t\t\t**** " + type + " CONCEPTS ****");

		for (EConcept c : changesetList) {
			try {
				diffWriter.write("\n\n\t\t\t---- " + type + " Concept #" + i++ + "   " + c.getPrimordialUuid() + " ----");
				diffWriter.write(c.toString());

				writer.writeLong(date.getTime());
				c.writeExternal(writer);
			} catch (Exception e) {
				System.out.println(c);
				throw e;
			}
		}
	}


	private void writeChangeSet(List<EConcept> changesetList, String type) throws IOException {
		int i = 1;

		diffWriter.write("\n\n\n\t\t\t**** " + type + " CONCEPTS ****");

		for (EConcept c : changesetList) {
			try {
				diffWriter.write("\n\n\t\t\t---- " + type + " Concept #" + i++ + "   " + c.getPrimordialUuid() + " ----");
				diffWriter.write(c.toString());

				componentCSWriter.writeLong(date.getTime());
				c.writeExternal(componentCSWriter);
			} catch (Exception e) {
				System.out.println(c);
				throw e;
			}
		}
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


	private void identifyChanges(List<EConcept> oldList, List<EConcept> newList) {
    	Set<UUID> matchedSet = new HashSet<UUID>();

    	// Find existing
	   	for (EConcept oldCon : oldList) {
    		for (EConcept newCon : newList) {
    			if (oldCon.getPrimordialUuid().equals(newCon.getPrimordialUuid())) {
    		    	EConcept diffCon = diffChangedConcept(oldCon, newCon);
    		    	if (diffCon != null) {
	    		    	changedConcepts.add(diffCon); 
    		    	}
    		    	matchedSet.add(oldCon.getPrimordialUuid());
    		    	break;
    			}
    		}
    	}

    	// Retire oldCons not in oldList
    	// Note: SNOMED CT should handle this itself... so really just for other terms
		
    	for (EConcept oldCon : oldList) {
    		if (!matchedSet.contains(oldCon.getPrimordialUuid())) {
    			retireUtil.retireCon(oldCon, retireUtil.getNewImportDate());
    			retiredConcepts.add(oldCon);
    		}
    	}
    	
    	// Add newCons not in newList
    	for (EConcept newCon : newList) {
    		if (!matchedSet.contains(newCon.getPrimordialUuid())) {
    			EConcept addedConcept = diffChangedConcept(null, newCon);
		    	if (addedConcept != null) {
		    		addedConcepts.add(addedConcept);
		    	}
    		}
    	}
	}





	private static void updateSeenComponentList(EConcept diffCon, EConcept diffRefsetCon) {
		if (diffCon.conceptAttributes != null) {
			seenComponentList.add(diffCon.conceptAttributes.getPrimordialComponentUuid());
		}
		
		if (diffCon.descriptions != null) {
			for (TkDescription d : diffCon.descriptions) {
				seenComponentList.add(d.getPrimordialComponentUuid());
			}
		}

		if (diffCon.relationships != null) {
			for (TkRelationship r : diffCon.relationships) {
				seenComponentList.add(r.getPrimordialComponentUuid());
			}
		}

		if (diffRefsetCon.refsetMembers != null) {
			for (TkRefexAbstractMember<?> rm : diffRefsetCon.refsetMembers) {
				seenComponentList.add(rm.getPrimordialComponentUuid());
			}
		}
	}


	// TODO: Handle components referencing new concepts yet to be imported.
	// Case 1) Concept has relationship where type or target not yet imported
	// Case 2) Concept has component with annotations that have COMPONENT that has yet to be imported
	private static EConcept diffChangedConcept(EConcept oldCon, EConcept newCon) {
		EConcept diffCon = new EConcept();
		EConcept diffRefsetCon = new EConcept();
		
		if (oldCon == null || oldCon.getPrimordialUuid().equals(newCon.getPrimordialUuid())) {
			EConceptDiffUtility.conceptChangeFound = false;
			
			diffCon.setPrimordialUuid(newCon.getPrimordialUuid());
			diffRefsetCon.setPrimordialUuid(newCon.getPrimordialUuid());

			if (oldCon == null || oldCon.annotationIndexStyleRefex != newCon.annotationIndexStyleRefex) {
				diffCon.annotationIndexStyleRefex = newCon.annotationIndexStyleRefex;
				diffRefsetCon.annotationIndexStyleRefex = newCon.annotationIndexStyleRefex;
				EConceptDiffUtility.conceptChangeFound = true;
			}
			
			if (oldCon == null || oldCon.annotationStyleRefex != newCon.annotationStyleRefex) {
				diffCon.annotationStyleRefex = newCon.annotationStyleRefex;
				diffRefsetCon.annotationStyleRefex = newCon.annotationStyleRefex;
				EConceptDiffUtility.conceptChangeFound = true;
			}
		
			if (oldCon == null) {
				diffCon.conceptAttributes = (TkConceptAttributes)attrUtil.diff(null, newCon.conceptAttributes);
				diffCon.descriptions = (List<TkDescription>)descUtil.diff(null, newCon.descriptions);
				diffCon.relationships = (List<TkRelationship>)relUtil.diff(null, newCon.relationships);
				diffRefsetCon.refsetMembers = util.handleRefsets(null, newCon.refsetMembers);
			} else {
				diffCon.conceptAttributes = (TkConceptAttributes)attrUtil.diff(oldCon.conceptAttributes, newCon.conceptAttributes);
				diffCon.descriptions = (List<TkDescription>)descUtil.diff(oldCon.descriptions, newCon.descriptions);
				diffCon.relationships = (List<TkRelationship>)relUtil.diff(oldCon.relationships, newCon.relationships);
				diffRefsetCon.refsetMembers = util.handleRefsets(oldCon.refsetMembers, newCon.refsetMembers);
			}
		}
		
		
		if (EConceptDiffUtility.conceptChangeFound == false) {
			return null;
		}
		
		updateSeenComponentList(newCon, diffRefsetCon);
		
		if (diffRefsetCon.refsetMembers != null && diffRefsetCon.refsetMembers.size() > 0) {
			consWithRefsetList.add(diffRefsetCon);
		}
		
		return diffCon;
	}

}

