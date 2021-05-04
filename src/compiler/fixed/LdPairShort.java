package compiler.fixed;

import compiler.CompilerConstants.RegisterPair;
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
	public int writeBytes(byte[] bytes, int indexToAddAt)
	{
		bytes[indexToAddAt++] = (byte) (0x01 | (pair.getValue() << 4));
		
		ByteUtils.writeAsShort(value, bytes, indexToAddAt);
		return indexToAddAt + 2;
	}
}
