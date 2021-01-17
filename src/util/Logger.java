package util;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;

public class Logger
{
	PrintStream logFile = null;
	
	public void print(String text)
	{
		if (logFile != null)
		{
			logFile.print(text);
		}
	}
	
	public void println(String line)
	{
		if (logFile != null)
		{
			logFile.println(line);
		}
	}
	
	public void printf(String format, Object... args)
	{
		if (logFile != null)
		{
			logFile.printf(format, args);
		}
	}
	
	public static void findMaxStringLengths(int[] fieldsMaxLengths, String ... fields)
	{
		if (fieldsMaxLengths.length != fields.length)
		{
	    	throw new IllegalArgumentException("Lengths array length (" + 
	    			fieldsMaxLengths.length + ") does not match the passed number of fields (" + 
	    			fields.length + ")");
		}
		
		int idx = 0;
		for (String field: fields)
		{
			if (field.length() > fieldsMaxLengths[idx])
			{
				fieldsMaxLengths[idx] = field.length();
			}
			idx++;
		}
	}
	
	public static String createSeparatorLine(int length)
	{		
		// Java doesn't have a good way to make a string n length with one character
		char[] tempArray = new char[length];
		Arrays.fill(tempArray, '-');
		return new String(tempArray);
	}
	
	public static String createTableTitle(String title, int length)
	{
		int spacing = length - title.length() - 2; // -2 for the | at each side
		StringBuilder tableNameFormatBuilder = new StringBuilder();
		tableNameFormatBuilder.append("|%");
		tableNameFormatBuilder.append(spacing / 2);
		tableNameFormatBuilder.append("s%s%");
		tableNameFormatBuilder.append(spacing / 2 + spacing % 2); // Handles odd lengths
		tableNameFormatBuilder.append("s|");
		return String.format(tableNameFormatBuilder.toString(), "", title, "");
	}
	
	public static String createTableFormatString(int[] fieldsMaxLengths, String ... formats)
	{
		if (fieldsMaxLengths.length < formats.length)
		{
	    	throw new IllegalArgumentException("Lengths array length (" + 
	    			fieldsMaxLengths.length + ") is less than the passed number of formats (" + 
	    			formats.length + ")");
		}
		
		// Get iterator over varargs
		Iterator<String> formatItr = Arrays.asList(formats).iterator();

		StringBuilder formatBuilder = new StringBuilder();
		formatBuilder.append("|");
		for (int idx = 0; idx < fieldsMaxLengths.length; idx++)
		{
			formatBuilder.append("%");
			if (formatItr.hasNext())
			{
				formatBuilder.append(formatItr.next());
			}
			formatBuilder.append(fieldsMaxLengths[idx]);
			formatBuilder.append("s|");
		}

		formatBuilder.append("\n");
		return formatBuilder.toString();		
	}
	
	public void open(String logFileName) throws FileNotFoundException
	{
		logFile = new PrintStream(logFileName); 
	}
	
	public void close()
	{
		if (logFile != null)
		{
			logFile.close();
		}
		logFile = null;
	}
}
