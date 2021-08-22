package compiler.staticInstructs.subInstructs;

import compiler.CompilerConstants.RegisterPair;
import compiler.staticInstructs.Inc;

public class IncPair extends Inc
{
	RegisterPair pair;

	public IncPair(RegisterPair pair)
	{
		super();
		this.pair = pair;
	}
	
	public void writeStaticBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt] = (byte) (0x03 | pair.getValue() << 4);
	}
}
