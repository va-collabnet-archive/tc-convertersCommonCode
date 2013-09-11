package gov.va.oia.terminology.converters.sharedUtils.stats;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Keep counts on all of the types of things that are converted.
 * 
 * @author Daniel Armbrust
 */

public class LoadStats
{
	private int concepts_ = 0;
	private TreeMap<String, Integer> descriptions_ = new TreeMap<String, Integer>();
	private TreeMap<String, Integer> conceptIds_ = new TreeMap<String, Integer>();
	private TreeMap<String, TreeMap<String, Integer>> componentIds_ = new TreeMap<String, TreeMap<String, Integer>>();
	private TreeMap<String, Integer> refsetMembers_ = new TreeMap<String, Integer>();
	private TreeMap<String, Integer> relationships_ = new TreeMap<String, Integer>();
	private TreeMap<String, TreeMap<String, Integer>> annotations_ = new TreeMap<String, TreeMap<String, Integer>>();
	
	private Object syncLock = new Object();

	public void addConcept()
	{
		concepts_++;
	}

	public int getConceptCount()
	{
		return concepts_;
	}

	public void addDescription(String descName)
	{
		increment(descriptions_, descName);
	}

	public void addConceptId(String idName)
	{
		increment(conceptIds_, idName);
	}

	public void addComponentId(String annotatedItem, String annotationName)
	{
		increment(componentIds_, annotatedItem, annotationName);
	}

	public void addAnnotation(String annotatedItem, String annotationName)
	{
		increment(annotations_, annotatedItem, annotationName);
	}

	public void addRefsetMember(String refsetName)
	{
		increment(refsetMembers_, refsetName);
	}

	public void addRelationship(String relName)
	{
		increment(relationships_, relName);
	}

	public ArrayList<String> getSummary()
	{
		ArrayList<String> result = new ArrayList<String>();

		result.add("Concepts: " + concepts_);

		int sum = 0;
		for (Map.Entry<String, Integer> value : relationships_.entrySet())
		{
			sum += value.getValue();
			result.add("Relationship '" + value.getKey() + "': " + value.getValue());
		}
		result.add("Relationships Total: " + sum);

		sum = 0;
		for (Map.Entry<String, Integer> value : conceptIds_.entrySet())
		{
			sum += value.getValue();
			result.add("Concept ID '" + value.getKey() + "': " + value.getValue());
		}
		result.add("Concept IDs Total: " + sum);

		sum = 0;
		int nestedSum = 0;
		for (Map.Entry<String, TreeMap<String, Integer>> value : componentIds_.entrySet())
		{
			nestedSum = 0;
			for (Map.Entry<String, Integer> nestedValue : value.getValue().entrySet())
			{
				result.add("Component ID '" + value.getKey() + ":" + nestedValue.getKey() + "': " + nestedValue.getValue());
				nestedSum += nestedValue.getValue();
			}
			sum += nestedSum;
			if (value.getValue().size() > 1)
			{
				result.add("Component ID '" + value.getKey() + "' Total: " + nestedSum);
			}
		}
		result.add("Component IDs Total: " + sum);

		sum = 0;
		for (Map.Entry<String, Integer> value : descriptions_.entrySet())
		{
			sum += value.getValue();
			result.add("Description '" + value.getKey() + "': " + value.getValue());
		}
		result.add("Descriptions Total: " + sum);

		sum = 0;
		nestedSum = 0;
		for (Map.Entry<String, TreeMap<String, Integer>> value : annotations_.entrySet())
		{
			nestedSum = 0;
			for (Map.Entry<String, Integer> nestedValue : value.getValue().entrySet())
			{
				result.add("Annotation '" + value.getKey() + ":" + nestedValue.getKey() + "': " + nestedValue.getValue());
				nestedSum += nestedValue.getValue();
			}
			sum += nestedSum;
			if (value.getValue().size() > 1)
			{
				result.add("Annotation '" + value.getKey() + "' Total: " + nestedSum);
			}
		}
		result.add("Annotations Total: " + sum);

		sum = 0;
		for (Map.Entry<String, Integer> value : refsetMembers_.entrySet())
		{
			sum += value.getValue();
			result.add("Refset Members '" + value.getKey() + "': " + value.getValue());
		}
		result.add("Refset Members Total: " + sum);

		return result;
	}

	private void increment(TreeMap<String, Integer> dataHolder, String type)
	{
		synchronized (syncLock)
		{
			Integer i = dataHolder.get(type);
			if (i == null)
			{
				i = new Integer(1);
			}
			else
			{
				i++;
			}
			dataHolder.put(type, i);
		}
	}

	private void increment(TreeMap<String, TreeMap<String, Integer>> dataHolder, String annotatedType, String type)
	{
		synchronized (syncLock)
		{
			TreeMap<String, Integer> map = dataHolder.get(annotatedType);
	
			if (map == null)
			{
				map = new TreeMap<String, Integer>();
			}
	
			Integer i = map.get(type);
	
			if (i == null)
			{
				i = new Integer(1);
			}
			else
			{
				i++;
			}
			map.put(type, i);
			dataHolder.put(annotatedType, map);
		}
	}
}
