/*
 * ScanForUnusedIl8nKeys.java
 * Copyright James Dempsey, 2012
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on 20/02/2012 7:31:38 AM
 *
 * $Id$
 */
package pcgen.gui2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.junit.Test;

/**
 * The Class <code>ScanForUnusedIl8nKeys</code> check for any unused keys in 
 * the il8n properties. Currently it is a utility class masquerading as a unit 
 * test but after completion of localisation work it will be used as means of 
 * verifying the properties files.  
 *
 * <br/>
 * Last Editor: $Author$
 * Last Edited: $Date$
 * 
 * @author James Dempsey <jdempsey@users.sourceforge.net>
 * @version $Revision$
 */

public class ScanForUnusedIl8nKeys
{

	private static final String CODE_PATH = "code/src/java/";
	private static final String PROPERTIES_PATH = "pcgen/resources/lang/";
	private static final String PROPERTIES_FILE = "LanguageBundle.properties";
	private static final String NEW_PROPERTIES_FILE = "cleaned.properties";
	private static final String[] PACKAGES = new String[]{"pcgen/gui2",
		"pcgen/core", "pcgen/system", "gmgen", "plugin", "pcgen/io",
		"pcgen/persistence", "pcgen/cdom", "pcgen/rules/context", "pcgen/util", };
	
	@Test
	public void scanForUnusedKeys() throws Exception
	{
		//Read in bundle, grab all keys
		Properties p = new Properties();
		p.load(new FileInputStream(CODE_PATH + PROPERTIES_PATH + PROPERTIES_FILE));
		Set<String> keys = new TreeSet<String>();
		for (Entry e : p.entrySet())
		{
			keys.add((String)e.getKey());
		}

		// Grab a list of files to be scanned
		List<File> fileList = buildFileList();
		
		// Scan each file marking each found entry
		Set<String> missingKeys = new TreeSet<String>(keys);
		actionWhitelistedKeys(missingKeys);
		for (File file : fileList)
		{
			scanJavaFileForKeys(file, missingKeys);
		}
		
		// Report all missing entries
		for (String key : missingKeys)
		{
			System.out.println("Found unused key '" + key + "'.");
		}
		System.out.println("Total unused keys: " + missingKeys.size()
			+ " from a set of " + keys.size() + " defined keys. "
			+ ((missingKeys.size() * 100.0) / keys.size()) + "%");

		// Output a new set
		outputCleanedProperties(new File(CODE_PATH + PROPERTIES_PATH
			+ PROPERTIES_FILE), new File(CODE_PATH + PROPERTIES_PATH
			+ NEW_PROPERTIES_FILE), missingKeys);
	}

	/**
	 * PCGenActionMap and PCGenAction dynamically construct keys. All keys 
	 * starting with the pattern used in those classes will be deemed present
	 * and removed from the missing keys set. 
	 * 
	 * @param missingKeys The list of missing keys
	 */
	private void actionWhitelistedKeys(Set<String> missingKeys)
	{
		for (Iterator<String> iterator = missingKeys.iterator(); iterator.hasNext();)
		{
			String key = (String) iterator.next();
			if (key.startsWith("in_mnu") || key.startsWith("in_mn_mnu"))
			{
				iterator.remove();
			}
		}
		
	}

	/**
	 * @param file
	 * @param missingKeys
	 * @throws IOException 
	 */
	private void scanJavaFileForKeys(File file, Set<String> missingKeys) throws IOException
	{
		Reader reader = new BufferedReader(new FileReader(file));
		List<String> lines = IOUtils.readLines(reader);
		reader.close();
		for (String line : lines)
		{
			for (Iterator<String> i = missingKeys.iterator(); i.hasNext();)
			{
				String key = i.next();
				if (line.contains("\"" + key + "\""))
				{
					i.remove();
				}
			}
			
		}
	}

	/**
	 * @param inputPropsFile
	 * @param cleanPropsFile
	 * @param unusedKeys
	 * @throws IOException 
	 */
	private void outputCleanedProperties(File inputPropsFile, File cleanPropsFile,
		Set<String> unusedKeys) throws IOException
	{
		Reader reader = new BufferedReader(new FileReader(inputPropsFile));
		List<String> lines = IOUtils.readLines(reader);
		reader.close();
		Writer writer = new BufferedWriter(new FileWriter(cleanPropsFile));
		writer.write("# " + PROPERTIES_FILE
			+ " with all unused keys removed as at "
			+ DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(new Date())
			+ "\n");
		boolean lastLineBlank = false;
		for (String line : lines)
		{
			boolean found = false;
			if (lastLineBlank && line.trim().isEmpty())
			{
				continue;
			}
			else
			{
				for (String key : unusedKeys)
				{
					if (line.startsWith(key+"="))
					{
						found = true;
						break;
					}
				}
			}
			if (!found)
			{
				lastLineBlank = line.trim().isEmpty();
				writer.write(line + "\n");
			}
		}
		writer.close();
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private List<File> buildFileList() throws IOException
	{
		List<File> allFiles = new ArrayList<File>();
		JavaFileLister lister = new JavaFileLister();
		
		for (String pkg : PACKAGES)
		{
			File folder = new File(CODE_PATH + pkg);
			allFiles.addAll(lister.getJavaFileList(folder));
		}
		return allFiles;
	}

	public class JavaFileLister extends DirectoryWalker
	{

		public JavaFileLister()
		{
			super();
		}

		public List getJavaFileList(File startDirectory) throws IOException
		{
			List results = new ArrayList();
			walk(startDirectory, results);
			return results;
		}

		protected boolean handleDirectory(File directory, int depth,
			Collection results)
		{
			// Ignore svn directories 
			if (".svn".equals(directory.getName()))
			{
				return false;
			}
			else
			{
				return true;
			}

		}

		protected void handleFile(File file, int depth, Collection results)
		{
			if (file.getName().endsWith(".java"))
			{
				results.add(file);
			}
		}
	}
	
}