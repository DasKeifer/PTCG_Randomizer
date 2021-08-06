package compiler;


import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import compiler.CompilerConstants.*;
import compiler.dynamicInstructs.*;
import compiler.staticInstructs.*;
import constants.RomConstants;
import data.romtexts.*;
import util.ByteUtils;

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
	
	public static boolean isOnlySubsegmentPartOfLabel(String line)
	{
		return line.startsWith(CompilerUtils.SUBSEGMENT_STARTLINE);
	}
	
	public static String tryParseFullSubsegmentName(String line, String rootSegmentName)
	{
		if (isOnlySubsegmentPartOfLabel(line))
		{
			return getSubsegmentName(line, rootSegmentName);
		}
		return null;
	}
	
	public static String formSubsegmentName(String subsegment, String rootSegmentName)
	{
		return rootSegmentName + "." + subsegment;
	}
	
	private static String getSubsegmentName(String line, String rootSegmentName)
	{
		return rootSegmentName + line.trim();
	}
	
	private static String tryParseBracketedArg(String arg)
	{
		arg = arg.trim();
		if (arg.startsWith("[") && arg.endsWith("]"))
		{
			// only -1 because the end is exclusive
			return arg.substring(1, arg.length() - 1);
		}
		
		return null;
	}
	
	public static byte parseByteArg(String arg)
	{
		return ByteUtils.parseByte(extractHexValString(arg, 2));
	}

	public static short parseShortArg(String arg)
	{
		return (short) ByteUtils.parseBytes(extractHexValString(arg, 4), 2);
	}

	public static byte parseSecondByteOfShort(String arg)
	{
		return ByteUtils.parseByte(extractHexValString(arg, 2, 2));
	}

	public static int parseGlobalAddrArg(String arg)
	{
		return (int) ByteUtils.parseBytes(extractHexValString(arg, 6), 3);
	}

	private static String extractHexValString(String arg, int numChars)
	{
		return extractHexValString(arg, numChars, 0);
	}

	public static boolean isHexArg(String arg)
	{
		return arg.contains(HEX_VAL_MARKER);
	}
	
	private static String extractHexValString(String arg, int maxNumChars, int offsetChars)
	{
		int valIdx = arg.indexOf(HEX_VAL_MARKER) + 1;
		if (valIdx <= 0)
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
		return arg.substring(valIdx + offsetChars, endIdx).split(" ", 2)[0];
	}
	
	public static Register parseRegisterArg(String arg)
	{
		if (arg.trim().equalsIgnoreCase("[hl]"))
		{
			return Register.BRACKET_HL_BRACKET;
		}
		return Register.valueOf(arg.trim().toUpperCase());
	}
	
	public static boolean parseHLIncDecArg(String arg)
	{
		if (arg.trim().equalsIgnoreCase("[hli]"))
		{
			return true;
		}
		else if (arg.trim().equalsIgnoreCase("[hld]"))
		{
			return false;
		}
		
		throw new IllegalArgumentException("Passed arg is not [hli] or [hld]: " + arg);
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
	
	public static short parseMemoryAddressArg(String arg)
	{
		arg = tryParseBracketedArg(arg);
		if (arg != null)
		{
			return parseShortArg(arg);
		}
		throw new IllegalArgumentException("Passed arg is not surrounded in brackets so it is not a valid memory address: " + arg);
	}
	
	public static OneBlockText parseOneBlockTextArg(String arg)
	{
		String[] formatAndVal = arg.trim().split(":", 2);
		if (formatAndVal.length != 2)
		{
			throw new IllegalArgumentException("Malformed rom text - does not begin with format info (e.g. 'textbox' or '36,3:'): " + arg);
		}		

		int maxLines = Integer.MAX_VALUE; // Unbounded by default
		int charsPerLine = Integer.MAX_VALUE; // Unbounded by default
		switch (formatAndVal[0].toLowerCase())
		{
			case "pokename":
				return new CardName(true, formatAndVal[1]); // true == pokename
			case "cardname":
				return new CardName(false, formatAndVal[1]); // false == not poke name
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
	
	public static boolean containsPlaceholder(String line)
	{
		if (line.contains(PLACEHOLDER_MARKER))
		{
			return true;
		}
		return false;
	}
	
	public static String createPlaceholder(String placeholderId)
	{
		return PLACEHOLDER_MARKER + placeholderId + PLACEHOLDER_MARKER;
	}
	
	public static String replacePlaceholders(String line, Map<String, String> placeholderToArgs)
	{
		for (Entry<String, String> entry : placeholderToArgs.entrySet())
		{
			if (!entry.getKey().startsWith(PLACEHOLDER_MARKER) || !entry.getKey().endsWith(PLACEHOLDER_MARKER))
			{
				throw new IllegalArgumentException("Non placeholder key passed: " + entry.getKey());
			}
			line = line.replace(entry.getKey(), entry.getValue());
		}
		return line;
	}
	
	private static String[] splitInstruction(String line)
	{
		// Split and trim the array
		return Arrays.stream(line.split(" ", 2)).map(String::trim).toArray(String[]::new);
	}

	private static String[] splitArgs(String[] keyArgs)
	{
		if (keyArgs.length > 1)
		{
			// Split and trim the array
			return Arrays.stream(keyArgs[1].split(",")).map(String::trim).toArray(String[]::new);
		}
		return new String[0];
	}
	
	public static boolean containsImplicitPlaceholder(String line, String rootSegment)
	{
		if (!containsPlaceholder(rootSegment))
		{
			return false;
		}
		
		String[] keyArgs = splitInstruction(line);
		String[] args = splitArgs(keyArgs);
		
		// Any instruction that takes the root segment may have a hidden placeholder
		switch (keyArgs[0])
		{
			case "jr":
			case "jp":
			case "farjump":
				return JumpCallCommon.useRootSegment(args, true);
			case "call":
			case "farcall":
				return JumpCallCommon.useRootSegment(args, false);
			default:
				return false;
		}
	}
	
	public static Instruction parseInstruction(String line, String rootSegment)
	{		
		// Split the keyword off and then the args apart
		String[] keyArgs = splitInstruction(line);
		String[] args = splitArgs(keyArgs);
		
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
			case "bank1call":
				return BankCall1.create(args);
				
			// Arithmetic
			case "dec":
				return Dec.create(args);
			case "inc":
				return Inc.create(args);
			case "sub":
				return Sub.create(args);
				
			// Misc
			case "pop":
				return Pop.create(args);
			case "push":
				return Push.create(args);
			case "nop":
				return Nop.create(args);
				
			// Writing raw data
				
			default:
				throw new UnsupportedOperationException("Unrecognized instruction key: " + keyArgs[0]);
		}
	}
}
