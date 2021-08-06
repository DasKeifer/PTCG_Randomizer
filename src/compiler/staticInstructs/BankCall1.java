package compiler.staticInstructs;

import compiler.CompilerUtils;
import compiler.StaticInstruction;
import util.ByteUtils;

public class BankCall1 extends StaticInstruction
{
	public static final int SIZE = 3;
	short value;

	public BankCall1(short bank1Address)
	{
		super(SIZE);
		this.value = bank1Address;
	}
	
	public static BankCall1 create(String[] args)
	{		
		final String allowedArgs = "BankCall1 only supports (short): ";
		if (args.length != 1)
		{
			throw new IllegalArgumentException(allowedArgs + args.toString());
		}
		
		try
		{
			return new BankCall1(CompilerUtils.parseShortArg(args[0]));
		}
		catch (IllegalArgumentException iae)
		{
			throw new IllegalArgumentException(allowedArgs + iae.getMessage());
		}
	}
	
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt)
	{
		// bankcall1 is in RST 18
		bytes[indexToAddAt++] = (byte) (0xC7 | 0x18); 
		ByteUtils.writeAsShort(value, bytes, indexToAddAt);
	}
}
