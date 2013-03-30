package gov.va.oia.terminology.converters.sharedUtils.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

/**
 * A utility class for generating UUIDs which keeps track of what was used to generate the UUIDs - which
 * can then be dumped to disk (or looked up by UUID)
 * 
 * @author darmbrust
 */

public class ConverterUUID
{
	public static boolean enableDupeUUIDException = true;
	private static Hashtable<UUID, String> masterUUIDMap_ = new Hashtable<UUID, String>();

	/**
	 * Simply calls java.util.UUID.randomUUID()
	 * 
	 * @see java.util.UUID#randomUUID
	 */
	public static UUID randomUUID()
	{
		UUID uuid = UUID.randomUUID();
		masterUUIDMap_.put(uuid, "_RANDOM_");
		return uuid;
	}

	/**
	 * Simply calls java.util.UUID.nameUUIDFromBytes(byte[])
	 * 
	 * @see java.util.UUID#nameUUIDFromBytes(byte[]);
	 */
	public static UUID nameUUIDFromBytes(byte[] name)
	{
		UUID uuid = UUID.nameUUIDFromBytes(name);
		String putResult = masterUUIDMap_.put(uuid, new String(name));
		if (enableDupeUUIDException && putResult != null)
		{
			throw new RuntimeException("Just made a duplicate UUID! '" + new String(name) + "' -> " + uuid);
		}
		return uuid;
	}

	/**
	 * Simply calls java.util.UUID.fromString(String)
	 * 
	 * @see java.util.UUID#fromString(String)
	 */
	public static UUID fromString(String name)
	{
		UUID uuid = UUID.fromString(name);
		masterUUIDMap_.put(uuid, name);
		return uuid;
	}

	/**
	 * Return the string that was used to generate this UUID (if available - null if not)
	 */
	public static String getUUIDCreationString(UUID uuid)
	{
		if (uuid == null)
		{
			return null;
		}
		return masterUUIDMap_.get(uuid);
	}

	/**
	 * Write out a debug file with all of the UUID - String mappings
	 */
	public static void dump(File file) throws IOException
	{
		BufferedWriter br = new BufferedWriter(new FileWriter(file));
		for (Map.Entry<UUID, String> entry : masterUUIDMap_.entrySet())
		{
			br.write(entry.getKey() + " - " + entry.getValue() + System.getProperty("line.separator"));
		}
		br.close();
	}

	/**
	 * Allow this map to be updated with UUIDs that were not generated via this utility class
	 */
	public static void addMapping(String value, UUID uuid)
	{
		masterUUIDMap_.put(uuid, value);
	}
}
