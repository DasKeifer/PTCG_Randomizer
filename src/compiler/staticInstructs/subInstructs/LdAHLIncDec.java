package compiler.staticInstructs.subInstructs;

import compiler.CompilerUtils;
import compiler.CompilerConstants.Register;
import compiler.staticInstructs.Ld;

public class LdAHLIncDec extends Ld
{
	boolean loadToA;
	boolean increment;
	
	public LdAHLIncDec(boolean loadToA, boolean increment)
	{
		super(1); // size
		this.loadToA = loadToA;
		this.increment = increment;
	}

	public static LdAHLIncDec create(String[] args)
	{
		final String supportedArgs = "LdAHLIncDec only supports (a, [hli or hld]) or ([hli or hld], a): ";
		if (args.length != 2)
		{
			throw new IllegalArgumentException(supportedArgs + "Given " + args.toString());
		}
		
		boolean isLoad = false;
		boolean increment = false;
		
		// TODO need to add try catch or something
		
		// First arg is A?
		if (CompilerUtils.parseRegisterArg(args[0]) == Register.A)
		{
			isLoad = true;
			increment = CompilerUtils.parseHLIncDecArg(args[1]);
		}
		// First arg is presumably [hl] and second is A
		else
		{
			// Is load already set to false
			if (CompilerUtils.parseRegisterArg(args[1]) != Register.A)
			{
				throw new IllegalArgumentException(supportedArgs + "Given " + args.toString());
			}
			increment = CompilerUtils.parseHLIncDecArg(args[0]);
		}
		
		return new LdAHLIncDec(isLoad, increment);
	}
	
	@Override
	public void writeStaticBytes(byte[] bytes, int indexToWriteAt) 
	{
		byte val = 0x2;
		if (loadToA)
		{
			val = 0xA;
		}
		if (increment)
		{
			val |= 2 << 4;
		}
		else
		{
			val |= 3 << 4;
		}
		
		bytes[indexToWriteAt] = val;
	}
}
