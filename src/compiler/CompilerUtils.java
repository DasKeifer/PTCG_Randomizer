package compiler;

import compiler.CompilerConstants.*;

public class CompilerUtils 
{
	public static final int UNASSIGNED_ADDRESS = -1;
	public static final int UNASSIGNED_LOCAL_ADDRESS = -2;
	static final String SEGMENT_ENDLINE = ":";
	static final String SUBSEGMENT_STARTLINE = ".";
	static final String LINE_BREAK = "\n";
	static final String HEX_VAL_MARKER = "$";
	
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
	public static String parseTextPlaceholder(String arg)
	{
		
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
}
