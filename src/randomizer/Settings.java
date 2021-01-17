package randomizer;

import java.util.EnumMap;
import java.util.Map;
import constants.CardDataConstants.CardType;

public class Settings 
{
	// TODO: Generation is very stretch - don't worry too much about for now
    public enum RandomizationStrategy 
    {
        INVALID, UNCHANGED, SHUFFLE, RANDOM, GENERATED;
        
        public static RandomizationStrategy getByName(String name)
        {
        	for (RandomizationStrategy strat : RandomizationStrategy.values())
        	{
        		if (strat.name().equals(name))
        		{
        			return strat;
        		}
        	}
        	
        	return INVALID;
        }
    }

    // TODO: Once these settle down some more, move them into separate files
    public static class TypeSpecificData 
    {
    	// Only if RANDOM or GENERATED
    	// TODO get from cards - only applicable for Random or generated
    	int[] percentWithNumMoves = new int[] {0, 50, 50}; // Need to do logic to make sure it matches with two following ones
    	int[] percentWithNumAttacks = new int[] {0, 50, 30};
    	int[] percentWithNumPowers = new int[] {0, 20, 0};
    }
    
    public static class SpecificDataPerType
    {
    	Map<CardType, TypeSpecificData> data = new EnumMap<>(CardType.class);
    }
    
    public static class MovesData
    {
		// Poke Move specific settings 
    	private RandomizationStrategy movesStrat;
    	
        // applicable if not UNCHANGED
    	private boolean movesForceOneDamaging; // If keep same number of attacks is off
    	private boolean movesAttacksWithinType; // Effects if GENERATED
    	
        public RandomizationStrategy getMovesStrat() {
			return movesStrat;
		}
		public void setMovesStrat(RandomizationStrategy movesStrat) {
			this.movesStrat = movesStrat;
		}
		public void setMovesStrat(String movesStratName) {
			this.movesStrat = RandomizationStrategy.getByName(movesStratName);
		}
		public boolean isMovesForceOneDamaging() {
			return movesForceOneDamaging;
		}
		public void setMovesForceOneDamaging(boolean movesForceOneDamaging) {
			this.movesForceOneDamaging = movesForceOneDamaging;
		}
		public boolean isMovesAttacksWithinType() {
			return movesAttacksWithinType;
		}
		public void setMovesAttacksWithinType(boolean movesAttacksWithinType) {
			this.movesAttacksWithinType = movesAttacksWithinType;
		}
    }
    
    public static class PokePowersData
    {
    	private boolean includeWithMoves;
    	
		// Poke power specific (applicable if not UNCHANGED)
    	private RandomizationStrategy movesPokePowerStrat;
    	
    	// applicable if not UNCHANGED
    	private boolean movesPowersWithinType; // Effects if GENERATED

		public boolean isIncludeWithMoves() {
			return includeWithMoves;
		}
		public void setIncludeWithMoves(boolean includeWithMoves) {
			this.includeWithMoves = includeWithMoves;
		}
    	public RandomizationStrategy getMovesPokePowerStrat() {
			return movesPokePowerStrat;
		}
		public void setMovesPokePowerStrat(RandomizationStrategy movesPokePowerStrat) {
			this.movesPokePowerStrat = movesPokePowerStrat;
		}
		public void setMovesPokePowerStrat(String movesPokePowerStratName) {
			this.movesPokePowerStrat = RandomizationStrategy.getByName(movesPokePowerStratName);
		}
		public boolean isMovesPowersWithinType() {
			return movesPowersWithinType;
		}
		public void setMovesPowersWithinType(boolean movesPowersWithinType) {
			this.movesPowersWithinType = movesPowersWithinType;
		}
    }
    
    private boolean logSeed;
    private boolean logDetails;
    
	private SpecificDataPerType specificDataPerType;
	private MovesData moves;
	private PokePowersData pokePowers;
	 
	private boolean movesRandomNumberOfAttacks;
	
	// TODO: Long term I want these to be randomizable
	// Think these can be removed - but may be simpler to keep for now
    // Move/Power Applicable (both use the same) (applicable if not UNCHANGED)
	private boolean movesMatchPokeSpecific;
	private boolean movesMatchTypeSpecific;

	public SpecificDataPerType getTypeSpecificData() {
		return specificDataPerType;
	}
	public void setTypeSpecificData(SpecificDataPerType specificDataPerType) {
		this.specificDataPerType = specificDataPerType;
	}
	public MovesData getMoves() {
		return moves;
	}
	public void setMoves(MovesData moves) {
		this.moves = moves;
	}
	public PokePowersData getPokePowers() {
		return pokePowers;
	}
	public void setPokePowers(PokePowersData pokePowers) {
		this.pokePowers = pokePowers;
	}
	public boolean isMovesRandomNumberOfAttacks() {
		return movesRandomNumberOfAttacks;
	}
	public void setMovesRandomNumberOfAttacks(boolean movesRandomNumberOfAttacks) {
		this.movesRandomNumberOfAttacks = movesRandomNumberOfAttacks;
	}
	// TODO: Temp setting
	public boolean isMovesMatchPokeSpecific() {
		return movesMatchPokeSpecific;
	}
	public void setMovesMatchPokeSpecific(boolean movesMatchPokeSpecific) {
		this.movesMatchPokeSpecific = movesMatchPokeSpecific;
	}    
	public boolean isMovesMatchTypeSpecific() {
		return movesMatchTypeSpecific;
	}
	public void setMovesMatchTypeSpecific(boolean movesMatchTypeSpecific) {
		this.movesMatchTypeSpecific = movesMatchTypeSpecific;
	}
	public boolean isLogSeed() {
		return logSeed;
	}
	public void setLogSeed(boolean logSeed) {
		this.logSeed = logSeed;
	}
	public boolean isLogDetails() {
		return logDetails;
	}
	public void setLogDetails(boolean logDetails) {
		this.logDetails = logDetails;
	}
}
