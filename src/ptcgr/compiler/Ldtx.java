package ptcgr.compiler;



import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import compiler.CompilerUtils;
import compiler.FixedLengthInstruct;
import compiler.CompilerConstants.RegisterPair;
import gbc_framework.QueuedWriter;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import gbc_framework.utils.ByteUtils;
import ptcgr.constants.PtcgRomConstants;
import ptcgr.data.romtexts.CardName;
import ptcgr.data.romtexts.OneBlockText;
import ptcgr.data.romtexts.PokeDescription;
import ptcgr.rom.Texts;

public class Ldtx extends FixedLengthInstruct
{
	public static final int SIZE = 3;
	RegisterPair pair;
	OneBlockText text;
	
	public Ldtx(RegisterPair pair, OneBlockText text)
	{
		super(SIZE);
		this.pair = pair;
		this.text = text;
	}
	
	public static Ldtx create(String arg)
	{	
		final String supportedArgs = "ldtx only supports (RegisterPair, String): ";	

		// Assume we have a RegisterPair first then the string
		String[] args = arg.split(",", 2);
		if (args.length != 2)
		{
			throw new IllegalArgumentException(supportedArgs + "given: " + Arrays.toString(args));
		}
		
		try
		{
			return new Ldtx(CompilerUtils.parseRegisterPairArg(args[0]), parseOneBlockTextArg(args[1]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException(supportedArgs + iae.getMessage());
		}
	}

	@Override
	public boolean containsPlaceholder() 
	{
		return false;
	}

	@Override
	public void replacePlaceholderIfPresent(Map<String, String> placeholderToArgs) 
	{
		// Nothing to do
	}
	
	public void finalizeAndAddTexts(Texts texts)
	{
		text.finalizeAndAddTexts(texts);
	}

	@Override
	public void writeFixedSizeBytes(QueuedWriter writer, BankAddress instructionAddress,
			AssignedAddresses assignedAddresses) throws IOException
	{
		// Write the instruction value then the text id
		writer.append((byte) (0x01 | (pair.getValue() << 4)));
		writer.append(ByteUtils.shortToLittleEndianBytes(text.getTextId()));
	}
	
	private static OneBlockText parseOneBlockTextArg(String arg)
	{
		String[] formatAndVal = arg.trim().split(":", 2);
		if (formatAndVal.length != 2)
		{
			throw new IllegalArgumentException("Malformed rom text - does not begin with format info (e.g. 'textbox' or '36,3:'): " + arg);
		}		

		int maxLines = Integer.MAX_VALUE; // Unbounded by default
		int charsPerLine; // Unbounded by default
		switch (formatAndVal[0].toLowerCase())
		{
			case "pokename":
				return new CardName(true, formatAndVal[1]); // true == pokename
			case "cardname":
				return new CardName(false, formatAndVal[1]); // false == not poke name
			case "pokedesc":
				return new PokeDescription(formatAndVal[1]);
			case "textbox":
				charsPerLine = PtcgRomConstants.MAX_CHARS_PER_LINE_TEXTBOX;
				break;
			case "halftextbox":
				charsPerLine = PtcgRomConstants.MAX_CHARS_PER_LINE_HALF_TEXTBOX;
				maxLines = PtcgRomConstants.MAX_LINES_HALF_TEXTBOX;
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
}
