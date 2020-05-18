package util;

public class ByteUtils 
{
	public static final int MAX_BYTE_VALUE = 0xff;
	public static final int MIN_BYTE_VALUE = 0;

	public static final int MAX_HEX_CHAR_VALUE = 0xf;
	public static final int BYTE_UPPER_HEX_CHAR_MASK = 0xf0;
	public static final int BYTE_LOWER_HEX_CHAR_MASK = 0x0f;
	
	public static byte readUpperHexChar(byte value)
	{
		return (byte) ((value & BYTE_UPPER_HEX_CHAR_MASK) >> 4);
	}
	
	public static byte readLowerHexChar(byte value)
	{
		return (byte) (value & BYTE_LOWER_HEX_CHAR_MASK);
	}
	
	public static byte packHexCharsToByte(byte upper, byte lower)
	{
		return (byte) (upper << 4 & 0xff | lower);
	}
	
	
	public static short readAsShort(byte[] byteArray, int index) 
	{	
		//little endian
		short value = byteArray[index];
		value += byteArray[index+1] << 4;
		return value;
	}

	public static short writeAsShort(short value, byte[] byteArray, int index) 
	{	
		//little endian
		byteArray[index] = (byte) (value & 0x0f);
		byteArray[index+1] = (byte) (value & 0xf0);
		return value;
	}
}
