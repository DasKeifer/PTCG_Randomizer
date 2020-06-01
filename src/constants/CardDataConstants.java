package constants;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import util.ByteUtils;

public class CardDataConstants 
{	
	public enum EnergyType
	{
		FIRE		(0x00),
		GRASS		(0x01),
		LIGHTNING	(0x02),
		WATER		(0x03),
		FIGHTING	(0x04),
		PSYCHIC	    (0x05),
		COLORLESS   (0x06),
		UNUSED_TYPE (0x07);

		private byte value;
		private EnergyType(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "EnergyTypes enum: " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
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
	}

	public enum CardType
	{
		POKEMON_FIRE            (EnergyType.FIRE.getValue()),
		POKEMON_GRASS           (EnergyType.GRASS.getValue()),
		POKEMON_LIGHTNING       (EnergyType.LIGHTNING.getValue()),
		POKEMON_WATER           (EnergyType.WATER.getValue()),
		POKEMON_FIGHTING        (EnergyType.FIGHTING.getValue()),
		POKEMON_PSYCHIC         (EnergyType.PSYCHIC.getValue()),
		POKEMON_COLORLESS       (EnergyType.COLORLESS.getValue()),
		POKEMON_UNUSED          (EnergyType.UNUSED_TYPE.getValue()),
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

		//Suspect these are unneeded but can be implemented as functions/constants
		//TYPE_PKMN      EQU %111
		//TYPE_ENERGY_F  EQU 3
		//TYPE_TRAINER_F EQU 4
		
		private static List<CardType> pokes = new ArrayList<>();
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
	    
	    public static final List<CardType> pokemonValues()
	    {
	    	if (pokes.isEmpty())
	    	{
	    		pokes.add(POKEMON_FIRE);
	    		pokes.add(POKEMON_GRASS);
	    		pokes.add(POKEMON_LIGHTNING);
		    	pokes.add(POKEMON_WATER);
		    	pokes.add(POKEMON_FIGHTING);
		    	pokes.add(POKEMON_PSYCHIC);
		    	pokes.add(POKEMON_COLORLESS);
		    	pokes.add(POKEMON_UNUSED);
	    	}
			return pokes;
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
	    
		public boolean isPokemonCard() 
		{
			return pokemonValues().contains(this);
		}

		public boolean isEnergyCard() 
		{
			return energyValues().contains(this);
		}

		public boolean isTrainerCard() 
		{
			return trainerValues().contains(this);
		}
	}

	public enum CardRarity
	{
		CIRCLE    (0x0),
		DIAMOND   (0x1),
		STAR      (0x2),
		PROMOSTAR (0xff);

		private byte value;
		private CardRarity(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "CardRarity enum: " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
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
		COLOSSEUM   (0x0),
		EVOLUTION   (0x1),
		MYSTERY     (0x2),
		LABORATORY  (0x3),
		PROMOTIONAL (0x4),
		ENERGY      (0x5);
		
		private byte value;
		
		private BoosterPack(int inValue)
		{
			// stored in upper half of byte with set in the lower half but we treat it as the lower 
			// half to make things make more sense in this code
			if (inValue > ByteUtils.MAX_HEX_CHAR_VALUE || inValue < ByteUtils.MIN_HEX_CHAR_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "BoosterPack enum " + inValue + " (" + (inValue << 4) + " )");
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
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
		NONE   (0x0),
		JUNGLE (0x1),
		FOSSIL (0x2),
		GB     (0x7),
		PRO    (0x8);

		private byte value;
		
		private CardSet(int inValue)
		{
			// stored in lower half of byte with pack in the upper half
			if (inValue > ByteUtils.MAX_HEX_CHAR_VALUE  || inValue < ByteUtils.MIN_HEX_CHAR_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "CardSet enum: " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
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
		BASIC  (0x00),
		STAGE1 (0x01),
		STAGE2 (0x02);

		private byte value;
		private EvolutionStage(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "EvolutionStage enum: " + inValue);
			}
			value = (byte) inValue;
		}

		public byte getValue()
		{
			return value;
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
		// TODO: This looks like a flag... Can we have multiple weaknesses?
		FIRE      (0x80),
		GRASS     (0x40),
		LIGHTNING (0x20),
		WATER     (0x10),
		FIGHTING  (0x08),
		PSYCHIC   (0x04),
		// TODO: Colorless 0x02?
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
	}

	public enum MoveCategory
	{
		DAMAGE_NORMAL (0x00),
		DAMAGE_PLUS   (0x01),
		DAMAGE_MINUS  (0x02),
		DAMAGE_X      (0x03),
		POKEMON_POWER (0x04),
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
		INFLICT_POISON           (1 << 0),
		INFLICT_SLEEP            (1 << 1),
		INFLICT_PARALYSIS        (1 << 2),
		INFLICT_CONFUSION        (1 << 3),
		LOW_RECOIL               (1 << 4),
		DAMAGE_TO_OPPONENT_BENCH (1 << 5),
		HIGH_RECOIL              (1 << 6),
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
		// TODO: bits 5, 6 and 7 cover a wide variety of effects
		SWITCH_OPPONENT_POKEMON  (1 << 0),
		HEAL_USER                (1 << 1),
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
		// TODO: bit 1 covers a wide variety of effects
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

	static final byte RETREAT_COST_UNABLE_RETREAT = 0x64;
}
