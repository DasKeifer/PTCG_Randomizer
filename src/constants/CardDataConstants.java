package constants;

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
		EnergyType(int inValue)
		{
			if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "EnergyTypes enum: " + inValue);
			}
			value = (byte) inValue;
		}

		byte getValue()
		{
			return value;
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

		private byte value;
		CardType(int inValue)
		{
			if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "CardType enum: " + inValue);
			}
			value = (byte) inValue;
		}

		byte getValue()
		{
			return value;
		}
	}

	public enum CardRarity
	{
		CIRCLE    (0x0),
		DIAMOND   (0x1),
		STAR      (0x2),
		PROMOSTAR (0xff);

		private byte value;
		CardRarity(int inValue)
		{
			if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "CardRarity enum: " + inValue);
			}
			value = (byte) inValue;
		}

		byte getValue()
		{
			return value;
		}
	}

	public enum CardSetAndPack
	{
		// bits 0-3 are the set, bits 4-7 are the booster pack they can be found in
		SET_JUNGLE (0x1),
		SET_FOSSIL (0x2),
		SET_GB     (0x7),
		SET_PRO    (0x8),
		PACK_COLOSSEUM   (0x0 << 4),
		PACK_EVOLUTION   (0x1 << 4),
		PACK_MYSTERY     (0x2 << 4),
		PACK_LABORATORY  (0x3 << 4),
		PACK_PROMOTIONAL (0x4 << 4),
		PACK_ENERGY      (0x5 << 4);

		private byte value;
		CardSetAndPack(int inValue)
		{
			if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "CardSetAndPack enum: " + inValue);
			}
			value = (byte) inValue;
		}

		byte getValue()
		{
			return value;
		}
	}

	public enum EvolutionStage
	{
		BASIC  (0x00),
		STAGE1 (0x01),
		STAGE2 (0x02);

		private byte value;
		EvolutionStage(int inValue)
		{
			if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "EvolutionStage enum: " + inValue);
			}
			value = (byte) inValue;
		}

		byte getValue()
		{
			return value;
		}
	}

	public enum WeaknessAndResistance
	{
		// TODO: This looks like a flag... Can we have multiple weaknesses?
		FIRE      (0x80),
		GRASS     (0x40),
		LIGHTNING (0x20),
		WATER     (0x10),
		FIGHTING  (0x08),
		PSYCHIC   (0x04);
		// TODO: Colorless 0x02?

		private byte value;
		WeaknessAndResistance(int inValue)
		{
			if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "WeaknessAndResistance enum: " + inValue);
			}
			value = (byte) inValue;
		}

		byte getValue()
		{
			return value;
		}
	}

	public enum MoveCategory
	{
		DAMAGE_NORMAL (0x00),
		DAMAGE_PLUS   (0x01),
		DAMAGE_MINUS  (0x02),
		DAMAGE_X      (0x03),
		POKEMON_POWER (0x04);
		//TODO:RESIDUAL_F    EQU 7
		//RESIDUAL      EQU 1 << RESIDUAL_F

		private byte value;
		MoveCategory(int inValue)
		{
			if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "MoveCategory enum: " + inValue);
			}
			value = (byte) inValue;
		}

		byte getValue()
		{
			return value;
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
		MoveEffect1(int inValue)
		{
			if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "MoveEffect1 enum: " + inValue);
			}
			value = (byte) inValue;
		}

		byte getValue()
		{
			return value;
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
		MoveEffect2(int inValue)
		{
			if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "MoveEffect2 enum: " + inValue);
			}
			value = (byte) inValue;
		}

		byte getValue()
		{
			return value;
		}
	}

	public enum MoveEffect3
	{
		// TODO: bit 1 covers a wide variety of effects
		// bits 2-7 are unused
		BOOST_IF_TAKEN_DAMAGE    (1 << 0),
		FLAG_3_BIT_1             (1 << 1);

		private byte value;
		MoveEffect3(int inValue)
		{
			if (inValue > Byte.MAX_VALUE || inValue < Byte.MIN_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "MoveEffect3 enum: " + inValue);
			}
			value = (byte) inValue;
		}

		byte getValue()
		{
			return value;
		}
	}

	static final byte RETREAT_COST_UNABLE_RETREAT = 0x64;
}
