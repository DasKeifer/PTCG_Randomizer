package constants;

public class RomConstants 
{
	//Counts
	public static final int TOTAL_NUM_POKEMON_CARDS = 187;
	public static final int TOTAL_NUM_ENERGY_CARDS = 7;
	public static final int TOTAL_NUM_TRAINER_CARDS = 34;
	public static final int TOTAL_NUM_CARDS = TOTAL_NUM_POKEMON_CARDS + TOTAL_NUM_ENERGY_CARDS + TOTAL_NUM_TRAINER_CARDS;
	
	//Locations
	public static final int HEADER_LOCATION = 0x134;
	public static final int FIRST_CARD = 0x30e28;
	
	//Misc
	public static final byte[] HEADER = 
		{0x50, 0x4F, 0x4B, 0x45, 0x43, 0x41, 0x52, 0x44, 
		 0x00, 0x00, 0x00, 0x41, 0x58, 0x51, 0x45, (byte) 0x80, 
		 0x30, 0x31, 0x03, 0x1B, 0x05, 0x03, 0x01, 0x33, 
		 0x00, 0x34, 0x26, (byte) 0xA6
	};
	
}
