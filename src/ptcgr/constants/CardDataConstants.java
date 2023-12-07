package ptcgr.constants;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import gbc_framework.utils.ByteUtils;

public class CardDataConstants 
{	
	public enum EnergyType
	{
		FIRE		(0x00, "Fir", "R"), // "R" for "Red" or "fiRe"
		GRASS		(0x01, "Grs", "G"),
		LIGHTNING	(0x02, "Ltn", "L"),
		WATER		(0x03, "Wtr", "W"),
		FIGHTING	(0x04, "Fgt", "B"), // "B" for "Brown"
		PSYCHIC	    (0x05, "Psy", "P"),
		COLORLESS   (0x06, "Col", "C"),
		UNUSED_TYPE (0x07, "Err", "E");

		private byte value;
		private String abbrev;
		private String charAbbrev;
		private EnergyType(int inValue, String inAbbrev, String inCharAbbrev)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "EnergyTypes enum: " + inValue);
			}
			value = (byte) inValue;
			abbrev = inAbbrev;
			charAbbrev = inCharAbbrev;
		}

		public byte getValue()
		{
			return value;
		}

		public String getAbbreviation()
		{
			return abbrev;
		}
		
		public String getCharAbbreviation()
		{
			return charAbbrev;
		}
		
	    public static EnergyType readFromByte(byte b)
	    {
	    	for(EnergyType num : EnergyType.values())
	    	{
	    		if(b == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid EnergyType value " + b + " was passed");
	    }

		public CardType convertToCardType()
		{
			return CardType.readFromByte(getValue());
		}
		
		public WeaknessResistanceType convertToWeaknessResistanceType()
		{
			switch(this)
			{
			case FIRE:
				return WeaknessResistanceType.FIRE;
			case GRASS:
				return WeaknessResistanceType.GRASS;
			case LIGHTNING:
				return WeaknessResistanceType.LIGHTNING;
			case FIGHTING:
				return WeaknessResistanceType.FIGHTING;
			case PSYCHIC:
				return WeaknessResistanceType.PSYCHIC;
			case COLORLESS:
				// TODO later: May have a valid value?
				return WeaknessResistanceType.NONE; 
			case UNUSED_TYPE:
				return WeaknessResistanceType.NONE;
			default:
				return WeaknessResistanceType.NONE;
			}
		}
	}

	public enum CardType
	{
		MONSTER_FIRE            (EnergyType.FIRE.getValue()),
		MONSTER_GRASS           (EnergyType.GRASS.getValue()),
		MONSTER_LIGHTNING       (EnergyType.LIGHTNING.getValue()),
		MONSTER_WATER           (EnergyType.WATER.getValue()),
		MONSTER_FIGHTING        (EnergyType.FIGHTING.getValue()),
		MONSTER_PSYCHIC         (EnergyType.PSYCHIC.getValue()),
		MONSTER_COLORLESS       (EnergyType.COLORLESS.getValue()),
		MONSTER_UNUSED          (EnergyType.UNUSED_TYPE.getValue()),
		ENERGY_FIRE             (0x08),
		ENERGY_GRASS            (0x09),
		ENERGY_LIGHTNING        (0x0a),
		ENERGY_WATER            (0x0b),
		ENERGY_FIGHTING         (0x0c),
		ENERGY_PSYCHIC          (0x0d),
		ENERGY_DOUBLE_COLORLESS (0x0e),
		ENERGY_UNUSED           (0x0f),
		TRAINER                 (0x10),
		TRAINER_UNUSED          (0x11);
		
		private static List<CardType> mons = new ArrayList<>();
		private static List<CardType> energies = new ArrayList<>();
		private static List<CardType> trainers = new ArrayList<>();

		private byte value;
		private CardType(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE|| inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "CardType enum: " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
		
	    public static CardType readFromByte(byte b)
	    {
	    	for(CardType num : CardType.values())
	    	{
	    		if(b == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid CardType value " + b + " was passed");
	    }
	    
	    public static final List<CardType> monsterValues()
	    {
	    	if (mons.isEmpty())
	    	{
	    		mons.add(MONSTER_FIRE);
	    		mons.add(MONSTER_GRASS);
	    		mons.add(MONSTER_LIGHTNING);
		    	mons.add(MONSTER_WATER);
		    	mons.add(MONSTER_FIGHTING);
		    	mons.add(MONSTER_PSYCHIC);
		    	mons.add(MONSTER_COLORLESS);
	    	}
			return mons;
	    }
	    
	    public static final List<CardType> monsterValuesWithUnused()
	    {
	    	if (mons.isEmpty())
	    	{
	    		mons.add(MONSTER_FIRE);
	    		mons.add(MONSTER_GRASS);
	    		mons.add(MONSTER_LIGHTNING);
		    	mons.add(MONSTER_WATER);
		    	mons.add(MONSTER_FIGHTING);
		    	mons.add(MONSTER_PSYCHIC);
		    	mons.add(MONSTER_COLORLESS);
		    	mons.add(MONSTER_UNUSED);
	    	}
			return mons;
	    }
	    
	    public static final List<CardType> energyValues()
	    {
	    	if (energies.isEmpty())
	    	{
		    	energies.add(ENERGY_FIRE);
		    	energies.add(ENERGY_GRASS);
		    	energies.add(ENERGY_LIGHTNING);
		    	energies.add(ENERGY_WATER);
		    	energies.add(ENERGY_FIGHTING);
		    	energies.add(ENERGY_PSYCHIC);
		    	energies.add(ENERGY_DOUBLE_COLORLESS);
		    	energies.add(ENERGY_UNUSED);
	    	}
			return energies;
	    }

	    public static final List<CardType> trainerValues()
	    {
	    	if (trainers.isEmpty())
	    	{
	    		trainers.add(TRAINER);
	    		trainers.add(TRAINER_UNUSED);
	    	}
			return trainers;
	    }
	    
		public boolean isMonsterCard() 
		{
			return monsterValues().contains(this);
		}

		public boolean isEnergyCard() 
		{
			return energyValues().contains(this);
		}

		public boolean isTrainerCard() 
		{
			return trainerValues().contains(this);
		}
		
		public EnergyType convertToEnergyType()
		{
			if (!isMonsterCard())
			{
				return EnergyType.UNUSED_TYPE;
			}
			return EnergyType.readFromByte(getValue());
		}
	}

	public enum CardRarity
	{
		CIRCLE    (0x0,  "C"),
		DIAMOND   (0x1,  "D"),
		STAR      (0x2,  "S"),
		PROMOSTAR (0xff, "P");

		private byte value;
		private String abbrev;
		
		private CardRarity(int inValue, String abbrev)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "CardRarity enum: " + inValue);
			}
			value = (byte) inValue;
			this.abbrev = abbrev;
		}

		public byte getValue()
		{
			return value;
		}

		public String getAbbreviation()
		{
			return abbrev;
		}
		
	    public static CardRarity readFromByte(byte b)
	    {
	    	for(CardRarity num : CardRarity.values())
	    	{
	    		if(b == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid CardType value " + b + " was passed");
	    }
	}
	
	public enum BoosterPack
	{
		PACK_C      (0x0, "Colo"),
		PACK_E      (0x1, "Evol"),
		PACK_M      (0x2, "Myst"),
		PACK_L      (0x3, "Lab"),
		PACK_PROMO  (0x4, "Prmo"),
		PACK_ENERGY (0x5, "Engy");
		
		private byte value;
		private String abbrev;
		
		private BoosterPack(int inValue, String abbrev)
		{
			// stored in upper half of byte with set in the lower half but we treat it as the lower 
			// half to make things make more sense in this code
			if (inValue > ByteUtils.MAX_HEX_CHAR_VALUE || inValue < ByteUtils.MIN_HEX_CHAR_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "BoosterPack enum " + inValue + " (" + (inValue << 4) + " )");
			}
			value = (byte) inValue;
			this.abbrev = abbrev;
		}

		public byte getValue()
		{
			return value;
		}

		public String getAbbreviation()
		{
			return abbrev;
		}
		
	    public static BoosterPack readFromHexChar(byte hexChar)
	    {
	    	for(BoosterPack num : BoosterPack.values())
	    	{
	    		if(hexChar == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid BoosterPack value " + hexChar + " was passed");
	    }
	}

	public enum CardSet
	{
		SET_N     (0x0, "None"),
		SET_J     (0x1, "Jngl"),
		SET_F     (0x2, "Fosl"),
		SET_G     (0x7, "GB"),
		SET_PROMO (0x8, "Prmo");

		private byte value;
		private String abbrev;
		
		private CardSet(int inValue, String abbrev)
		{
			// stored in lower half of byte with pack in the upper half
			if (inValue > ByteUtils.MAX_HEX_CHAR_VALUE  || inValue < ByteUtils.MIN_HEX_CHAR_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "CardSet enum: " + inValue);
			}
			value = (byte) inValue;
			this.abbrev = abbrev;
		}

		public byte getValue()
		{
			return value;
		}

		public String getAbbreviation()
		{
			return abbrev;
		}
		
	    public static CardSet readFromHexChar(byte hexChar)
	    {
	    	for(CardSet num : CardSet.values())
	    	{
	    		if(hexChar == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid CardSet value " + hexChar + " was passed");
	    }
	}

	public enum EvolutionStage
	{
		BASIC   (0x00, "B"),
		STAGE_1 (0x01, "1"),
		STAGE_2 (0x02, "2");

		private byte value;
		private String abbrev;
		
		private EvolutionStage(int inValue, String abbrev)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "EvolutionStage enum: " + inValue);
			}
			value = (byte) inValue;
			this.abbrev = abbrev;
		}

		public byte getValue()
		{
			return value;
		}

		public String getAbbreviation()
		{
			return abbrev;
		}
		
	    public static EvolutionStage readFromByte(byte b)
	    {
	    	for(EvolutionStage num : EvolutionStage.values())
	    	{
	    		if(b == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid EvolutionStage value " + b + " was passed");
	    }
	}

	public enum WeaknessResistanceType
	{
		// Note this is a flag. We can we have multiple weaknesses
		// TODO later: Is there a limit to the max number we can have?
		FIRE      (0x80),
		GRASS     (0x40),
		LIGHTNING (0x20),
		WATER     (0x10),
		FIGHTING  (0x08),
		PSYCHIC   (0x04),
		// TODO later: Colorless 0x02?
		NONE      (0x00);

		private byte value;
		private WeaknessResistanceType(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "WeaknessResistanceType enum: " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
		
	    public static WeaknessResistanceType readFromByte(byte b)
	    {
	    	for(WeaknessResistanceType num : WeaknessResistanceType.values())
	    	{
	    		if(b == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid WeaknessResistanceType value " + b + " was passed");
	    }
	    
		public EnergyType convertToEnergyType()
		{
			switch(this)
			{
			case FIRE:
				return EnergyType.FIRE;
			case GRASS:
				return EnergyType.GRASS;
			case LIGHTNING:
				return EnergyType.LIGHTNING;
			case FIGHTING:
				return EnergyType.FIGHTING;
			case PSYCHIC:
				return EnergyType.PSYCHIC;
			case NONE:
				return EnergyType.UNUSED_TYPE;
			default:
				return EnergyType.UNUSED_TYPE;
			}
		}
	}

	public enum MoveCategory
	{
		DAMAGE_NORMAL (0x00),
		DAMAGE_PLUS   (0x01),
		DAMAGE_MINUS  (0x02),
		DAMAGE_X      (0x03),
		POWER         (0x04),
		RESIDUAL      (1 << 7);

		private byte value;
		private MoveCategory(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "MoveCategory enum: " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
		
	    public static MoveCategory readFromByte(byte b)
	    {
	    	for(MoveCategory num : MoveCategory.values())
	    	{
	    		if(b == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid MoveCategory value " + b + " was passed");
	    }
	}

	public enum MoveEffect1
	{
		POISON           (1 << 0),
		SLEEP            (1 << 1),
		PARALYSIS        (1 << 2),
		CONFUSION                (1 << 3),
		RECOIL_SMALL               (1 << 4),
		DAMAGE_TO_OPPONENT_BENCH (1 << 5),
		RECOIL_LARGE              (1 << 6),
		DRAW_CARD                (1 << 7);
		
		private byte value;
		private MoveEffect1(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "MoveEffect1 enum: " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
		
	    public static Set<MoveEffect1> readFromByte(byte b)
	    {
	    	EnumSet<MoveEffect1> readInEffects = EnumSet.noneOf(MoveEffect1.class);
	    	for(MoveEffect1 num : MoveEffect1.values())
	    	{
	    		if ((num.getValue() & b) != 0)
	    		{
	    	    	readInEffects.add(num);
	    		}
	    	}
	    	return readInEffects;
	    }
	    
	    public static byte storeAsByte(Set<MoveEffect1> set)
	    {
	    	byte combinedValue = 0;
	    	for(MoveEffect1 num : MoveEffect1.values())
	    	{
	    		if(set.contains(num))
	    		{
	    			combinedValue += num.getValue();
	    		}
	    	}
	    	return combinedValue;
	    }
	}

	public enum MoveEffect2
	{
		// TODO later: bits 5, 6 and 7 cover a wide variety of effects. See if we can figure something out for them
		SWITCH_OPPONENT_MONSTER  (1 << 0),
		HEAL_SELF                (1 << 1),
		NULLIFY_OR_WEAKEN_ATTACK (1 << 2),
		DISCARD_ENERGY           (1 << 3),
		ATTACHED_ENERGY_BOOST    (1 << 4),
		FLAG_2_BIT_5             (1 << 5),
		FLAG_2_BIT_6             (1 << 6),
		FLAG_2_BIT_7             (1 << 7);

		private byte value;
		private MoveEffect2(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "MoveEffect2 enum: " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}

	    public static Set<MoveEffect2> readFromByte(byte b)
	    {
	    	EnumSet<MoveEffect2> readInEffects = EnumSet.noneOf(MoveEffect2.class);
	    	for(MoveEffect2 num : MoveEffect2.values())
	    	{
	    		if ((num.getValue() & b) != 0)
	    		{
	    	    	readInEffects.add(num);
	    		}
	    	}
	    	return readInEffects;
	    }
	    
	    public static byte storeAsByte(Set<MoveEffect2> set)
	    {
	    	byte combinedValue = 0;
	    	for(MoveEffect2 num : MoveEffect2.values())
	    	{
	    		if(set.contains(num))
	    		{
	    			combinedValue += num.getValue();
	    		}
	    	}
	    	return combinedValue;
	    }
	}

	public enum MoveEffect3
	{
		// TODO later: bit 1 covers a wide variety of effects. See if we can figure it out
		// bits 2-7 are unused
		BOOST_IF_TAKEN_DAMAGE    (1 << 0),
		FLAG_3_BIT_1             (1 << 1);

		private byte value;
		private MoveEffect3(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "MoveEffect3 enum: " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
		}
		
	    public static Set<MoveEffect3> readFromByte(byte b)
	    {
	    	EnumSet<MoveEffect3> readInEffects = EnumSet.noneOf(MoveEffect3.class);
	    	for(MoveEffect3 num : MoveEffect3.values())
	    	{
	    		if ((num.getValue() & b) != 0)
	    		{
	    	    	readInEffects.add(num);
	    		}
	    	}
	    	return readInEffects;
	    }
	    
	    public static byte storeAsByte(Set<MoveEffect3> set)
	    {
	    	byte combinedValue = 0;
	    	for(MoveEffect3 num : MoveEffect3.values())
	    	{
	    		if(set.contains(num))
	    		{
	    			combinedValue += num.getValue();
	    		}
	    	}
	    	return combinedValue;
	    }
	}

	static final byte RETREAT_COST_UNABLE_TO_RETREAT = 0x64;
}
