package constants;

public class DeckConstants 
{
	public enum DeckValues
	{
		// Both *_DECK and *_DECK_ID constants are defined here.
		// *_DECK constants are to be used with LoadDeck and related routines.
		// *_DECK_ID constants are specific to be loaded into wOpponentDeckID.
		// Always, *_DECK_ID = *_DECK - 2. UNNAMED_DECK_ID and UNNAMED_2_DECK_ID do not exist.
		UNNAMED_DECK                (0x00),
		UNNAMED_2_DECK              (0x01),
		SAMS_PRACTICE_DECK          (0x02),
		PRACTICE_PLAYER_DECK        (0x03),
		SAMS_NORMAL_DECK            (0x04),
		CHARMANDER_AND_FRIENDS_DECK (0x05),
		CHARMANDER_EXTRA_DECK       (0x06),
		SQUIRTLE_AND_FRIENDS_DECK   (0x07),
		SQUIRTLE_EXTRA_DECK         (0x08),
		BULBASAUR_AND_FRIENDS_DECK  (0x09),
		BULBASAUR_EXTRA_DECK        (0x0A),
		LIGHTNING_AND_FIRE_DECK     (0x0B),
		WATER_AND_FIGHTING_DECK     (0x0C),
		GRASS_AND_PSYCHIC_DECK      (0x0D),
		LEGENDARY_MOLTRES_DECK      (0x0E),
		LEGENDARY_ZAPDOS_DECK       (0x0F),
		LEGENDARY_ARTICUNO_DECK     (0x10),
		LEGENDARY_DRAGONITE_DECK    (0x11),
		FIRST_STRIKE_DECK           (0x12),
		ROCK_CRUSHER_DECK           (0x13),
		GO_GO_RAIN_DANCE_DECK       (0x14),
		ZAPPING_SELFDESTRUCT_DECK   (0x15),
		FLOWER_POWER_DECK           (0x16),
		STRANGE_PSYSHOCK_DECK       (0x17),
		WONDERS_OF_SCIENCE_DECK     (0x18),
		FIRE_CHARGE_DECK            (0x19),
		IM_RONALD_DECK              (0x1A),
		POWERFUL_RONALD_DECK        (0x1B),
		INVINCIBLE_RONALD_DECK      (0x1C),
		LEGENDARY_RONALD_DECK       (0x1D),
		MUSCLES_FOR_BRAINS_DECK     (0x1E),
		HEATED_BATTLE_DECK          (0x1F),
		LOVE_TO_BATTLE_DECK         (0x20),
		EXCAVATION_DECK             (0x21),
		BLISTERING_POKEMON_DECK     (0x22),
		HARD_POKEMON_DECK           (0x23),
		WATERFRONT_POKEMON_DECK     (0x24),
		LONELY_FRIENDS_DECK         (0x25),
		SOUND_OF_THE_WAVES_DECK     (0x26),
		PIKACHU_DECK                (0x27),
		BOOM_BOOM_SELFDESTRUCT_DECK (0x28),
		POWER_GENERATOR_DECK        (0x29),
		ETCETERA_DECK               (0x2A),
		FLOWER_GARDEN_DECK          (0x2B),
		KALEIDOSCOPE_DECK           (0x2C),
		GHOST_DECK                  (0x2D),
		NAP_TIME_DECK               (0x2E),
		STRANGE_POWER_DECK          (0x2F),
		FLYIN_POKEMON_DECK          (0x30),
		LOVELY_NIDORAN_DECK         (0x31),
		POISON_DECK                 (0x32),
		ANGER_DECK                  (0x33),
		FLAMETHROWER_DECK           (0x34),
		RESHUFFLE_DECK              (0x35),
		IMAKUNI_DECK                (0x36);
		
		private byte value;
		DeckValues(int inValue)
		{
			if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "DeckValues enum: " + inValue);
			}
			value = (byte) inValue;
		}
		
		boolean isValidDeck()
		{
			return value >= 2;
		}
		
		byte getValue()
		{
			return value;
		}

		// Always, *_DECK_ID = *_DECK - 2. UNNAMED_DECK_ID and UNNAMED_2_DECK_ID do not exist.
		byte getId()
		{
			if (value < 2)
			{
				return (byte) (value - 2);
			}
			else
			{
				throw new IllegalArgumentException("Attempted to retrieve ID for invalid deck with value: " + value);
			}
		}

	    public static DeckValues readFromByte(byte b)
	    {
	    	for(DeckValues num : DeckValues.values())
	    	{
	    		if(b == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid DeckValues value " + b + " was passed");
	    }
	    
	    public static DeckValues readIdFromByte(byte b)
	    {
	    	for(DeckValues num : DeckValues.values())
	    	{
	    		if(b == num.getId())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid DeckValues id value " + b + " was passed");
	    }
	}
}
