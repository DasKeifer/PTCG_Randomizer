package randomizer;

import java.util.Map;
import java.util.HashMap;

import constants.CardDataConstants.CardType;

public class Settings 
{
    public enum RandomizationStrategy 
    {
        UNCHANGED, SHUFFLE, RANDOM,
    }

    public class TypeSpecificData 
    {
    	// default is 0% for 0 moves, 50% for 1 move, and 50% for 2 moves
    	int[] percentWithNumMoves = new int[] {0, 50, 50};
    }

    // Poke Move Settings
	private boolean movesWithinType;
	private boolean movesMmatchPokeSpecific;
	private boolean movesMatchTypeSpecific;
	private boolean movesForceOneDamaging;
	private boolean movesForceOneNonPokePower;
	private Map<CardType, TypeSpecificData> movesTypeSpecificData;

	private boolean movesPokePowerWithMoves;
	private RandomizationStrategy movesStrat;
	private RandomizationStrategy movesPokePowerStrat;
}
