package compiler.dynamic;

import java.util.LinkedList;
import java.util.List;

import compiler.fixed.Cp;
import compiler.fixed.Inc;
import compiler.fixed.Instruction;
import compiler.fixed.Lb;
import compiler.fixed.Ld;

public class Block 
{
	private static final String SEGMENT_ENDLINE = ":";
	private static final String SUBSEGMENT_STARTLINE = ".";
	private static final String LINE_BREAK = "\n";
	private int address;
	List<Segment> segments;
	
	// Generic, highest level construct for holding just raw data or a series of functions
	// Represents one block that we want to place contiguously in the ROM
	// I.e. replace Code Snippet
	public Block(String startingSegmentName, String source)
	{
		segments = new LinkedList<Segment>();
		String sourcePlusSegmentName = startingSegmentName + SEGMENT_ENDLINE + LINE_BREAK + source;
		parseSource(startingSegmentName, sourcePlusSegmentName.split(LINE_BREAK));
	}
	
	public Block(String source)
	{
		segments = new LinkedList<Segment>();
		
		String[] lines = source.split(LINE_BREAK);
		if (!lines[0].trim().endsWith(SEGMENT_ENDLINE))
		{
			throw new IllegalArgumentException("The first line must be a Segment label (i.e. the segment name followed by a ':'");
		}

		parseSource(getSegmentName(lines[0]), lines);
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
	
	public void assignAddress(int address)
	{
		this.address = address;
	}
	
	public int getAssignedAddress(int address)
	{
		return address;
	}
	
	private String getSegmentName(String line)
	{
		return line.substring(0, line.indexOf(SEGMENT_ENDLINE)).trim();
	}
	
	private String getSubsegmentName(String segmentName, String line)
	{
		return segmentName + line.trim();
	}
	
	public static Data create(String line)
	{
		// Split the keyword off
		String[] keyArgs = line.split(" ", 1);
		
		// TODO determine types and put in object list. Then sub objects call the instance of to see if its compatible
		// Maybe use something less generic than object too
		String[] args = new String[0];
		if (keyArgs.length >= 1)
		{
			args = keyArgs[1].split(",");
		}
		
		switch (keyArgs[0])
		{
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
				
			// Flow control? Maybe handled in separate class?
				
			// Writing raw
			default:
				throw new UnsupportedOperationException("Unrecognized instruction key: " + keyArgs[0]);
		}
	}
}
