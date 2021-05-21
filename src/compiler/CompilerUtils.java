package compiler;


import java.util.Map;
import java.util.Map.Entry;

import compiler.CompilerConstants.*;
import compiler.dynamic.*;
import compiler.fixed.*;
import constants.RomConstants;
import data.romtexts.*;

public class CompilerUtils 
{
	public static final int UNASSIGNED_ADDRESS = -1;
	public static final int UNASSIGNED_LOCAL_ADDRESS = -2;
	static final String SEGMENT_ENDLINE = ":";
	static final String SUBSEGMENT_STARTLINE = ".";
	static final String STRING_QUOTE = "\n";
	static final String LINE_BREAK = "\n";
	static final String HEX_VAL_MARKER = "$";
	static final String PLACEHOLDER_MARKER = "#";
	
	public static String tryParseSegmentName(String line)
	{
		if (line.endsWith(CompilerUtils.SEGMENT_ENDLINE))
		{
			return getSegmentName(line);
		}
		return null;
	}

	private static String getSegmentName(String line)
	{
		return line.substring(0, line.indexOf(CompilerUtils.SEGMENT_ENDLINE)).trim();
	}
	
	public static String tryParseSubsegmentName(String line, String rootSegmentName)
	{
		if (line.startsWith(CompilerUtils.SUBSEGMENT_STARTLINE))
		{
			return getSubsegmentName(line, rootSegmentName);
		}
		return null;
	}
	
	public static String formSubsegmentName(String subsegment, String rootSegmentName)
	{
		return rootSegmentName + "." + subsegment;
	}
	
	private static String getSubsegmentName(String segmentName, String line)
	{
		return segmentName + line.trim();
	}
	
	public static String tryParseBracketedArg(String arg)
	{
		arg = arg.trim();
		if (arg.startsWith("[") && arg.endsWith("]"))
		{
			return arg.substring(1, arg.length() - 2);
		}
		
		return null;
	}
	
	public static byte parseByteArg(String arg)
	{
		return Byte.parseByte(extractHexValString(arg, 2));
	}

	public static short parseShortArg(String arg)
	{
		return Short.parseShort(extractHexValString(arg, 4));
	}

	public static byte parseSecondByteOfShort(String arg)
	{
		return Byte.parseByte(extractHexValString(arg, 2, 2));
	}

	public static int parseGlobalAddrArg(String arg)
	{
		return Short.parseShort(extractHexValString(arg, 6));
	}

	private static String extractHexValString(String arg, int numChars)
	{
		return extractHexValString(arg, numChars, 0);
	}
	
	private static String extractHexValString(String arg, int maxNumChars, int offsetChars)
	{
		int valIdx = arg.indexOf(HEX_VAL_MARKER);
		if (valIdx < 0)
		{
			throw new IllegalArgumentException("Failed to find the " + HEX_VAL_MARKER + 
					" hex value marker: " + arg);
		}
		
		// Handle shorter strings
		int endIdx = valIdx + maxNumChars + offsetChars;
		if (endIdx > arg.length())
		{
			endIdx = arg.length();
		}
		
		// Get the base string, split on space and return the first in case we overflowed into another arg
		return arg.substring(valIdx + 1 + offsetChars, endIdx).split(" ", 1)[0];
	}
	
	public static Register parseRegisterArg(String arg)
	{
		return Register.valueOf(arg.trim().toUpperCase());
	}

	public static RegisterPair parseRegisterPairArg(String arg)
	{
		return RegisterPair.valueOf(arg.trim().toUpperCase());
	}

	public static PushPopRegisterPair parsePushPopRegisterPairArg(String arg)
	{
		return PushPopRegisterPair.valueOf(arg.trim().toUpperCase());
	}
	
	public static InstructionConditions parseInstructionConditionsArg(String arg)
	{
		return InstructionConditions.valueOf(arg.trim().toUpperCase());
	}
	
	public static OneBlockText parseOneBlockTextArg(String arg)
	{
		String[] formatAndVal = arg.trim().split(":", 1);
		if (formatAndVal.length != 2)
		{
			throw new IllegalArgumentException("Malformed rom text - does not begin with format info (e.g. 'textbox' or '36,3:'): " + arg);
		}		

		int maxLines = Integer.MAX_VALUE; // Unbounded by default
		int charsPerLine = Integer.MAX_VALUE; // Unbounded by default
		switch (formatAndVal[0])
		{
			case "pokename":
				return new PokeName(formatAndVal[1]);
			case "pokedesc":
				return new PokeDescription(formatAndVal[1]);
			case "textbox":
				charsPerLine = RomConstants.MAX_CHARS_PER_LINE_TEXTBOX;
				break;
			case "halftextbox":
				charsPerLine = RomConstants.MAX_CHARS_PER_LINE_HALF_TEXTBOX;
				maxLines = RomConstants.MAX_LINES_HALF_TEXTBOX;
				break;
			default:
				String[] charsLines = formatAndVal[0].split(",");
				charsPerLine = Integer.parseInt(charsLines[0]);
				if (charsLines.length > 1)
				{
					maxLines = Integer.parseInt(charsLines[1]);
				}
				break;
		}
		
		return new OneBlockText(formatAndVal[1], charsPerLine, maxLines);
	}

	public static String formSegmentLabelArg(String arg, String rootSegment)
	{
		String trimmed = arg.trim();
		if (trimmed.startsWith(SUBSEGMENT_STARTLINE))
		{
			return rootSegment + trimmed;
		}
		// Otherwise we assume its the full name
		return trimmed;
	}
	
	public static boolean isPlaceholderLine(String line)
	{
		if (line.contains(PLACEHOLDER_MARKER))
		{
			return true;
		}
		return false;
	}
	
	public static String replacePlaceholders(String line, Map<String, String> placeholderToArgs)
	{
		for (Entry<String, String> entry : placeholderToArgs.entrySet())
		{
			line.replaceAll(PLACEHOLDER_MARKER + entry.getKey(), entry.getValue());
		}
		return line;
	}
	
	public static Instruction parseInstruction(String line, String rootSegment)
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
			// Loading
			case "lb":
				return Lb.create(args);
			case "ld":
				return Ld.create(args);
			case "ldh":
				return Ldh.create(args);
			case "ldtx":
				// we don't want to split on commas since the text
				// may have it - let it handle it itself
				return Ldtx.create(keyArgs[1]);
		
			// Logic
			case "cp":
				return Cp.create(args);
			case "or":
				return Or.create(args);				
				
			// Flow control
			case "jr":
				// JR is a bit special because we only allow it inside a block and we only
				// allow referencing labels
				return Jump.createJr(args, rootSegment);
			case "jp":
			case "farjp":
				return JumpCallCommon.create(args, rootSegment, true); // true == jump
			case "call":
			case "farcall":
				return JumpCallCommon.create(args, rootSegment, false); // false == call
			case "ret":
				return Ret.create(args);		
				
			// Misc
			case "dec":
				return Dec.create(args);
			case "inc":
				return Inc.create(args);
			case "pop":
				return Pop.create(args);
			case "push":
				return Push.create(args);
				
			// Writing raw data
				
			default:
				throw new UnsupportedOperationException("Unrecognized instruction key: " + keyArgs[0]);
		}
	}
}
