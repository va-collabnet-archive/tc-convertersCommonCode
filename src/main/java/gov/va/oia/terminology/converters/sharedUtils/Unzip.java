package gov.va.oia.terminology.converters.sharedUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;

public class Unzip
{
	public final static void unzip(File zipFile, File rootDir) throws IOException
	{
		ZipFile zip = new ZipFile(zipFile);
		@SuppressWarnings("unchecked") Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
		while (entries.hasMoreElements())
		{
			ZipEntry entry = entries.nextElement();
			java.io.File f = new java.io.File(rootDir, entry.getName());
			if (entry.isDirectory())
			{
				f.mkdirs();
				continue;
			}
			else
			{
				f.createNewFile();
			}
			InputStream is = null;
			OutputStream os = null;
			try
			{
				is = zip.getInputStream(entry);
				os = new FileOutputStream(f);
				IOUtils.copy(is, os);
			}
			finally
			{
				if (is != null)
				{
					try
					{
						is.close();
					}
					catch (Exception e)
					{
						// noop
					}
				}
				if (os != null)
				{
					try
					{
						os.close();
					}
					catch (Exception e)
					{
						// noop
					}
				}
			}
		}
		zip.close();
	}
}
