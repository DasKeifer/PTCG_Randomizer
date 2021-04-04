package randomizer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

import constants.CardDataConstants.CardType;
import data.PokemonCard.MoveCategories;

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
    
    public enum MoveTypeChanges
    {
    	INVALID, UNCHANGED, MATCH_CARD_TYPE, ALL_COLORLESS;
        
        public static MoveTypeChanges getByName(String name)
        {
        	for (MoveTypeChanges change : MoveTypeChanges.values())
        	{
        		if (change.name().equals(name))
        		{
        			return change;
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
    	private RandomizationStrategy randomizationStrat;
    	
    	// applicable if not UNCHANGED
    	private boolean randomizationWithinType; // Effects if GENERATED

        public RandomizationStrategy getRandomizationStrat() {
			return randomizationStrat;
		}
		public void setRandomizationStrat(RandomizationStrategy randomizationStrat) {
			this.randomizationStrat = randomizationStrat;
		}
		public void setRandomizationStrat(String randomizationStratName) {
			this.randomizationStrat = RandomizationStrategy.getByName(randomizationStratName);
		}
		public boolean isRandomizationWithinType() {
			return randomizationWithinType;
		}
		public void setRandomizationWithinType(boolean withinType) {
			this.randomizationWithinType = withinType;
		}
    }
    
    public static class AttacksData extends MovesData
    {
        // applicable if not UNCHANGED
    	private boolean forceOneDamagingAttack; // If keep same number of attacks is off?
    	
    	// Applicable always - maybe also applicable to poke powers?
    	private MoveTypeChanges moveTypeChanges;
    	
		public boolean isForceOneDamagingAttack() {
			return forceOneDamagingAttack;
		}
		public void setForceOneDamagingAttack(boolean forceOneDamagingAttack) {
			this.forceOneDamagingAttack = forceOneDamagingAttack;
		}
		public MoveTypeChanges getMoveTypeChanges() {
			return moveTypeChanges;
		}
		public void setMoveTypeChanges(String moveTypeChangeName) {
			this.moveTypeChanges = MoveTypeChanges.getByName(moveTypeChangeName);
		}
    }
    
    public static class PokePowersData extends MovesData
    {
    	private boolean includeWithMoves;

		public boolean isIncludeWithMoves() {
			return includeWithMoves;
		}
		public void setIncludeWithMoves(boolean includeWithMoves) {
			this.includeWithMoves = includeWithMoves;
		}
    }
    
    private Random rand = new Random();
    private String seed = String.valueOf(rand.nextInt());
    private boolean logSeed;
    private boolean logDetails;
    
	private SpecificDataPerType specificDataPerType;
	private AttacksData attacks;
	private PokePowersData pokePowers;
	 
	private boolean movesRandomNumberOfAttacks;
	
	// Long term I want these to be randomizable/modifiable. I know for some like discarding a card 
	// type that shouldn't be difficult but others may cause problems
	// Think these can be removed - but may be simpler to keep for now
    // Move/Power Applicable (both use the same) (applicable if not UNCHANGED)
	private boolean movesMatchPokeSpecific;
	private boolean movesMatchTypeSpecific;
	
	
	public int getSeedValue()
	{
		// Try to treat it as a number first
		try
		{
			return Integer.parseInt(seed);
		}
		// If its not a valid int, just hash it
		catch (NumberFormatException nfe)
		{
			return seed.hashCode();
		}
	}
	public void setSeed(String seed) {
		// If its empty or "random" then generate a random seed for them
		if (seed.trim().isEmpty() || seed.equalsIgnoreCase("random"))
		{
			this.seed = String.valueOf(rand.nextInt());
		}
		// Otherwise save their seed
		else
		{
			this.seed = seed;
		}
	}

	public SpecificDataPerType getTypeSpecificData() {
		return specificDataPerType;
	}
	public void setTypeSpecificData(SpecificDataPerType specificDataPerType) {
		this.specificDataPerType = specificDataPerType;
	}
	public MovesData getMoves(MoveCategories moveCategory) 
	{
		if (MoveCategories.ALL == moveCategory || MoveCategories.ATTACK == moveCategory)
		{
			return getAttacks();
		}
		else
		{
			return getPokePowers();
		}
	}
	public AttacksData getAttacks() {
		return attacks;
	}
	public void setAttacks(AttacksData attacks) {
		this.attacks = attacks;
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
	public String getSeedString() {
		return seed;
	}
}
