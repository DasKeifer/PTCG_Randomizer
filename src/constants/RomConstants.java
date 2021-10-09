package constants;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RomConstants 
{
	//Counts
	public static final int TOTAL_NUM_POKEMON_CARDS = 187;
	public static final int TOTAL_NUM_ENERGY_CARDS = 7;
	public static final int TOTAL_NUM_TRAINER_CARDS = 34;
	public static final int TOTAL_NUM_CARDS = TOTAL_NUM_POKEMON_CARDS + TOTAL_NUM_ENERGY_CARDS + TOTAL_NUM_TRAINER_CARDS;

	public static final int EFFECT_FUNCTION_POINTER_OFFSET = 0x28000;

	// Text info
	public static final int MAX_CHARS_POKE_NAME = 20; // not including starting char. For reasons poke names have to be shorter
	public static final int MAX_CHARS_CARD_NAME = 25; // not including starting char
	public static final int MAX_CHARS_POKE_CATEGORY = 20; // not including starting char // TODO later: figure out
	public static final int MAX_CHARS_MOVE_NAME = 20; // not including starting char // TODO later: figure out
	public static final int MAX_CHARS_PER_LINE_CARD = 36; // Not including newline or starting char 
	public static final int MAX_CHARS_PER_LINE_TEXTBOX = 36; // Not including newline or starting char // TODO later: figure out
	public static final int MAX_CHARS_PER_LINE_HALF_TEXTBOX = 20; // Not including newline or starting char // TODO later: figure out
	
	public static final int MAX_LINES_POKE_DESC = 4;
	public static final int MAX_LINES_HALF_TEXTBOX = 2;
	public static final int PREFERRED_LINES_PER_BLOCK_EFFECT_DESC = 6;
	public static final int MAX_LINES_PER_BLOCK_EFFECT_DESC = 7;
	public static final int MAX_BLOCKS_EFFECT_DESC = 2;
	
	
	//Locations
	public static final int HEADER_LOCATION = 0x134;
	
	public static final int BANK_SIZE = 0x4000;
	public static final byte NUMBER_OF_BANKS = 64;

	// TODO later: It would potentially be more stable to read in from a location in the engine than
	// hardcoded locations in case we ever want to support adding more cards or hacks that add
	// more cards or shifted data around. Not sure how easy that would be though
	
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
	public static final int CARD_POINTER_OFFSET = 0x2c000; // Don't ask my why its this an not 0x30000... It just is
	
	// Starts with a null pointer
	public static final int TEXT_ID_SIZE_IN_BYTES = 2;
	public static final int TEXT_POINTER_SIZE_IN_BYTES = 3;
	public static final int TEXT_POINTERS_LOC = 0x34000; // TextOffsets
	public static final int TEXT_POINTER_OFFSET = 0x34000;

	// There is alot of text that comes before this but for now we just
	// care about the card texts which are all grouped at the end
	public static final int FIRST_TEXT_BYTE = 0x3630a;
	public static final int LAST_TEXT_BYTE = 0x67fff; // Used for padding data as needed
	
	// Misspelled card names
	public static final Map<String, String> MISPELLED_CARD_NAMES;
    static {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("Ninetails", "Ninetales");
        MISPELLED_CARD_NAMES = Collections.unmodifiableMap(tempMap);
    }
    
    // "Damaging" attacks that have no damage number associated with them
	public static final Set<String> ZERO_DAMAGE_DAMAGING_MOVES;
    static {
    	Set<String> tempSet = new HashSet<>();
    	tempSet.add("Ice Breath"); // Does 40 damage to 1 of your opponent's Pok`mon chosen at random. Don't apply Weakness and Resistance for this attack...
    	tempSet.add("Big Thunder"); // Choose a Pok`mon other than  at random. This attack does 70 damage to that Pok`mon. Don't apply Weakness and Resistance for this attack. (Any other effects that would happen after applying Weakness and Resistance still happen.)
    	tempSet.add("Stretch Kick"); // If your opponent has any Benched Pok`mon, choose 1 of them and this attack does 20 damage to it. (Don't apply Weakness and Resistance for Benched Pok`mon.)
    	tempSet.add("Mystery Attack"); // Does a random amount of damage to the Defending Pok`mon and may cause a random effect to the Defending Pok`mon.
    	tempSet.add("Slicing Wind"); // Does 30 damage to 1 of your opponent's Pok`mon chosen at random. Don't apply Weakness and Resistance for this attack... 
    	tempSet.add("Super Fang"); //  Does damage to the Defending Pok`mon equal to half the Defending Pok`mon's remaining HP (rounded up to the nearest 10).
    	tempSet.add("Metronome"); // Choose 1 of the Defending Pok`mon's attacks. Metronome copies that attack except for its Energy costs. (No matter what type the Defending Pokemon is, 's type is still Colorless.)
    	tempSet.add("Cat Punch"); // Does 20 damage to 1 of your opponent's Pok`mon chosen at random. Don't apply Weakness and Resistance for this attack...
    	
    	// Status that can inflict damage
    	tempSet.add("Spit Poison"); // Flip a coin. If heads, the Defending Pok`mon is now Poisoned.
    	tempSet.add("Poisonpowder"); // The Defending Pok`mon is now Poisoned.

    	// Possibilities not added to the list
    	// tempSet.put("Supersonic"); // Flip a coin. If heads, the Defending Pok`mon is now Confused.
 		// Mirror Move DAMAGE_NORMAL -* If  was attacked last turn, do the final result of that attack on  to the Defending Pok`mon.
    	ZERO_DAMAGE_DAMAGING_MOVES = Collections.unmodifiableSet(tempSet); 
    }
    
	//Misc
	public static final byte[] HEADER = 
		{0x50, 0x4F, 0x4B, 0x45, 0x43, 0x41, 0x52, 0x44, 
		 0x00, 0x00, 0x00, 0x41, 0x58, 0x51, 0x45, (byte) 0x80, 
		 0x30, 0x31, 0x03, 0x1B, 0x05, 0x03, 0x01, 0x33, 
		 0x00, 0x34, 0x26, (byte) 0xA6
	};
}
