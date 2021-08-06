package compiler.staticInstructs.subInstructs;

import compiler.CompilerConstants.RegisterPair;
import compiler.staticInstructs.Ld;
import util.ByteUtils;

public class LdPairShort extends Ld
{
	RegisterPair pair;
	short value;
	
	public LdPairShort(RegisterPair pair, short value)
	{
		super(3); // size
		this.pair = pair;
		this.value = value;
	}
	
	@Override
	public void writeFixedSizeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) (0x01 | (pair.getValue() << 4));
		
		ByteUtils.writeAsShort(value, bytes, indexToAddAt);
	}
}
