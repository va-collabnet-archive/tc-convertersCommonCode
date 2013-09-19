package gov.va.oia.terminology.converters.sharedUtils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import org.dwfa.util.id.Type5UuidFactory;

public class UuidFromName
{

	/**
	 * This is how to get a UUID that the WB expects from a string in a pom....
	 * @param args
	 * @throws UnsupportedEncodingException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		System.out.println(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, "VA JIF Terminology Workbench development path"));
		System.out.println(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, "VA JIF Terminology Workbench development origin"));
		System.out.println(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, "VA JIF Terminology Workbench release candidate path"));
		System.out.println(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC, "Project Refsets"));

	}

}
