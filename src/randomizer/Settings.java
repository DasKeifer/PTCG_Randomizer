package randomizer;

import java.util.Map;
import constants.CardDataConstants.CardType;

public class Settings 
{
	// TODO: Generation is very stretch - don't worry too much about for now
    public enum RandomizationStrategy 
    {
        UNCHANGED, SHUFFLE, RANDOM, GENERATED
    }

    public class TypeSpecificData 
    {
    	// Only if RANDOM or GENERATED
    	// TODO get from cards - only applicable for Random or generated
    	int[] percentWithNumMoves = new int[] {0, 50, 50}; // Need to do logic to make sure it matches with two following ones
    	int[] percentWithNumAttacks = new int[] {0, 50, 30};
    	int[] percentWithNumPowers = new int[] {0, 20, 0};
    }
    
    // Poke Move specific settings 
	private RandomizationStrategy movesStrat;
	private Map<CardType, TypeSpecificData> movesTypeSpecificData;
	
    // applicable if not UNCHANGED
	private boolean movesKeepSameNumberOfAttacks;
	private boolean movesForceOneDamaging; // If keep same number of attacks is off
	private boolean movesAttacksWithinType; // Effects if GENERATED
	
	
	// Poke power specific (applicable if not UNCHANGED)
	private RandomizationStrategy movesPokePowerStrat;
	
	// applicable if not UNCHANGED
	private boolean movesKeepSameNumberOfPowers;
	private boolean movesPowersWithinType; // Effects if GENERATED

	// Think these can be removed - but may be simpler to keep for now
    // Move/Power Applicable (both use the same) (applicable if not UNCHANGED)
	// private boolean movesMatchPokeSpecific;
	// private boolean movesMatchTypeSpecific;
}
