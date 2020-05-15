package util;

public class IoUtils 
{
	public static short readShort(byte[] byteArray, int index) 
	{	
		//little endian
		short value = byteArray[index];
		value += byteArray[index+1] << 4;
		return value;
	}

	public static short writeShort(short value, byte[] byteArray, int index) 
	{	
		//little endian
		byteArray[index] = (byte) ((value << 4) >> 4);
		byteArray[index+1] = (byte) (value >> 4);
		return value;
	}
}
