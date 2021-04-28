package compiler;

import util.ByteUtils;

public class CompilerConstants 
{
	public enum Register
	{
		B	(0x0), 
		C	(0x1), 
		D	(0x2), 
		E	(0x3), 
		H	(0x4), 
		L	(0x5), 
		HL	(0x6), 
		A	(0x7), 
		NONE(0xFF);
		
		private byte value;
		
		private Register(int inValue)
		{
			// stored in upper half of byte with set in the lower half but we treat it as the lower 
			// half to make things make more sense in this code
			if (inValue > ByteUtils.MAX_HEX_CHAR_VALUE || inValue < ByteUtils.MIN_HEX_CHAR_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "InstructionConditions enum " + inValue + " (" + (inValue << 4) + " )");
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
	}
	
	public enum RegisterPair
	{
		BC	(0x0), 
		DE	(0x1), 
		HL	(0x2),
		SP	(0x3),
		NONE(0xFF);
		
		private byte value;
		
		private RegisterPair(int inValue)
		{
			// stored in upper half of byte with set in the lower half but we treat it as the lower 
			// half to make things make more sense in this code
			if (inValue > ByteUtils.MAX_HEX_CHAR_VALUE || inValue < ByteUtils.MIN_HEX_CHAR_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "InstructionConditions enum " + inValue + " (" + (inValue << 4) + " )");
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
	}
	
	public enum InstructionConditions
	{
		NZ	(0x0), 
		Z	(0x1), 
		NC	(0x2), 
		C	(0x3),
		NONE(0xFF);
		
		private byte value;
		
		private InstructionConditions(int inValue)
		{
			// stored in upper half of byte with set in the lower half but we treat it as the lower 
			// half to make things make more sense in this code
			if (inValue > ByteUtils.MAX_HEX_CHAR_VALUE || inValue < ByteUtils.MIN_HEX_CHAR_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "InstructionConditions enum " + inValue + " (" + (inValue << 4) + " )");
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
	}
}
