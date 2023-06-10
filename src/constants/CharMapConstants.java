package constants;

import gbc_framework.utils.ByteUtils;

public class CharMapConstants
{
	public enum CharSetPrefix
	{
		EMPTY     		(0x00),
		FULL_WIDTH_1    (0x01),
		FULL_WIDTH_2    (0x02),
		FULL_WIDTH_3    (0x03),
		FULL_WIDTH_4    (0x04),
		HALF_WIDTH      (0x06),
		HIRAGANA      	(0x0e),
		KATAKANA       	(0x0f),
		FULL_WIDTH_0    (0xff);

		private char value;
		private String valueAsString;
		private CharSetPrefix(int inChar)
		{
			if (inChar > ByteUtils.MAX_BYTE_VALUE || inChar < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "CharSetPrefix enum: " + inChar);
			}
			value = (char) inChar;
			valueAsString = "" + value;
		}

		public char getChar()
		{
			return (char) value;
		}

		public String getCharAsString()
		{
			return valueAsString;
		}

	    public static CharSetPrefix readFromByte(byte b)
	    {
	    	for (CharSetPrefix num : CharSetPrefix.values())
	    	{
	    		if (b == num.getChar())
	    		{
	    			return num;
	    		}
	    	}
	    	// Full width 0 is the default and does not have a specific
	    	// byte so if its not one of the others, its full width 0
	    	return FULL_WIDTH_0;
	    }
	}
	
	// Reserved/Special chars
	public static final char TEXT_END_CHAR = 0x0;
	public static final char SYMBOL_PREFIX_CHAR = 0x5;
	public static final char RAMNAME = 0x9;
	public static final char RAMTEXT = 0xb;
	public static final char RAMNUM = 0xc;
	
	// Some repurposed characters
	public static final char ACCENT_LOWER_CASE_E = '`';
	public static final char MALE_SYMBOL = '$';
	public static final char FEMALE_SYMBOL = '%';
	public static final char QUOTE = '\"';
	
	public enum SpecialSymbol
	{
		SPACE         	(0x00),
		FIRE         	(0x01),
		GRASS         	(0x02),
		LIGHTNING      	(0x03),
		WATER         	(0x04),
		FIGHTING      	(0x05),
		PSYCHIC        	(0x06),
		COLORLESS      	(0x07),
		POISONED   		(0x08),
		ASLEEP		    (0x09),
		CONFUSED	    (0x0a),
		PARALYZED 		(0x0b),
		CURSOR_UP 		(0x0c),
		MONSTER		    (0x0d),
		ATTACK_DESC		(0x0e),
		CURSOR_RIGHT    (0x0f),
		HP        		(0x10),
		LEVEL      		(0x11),
		E          		(0x12),
		NUMBER     		(0x13),
		PLUS_POWER 		(0x14),
		DEFENDER   		(0x15),
		HP_OK     		(0x16),
		HP_NOT_OK     	(0x17),
		BOX_TOP_L  		(0x18),
		BOX_TOP_R  		(0x19),
		BOX_BOTTOM_L  	(0x1a),
		BOX_BOTTOM_R  	(0x1b),
		BOX_TOP    		(0x1c),
		BOX_BOTTOM 		(0x1d),
		BOX_LEFT   		(0x1e),
		BOX_RIGHT  		(0x1f),
		NUMBER_0        (0x20),
		NUMBER_1        (0x21),
		NUMBER_2        (0x22),
		NUMBER_3        (0x23),
		NUMBER_4        (0x24),
		NUMBER_5        (0x25),
		NUMBER_6        (0x26),
		NUMBER_7        (0x27),
		NUMBER_8        (0x28),
		NUMBER_9        (0x29),
		DOT       		(0x2a),
		PLUS       		(0x2b),
		MINUS      		(0x2c),
		CROSS      		(0x2d),
		SLASH      		(0x2e),
		CURSOR_DOWN   	(0x2f),
		PRIZE      		(0x30);
		
		private String value;
		private SpecialSymbol(int byteVal)
		{
			if (byteVal > ByteUtils.MAX_BYTE_VALUE || byteVal < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "SpecialSymbol enum: " + byteVal);
			}
			value = SYMBOL_PREFIX_CHAR + "" + byteVal;
		}
	
		public String getString()
		{
			return value;
		}
	}
}
