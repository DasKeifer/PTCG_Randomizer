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
		String formatString = "0x%" + bytesPerNumber*2 + "X";
		for (int i = 0; i < numberToPrint; i++)
		{
			System.out.println(String.format(formatString, 
					readLittleEndian(bytes, index + i * bytesPerNumber, bytesPerNumber)));
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
		return  (short) readLittleEndian(byteArray, index, 2);
	}

	public static void writeAsShort(short value, byte[] byteArray, int index) 
	{	
		writeLittleEndian(value, byteArray, index, 2);
	}
	
	public static long readLittleEndian(byte[] byteArray, int index, int numBytes) 
	{	
		long number = 0;
		for (int j = numBytes - 1; j >= 0; j--)
		{
			number = number << 8;
			// Its a pain because bytes are signed so we need to make sure when the
			// byte is promoted here that it only takes the last digits or else if its
			// > byte's max signed value, it will add FFs to promote it and keep the
			// same negative value whereas we only want the byte values
			number |= byteArray[index + j] & 0xff;
		}
		return number;
	}

	public static void writeLittleEndian(long value, byte[] byteArray, int index, int numBytes) 
	{	
		for (int j = 0; j < numBytes; j++)
		{
			byteArray[index + j] = (byte) (value & 0xff);
			value = value >> 8;
		}
	}
}
