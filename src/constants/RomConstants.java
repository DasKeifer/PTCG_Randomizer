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

	// TODO: It would potentially be more stable to read in from a location in the engine than
	// hardcoded locations in case we ever want to support adding more cards or hacks that add
	// more cards or shifted data around
	
	// Note: We have to block 0x30000 to 0x67fff that is used to store decks, cards, and text.
	// In order to add more cards, we would need to shift all the data back but as long as it
	// doesn't pass 0x67fff it shouldn't be too difficult. The one complication is that the 
	// text has a hardcoded offset of 0x4000 from 0x30000 which be hard to change and the game 
	// does this offset by setting bit 7 (i.e. adding 0x4000). We would need to figure some way
	// to replace this hopefully without having to rewrite the whole engine as changing it to the
	// 8th bit (0x8000) would be too high as there is currently only 0x3667 bytes of space before
	// the end of the text block. Also there may be other data after that that we can shift back
	// instead. You could potentially get sneaky and adjust the input by 0x2000 before multiplying 
	// by 3 to get an offset of 0x6000 and that should still be the same number of commands 
	// (i.e you wouldn't have to shift all the game logic). Regardless, that's not my current focus 
	// so I'm shelving it for now but wanted to get some thoughts down first
	
	// Does NOT start with a null pointer but pointers to unnamed decks
	public static final int DECK_POINTER_SIZE_IN_BYTES = 2;
	public static final int DECK_POINTERS_LOC = 0x30000;
	public static final int DECK_POINTER_OFFSET = 0x30000;
	
	// Starts with a null pointer
	public static final int CARD_POINTER_SIZE_IN_BYTES = 2;
	public static final int CARD_POINTERS_LOC = 0x30c5c + CARD_POINTER_SIZE_IN_BYTES;
	public static final int CARD_POINTER_OFFSET = 0x30000;
	
	// Starts with a null pointer
	public static final int TEXT_POINTER_SIZE_IN_BYTES = 3;
	public static final int TEXT_POINTERS_LOC = 0x34000 + TEXT_POINTER_SIZE_IN_BYTES; // TextOffsets
	public static final int TEXT_POINTER_OFFSET = 0x34000;
	
	// TODO Remove - read from pointers
	public static final int FIRST_CARD_BYTE = 0x30e28;
	public static final int LAST_CARD_BYTE = 0x33fff; // used for padding data as needed

	// There is alot of text that comes before this but for now we just
	// care about the card texts which are all grouped at the end
	public static final int FIRST_TEXT_BYTE = 0x3630a;
	public static final int LAST_TEXT_BYTE = 0x67fff; // Used for padding data as needed
	
	//Misc
	public static final byte[] HEADER = 
		{0x50, 0x4F, 0x4B, 0x45, 0x43, 0x41, 0x52, 0x44, 
		 0x00, 0x00, 0x00, 0x41, 0x58, 0x51, 0x45, (byte) 0x80, 
		 0x30, 0x31, 0x03, 0x1B, 0x05, 0x03, 0x01, 0x33, 
		 0x00, 0x34, 0x26, (byte) 0xA6
	};
	
}
