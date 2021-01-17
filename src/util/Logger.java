package util;

import java.io.FileNotFoundException;
import java.io.PrintStream;

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
