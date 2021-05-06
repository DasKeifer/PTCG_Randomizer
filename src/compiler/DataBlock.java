package compiler;

import java.util.LinkedList;
import java.util.List;

import compiler.fixed.*;

public class DataBlock 
{
	public static final int UNASSIGNED_ADDRESS = -1;
	public static final int UNASSIGNED_LOCAL_ADDRESS = -2;
	private static final String SEGMENT_ENDLINE = ":";
	private static final String SUBSEGMENT_STARTLINE = ".";
	private static final String LINE_BREAK = "\n";
	
	private int assignedAddress;
	List<Segment> segments;
	private String id;
	
	// Generic, highest level construct for holding just raw data or a series of functions
	// Represents one block that we want to place contiguously in the ROM
	// I.e. replace Code Snippet
	public DataBlock(String startingSegmentName, String source)
	{
		assignedAddress = UNASSIGNED_ADDRESS;
		segments = new LinkedList<Segment>();
		id = startingSegmentName;
		String sourcePlusSegmentName = startingSegmentName + SEGMENT_ENDLINE + LINE_BREAK + source;
		parseSource(startingSegmentName, sourcePlusSegmentName.split(LINE_BREAK));
	}
	
	public DataBlock(String source)
	{
		assignedAddress = UNASSIGNED_ADDRESS;
		segments = new LinkedList<Segment>();
		
		String[] lines = source.split(LINE_BREAK);
		if (!lines[0].trim().endsWith(SEGMENT_ENDLINE))
		{
			throw new IllegalArgumentException("The first line must be a Segment label (i.e. the segment name followed by a ':'");
		}

		id = getSegmentName(lines[0]);
		parseSource(id, lines);
	}
	
	private void parseSource(String startingSegmentName, String[] sourceLines)
	{
		String rootSegmentName = startingSegmentName;
		segments.add(new Segment(rootSegmentName));
		
		// Plus one because we already handled the first line
		String line;
		for (int lineIdx = 1; lineIdx < sourceLines.length; lineIdx++)
		{
			// split of the instruction (if there is one)
			line = sourceLines[lineIdx].trim();
			if (line.endsWith(SEGMENT_ENDLINE))
			{
				rootSegmentName = getSegmentName(line);
				segments.add(new Segment(rootSegmentName));
			}
			else if (line.startsWith(SUBSEGMENT_STARTLINE))
			{
				segments.add(new Segment(getSubsegmentName(rootSegmentName, line)));
			}
			// A line that will turn into bytes
			else
			{
				
			}
		}
	}
	
	private String getSegmentName(String line)
	{
		return line.substring(0, line.indexOf(SEGMENT_ENDLINE)).trim();
	}
	
	private String getSubsegmentName(String segmentName, String line)
	{
		return segmentName + line.trim();
	}
	
	public void setAssignedAddress(int address)
	{
		this.assignedAddress = address;
	}
	
	public int getAssignedAddress()
	{
		return assignedAddress;
	}
	
	public String getId()
	{
		return id;
	}
	
	public int getWorstCaseSizeOnBank(byte bank)
	{
		int worstCaseSize = 0;
		for (Segment segment : segments)
		{
			worstCaseSize += segment.getWorstCaseSizeOnBank(bank);
		}
		return worstCaseSize;
	}
	
	public static Data create(String line)
	{
		// Split the keyword off
		String[] keyArgs = line.split(" ", 1);
		
		// Split the args apart
		String[] args = new String[0];
		if (keyArgs.length >= 1)
		{
			args = keyArgs[1].split(",");
		}
		
		switch (keyArgs[0])
		{
			// TODO: complete this list
			// Loading
			case "lb":
				return Lb.create(args);
			case "ld":
				return Ld.create(args);
		
			// Logic
			case "cp":
				return Cp.create(args);
			case "inc":
				return Inc.create(args);
				
			// Flow control
				
			// Writing raw data
				
			default:
				throw new UnsupportedOperationException("Unrecognized instruction key: " + keyArgs[0]);
		}
	}
}
