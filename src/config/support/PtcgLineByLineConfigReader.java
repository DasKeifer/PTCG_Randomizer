package config.support;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;

import gbc_framework.utils.IOUtils;

public abstract class PtcgLineByLineConfigReader 
{	
	public static final String CONFIG_FILE_SOURCE_LOC = "/config/defaultFiles/";
	public static final String CONFIG_FILE_INSTALL_LOC = "configs";
	public static final String CSV_CONFIG_FILE_EXTENSION = ".csv";

	protected final Component toCenterPopupsOn;
	protected StringBuilder warningsFoundParsing;
	
	protected PtcgLineByLineConfigReader(Component toCenterPopupsOn)
	{
		this.toCenterPopupsOn = toCenterPopupsOn;
		warningsFoundParsing = new StringBuilder();
	}

	public abstract String getName();
	public abstract String getExtension();
	protected abstract void parseAndAddLine(String line, PtcgAdditionalLineArgs additionalArgs);	
	
	protected void readAndParseConfig(PtcgAdditionalLineArgs additionalParsingArgs)
	{
		try
		{
			File file = IOUtils.copyFileFromConfigsIfNotPresent(CONFIG_FILE_SOURCE_LOC, getName() + getExtension(), CONFIG_FILE_INSTALL_LOC);
	        
			// Now go ahead and read the file
			try (FileReader configFR = new FileReader(file);
					BufferedReader configReader = new BufferedReader(configFR)
			)
			{
				String line;
		        while((line = configReader.readLine()) != null)  
		        {  
		        	// # is the line we use for comments in the files - skip those
		        	line = line.trim();
		        	if (!line.isEmpty() && !line.startsWith("#"))
		        	{
			        	parseAndAddLine(line, additionalParsingArgs);
		        	}
		        }  
			}
			// IO errors will be caught by the below statement
		}
		catch (IOException e)
		{
			// We have to insert in in reverse order since we are always inserting at the start
			warningsFoundParsing.insert(0, e.getMessage());
			warningsFoundParsing.insert(0, " - information likely was not read in successfully: ");
			warningsFoundParsing.insert(0, getExtension());
			warningsFoundParsing.insert(0, getName());
			warningsFoundParsing.insert(0, "Unexpected IO Exception reading ");
		}
		
		displayIOWarningsIfPresent();
	}

	private void displayIOWarningsIfPresent()
	{
		if (warningsFoundParsing.length() > 0)
		{
			// We have to insert in in reverse order since we are always inserting at the start
			warningsFoundParsing.insert(0, IOUtils.NEWLINE);
			warningsFoundParsing.insert(0, "\" relative to the JAR:");
			warningsFoundParsing.insert(0, CONFIG_FILE_INSTALL_LOC);
			warningsFoundParsing.insert(0, IOUtils.FILE_SEPARATOR);
			warningsFoundParsing.insert(0, "\" config file located in \"");
			warningsFoundParsing.insert(0, getExtension());
			warningsFoundParsing.insert(0, getName());
			warningsFoundParsing.insert(0, "The following issue(s) were encoundered while parsing the \"");
			IOUtils.showScrollingMessageDialog(toCenterPopupsOn, warningsFoundParsing.toString(), 
					"Issue(s) encountered while parsing " + getName() + getExtension(), 
					JOptionPane.WARNING_MESSAGE);
			
			// Reset the string buffer
			warningsFoundParsing.setLength(0);
		}
	}
	
	public void displayWarningsIfPresent()
	{
		if (warningsFoundParsing.length() > 0)
		{
			// We have to insert in in reverse order since we are always inserting at the start
			warningsFoundParsing.insert(0, IOUtils.NEWLINE);
			warningsFoundParsing.insert(0, ":");
			warningsFoundParsing.insert(0, getName());
			warningsFoundParsing.insert(0, "The following issue(s) were encoundered while handling ");
			IOUtils.showScrollingMessageDialog(toCenterPopupsOn, warningsFoundParsing.toString(), 
					"Issue(s) encountered while handling" + getName(), JOptionPane.WARNING_MESSAGE);
			
			// Reset the string buffer
			warningsFoundParsing.setLength(0);
		}
	}
}
