package util;

public class ByteUtils 
{
	public static final int MAX_BYTE_VALUE = 0xff;
	public static final int MIN_BYTE_VALUE = 0;

	public static final int MAX_HEX_CHAR_VALUE = 0xf;
	public static final int BYTE_UPPER_HEX_CHAR_MASK = 0xf0;
	public static final int BYTE_LOWER_HEX_CHAR_MASK = 0x0f;

	public static void printBytes(byte[] bytes, int index, int bytesPerNumber, int numberToPrint)
	{
		printBytes(bytes, index, bytesPerNumber, numberToPrint, true);
	}
	
	public static void printBytes(byte[] bytes, int index, int bytesPerNumber, int numberToPrint, boolean littleEndian)
	{
		String formatString = "0x%" + bytesPerNumber*2 + "X";
		for (int i = 0; i < numberToPrint; i++)
		{
			int number = 0;
			for (int j = bytesPerNumber - 1; j >= 0; j--)
			{
				number = number << 8;
				number += bytes[index + i * bytesPerNumber + j];
			}
			System.out.println(String.format(formatString, number));
		}
	}
	
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
		return  (short) (byteArray[index] + 
				         byteArray[index + 1] << 8);
	}

	public static void writeAsShort(short value, byte[] byteArray, int index) 
	{	
		//little endian
		byteArray[index] =     (byte) (value & 0x00ff);
		byteArray[index + 1] = (byte) (value & 0xff00);
	}
	
	public static int readAsTriplet(byte[] byteArray, int index) 
	{	
		//little endian
		return  byteArray[index] + 
				byteArray[index + 1] << 8 +
				byteArray[index + 1] << 16;
	}

	public static void writeAsTriplet(short value, byte[] byteArray, int index) 
	{	
		//little endian
		byteArray[index] =     (byte) (value & 0x0000ff);
		byteArray[index + 1] = (byte) (value & 0x00ff00);
		byteArray[index + 2] = (byte) (value & 0xff0000);
	}
}
