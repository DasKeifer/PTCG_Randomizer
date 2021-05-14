package compiler;


import java.util.Map;
import java.util.Map.Entry;

import compiler.CompilerConstants.*;
import compiler.dynamic.Jump;
import compiler.dynamic.JumpCallCommon;
import compiler.dynamic.Ldtx;
import compiler.fixed.Cp;
import compiler.fixed.Inc;
import compiler.fixed.Lb;
import compiler.fixed.Ld;
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
	
	// TODO handle shortened hex values?
	private static String extractHexValString(String arg, int numChars, int offsetChars)
	{
		int valIdx = arg.indexOf(HEX_VAL_MARKER);
		try
		{
			return arg.substring(valIdx + 1 + offsetChars, valIdx + numChars + offsetChars);
		} 
		catch (IndexOutOfBoundsException iobe)
		{
			throw new IllegalArgumentException("Failed to find the " + HEX_VAL_MARKER + 
					" hex value marker or " + numChars + " characters were found after the " + 
					offsetChars + " character offset: " + arg);
		}
	}
	
	// TODO: Parse brackets? How do we handle those
	
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
	
	// TODO - make only a verify instead of a parse - i.e. no markers for placeholders?
	// We do possibly want some parsing for addresses so we can handle ".name" type ones
	public static OneBlockText parseOneBlockTextArg(String arg)
	{
		// TODO: add prefixes so we know what type of RomText to use
		// Maybe input unformatted and let the utils handle formatting it
		// based on prefixes? To do this I need to research what is allowed in
		// text first - i.e. line lengths, max blocks etc. and make more romTexts
		// to allow this. I think all text cannot use line breaks and that is something
		// I added to support things like the pokemon descriptions
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
			// TODO: complete this list
			// Loading
			case "lb":
				return Lb.create(args);
			case "ld":
				return Ld.create(args);
			case "ldtx":
				// we don't want to split on commas since the text
				// may have it - let it handle it itself
				return Ldtx.create(keyArgs[1]);
		
			// Logic
			case "cp":
				return Cp.create(args);
			case "inc":
				return Inc.create(args);
				
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
				
			// Writing raw data
				
			default:
				throw new UnsupportedOperationException("Unrecognized instruction key: " + keyArgs[0]);
		}
	}
}
