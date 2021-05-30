package constants;

import util.ByteUtils;

public class CharMapConstants
{
	// Reserved/Special chars
	public static final char TEXT_END_CHAR = 0x0;
	public static final char SYMBOL_PREFIX_CHAR = 0x5;
	public static final char HALFWIDTH_TEXT_PREFIX_CHAR = 0x6;
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
		Space         	(0x00),
		Fire         	(0x01),
		Grass         	(0x02),
		Lightning      	(0x03),
		Water         	(0x04),
		Fighting      	(0x05),
		Psychic        	(0x06),
		Colorless      	(0x07),
		Poisoned   		(0x08),
		Asleep		    (0x09),
		Confused	    (0x0a),
		Paralyzed 		(0x0b),
		CursorUp   		(0x0c),
		Pkmn		    (0x0d),
		AttackDesc		(0x0e),
		CursorRight	    (0x0f),
		HP        		(0x10),
		Lv         		(0x11),
		E          		(0x12),
		No         		(0x13),
		Pluspower  		(0x14),
		Defender   		(0x15),
		HPOk     		(0x16),
		HPNotOk     	(0x17),
		BoxTopL  		(0x18),
		BoxTopR  		(0x19),
		BoxBottomL  	(0x1a),
		BoxBottomR  	(0x1b),
		BoxTop    		(0x1c),
		BoxBottom 		(0x1d),
		BoxLeft   		(0x1e),
		BoxRight  		(0x1f),
		Number0         (0x20),
		Number1         (0x21),
		Number2         (0x22),
		Number3         (0x23),
		Number4         (0x24),
		Number5         (0x25),
		Number6         (0x26),
		Number7         (0x27),
		Number8         (0x28),
		Number9         (0x29),
		Dot       		(0x2a),
		Plus       		(0x2b),
		Minus      		(0x2c),
		Cross      		(0x2d),
		Slash      		(0x2e),
		CursorDown   	(0x2f),
		Prize      		(0x30);
		
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
