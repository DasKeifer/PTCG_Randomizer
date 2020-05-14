package constants;

public class CardDataConstants 
{
	enum EnergyTypes
	{
		FIRE		(0x00),
		GRASS		(0x01),
		LIGHTNING	(0x02),
		WATER		(0x03),
		FIGHTING	(0x04),
		PSYCHIC	    (0x05),
		COLORLESS   (0x06),
		UNUSED_TYPE (0x07);

		private int value;
		EnergyTypes(int inValue)
		{
			value = inValue;
		}

		int getValue()
		{
			return value;
		}
	}

	enum CardType
	{
		POKEMON_FIRE            (EnergyTypes.FIRE.getValue()),
		POKEMON_GRASS           (EnergyTypes.GRASS.getValue()),
		POKEMON_LIGHTNING       (EnergyTypes.LIGHTNING.getValue()),
		POKEMON_WATER           (EnergyTypes.WATER.getValue()),
		POKEMON_FIGHTING        (EnergyTypes.FIGHTING.getValue()),
		POKEMON_PSYCHIC         (EnergyTypes.PSYCHIC.getValue()),
		POKEMON_COLORLESS       (EnergyTypes.COLORLESS.getValue()),
		POKEMON_UNUSED          (EnergyTypes.UNUSED_TYPE.getValue()),
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

		//TODO TYPE_PKMN      EQU %111
		//TYPE_ENERGY_F  EQU 3
		//TYPE_TRAINER_F EQU 4

		private int value;
		CardType(int inValue)
		{
			value = inValue;
		}

		int getValue()
		{
			return value;
		}
	}

	enum CardRarity
	{
		CIRCLE    (0x0),
		DIAMOND   (0x1),
		STAR      (0x2),
		PROMOSTAR (0xff);

		private int value;
		CardRarity(int inValue)
		{
			value = inValue;
		}

		int getValue()
		{
			return value;
		}
	}

	enum BoosterPacks // Upper bits?
	{
		COLOSSEUM   (0x0 << 4),
		EVOLUTION   (0x1 << 4),
		MYSTERY     (0x2 << 4),
		LABORATORY  (0x3 << 4),
		PROMOTIONAL (0x4 << 4),
		ENERGY      (0x5 << 4);

		private int value;
		BoosterPacks(int inValue)
		{
			value = inValue;
		}

		int getValue()
		{
			return value;
		}
	}

	enum CardSet // Lower Bits?
	{
		JUNGLE (0x1),
		FOSSIL (0x2),
		GB     (0x7),
		PRO    (0x8);

		private int value;
		CardSet(int inValue)
		{
			value = inValue;
		}

		int getValue()
		{
			return value;
		}
	}

	enum EvolutionStage
	{
		BASIC  (0x00),
		STAGE1 (0x01),
		STAGE2 (0x02);

		private int value;
		EvolutionStage(int inValue)
		{
			value = inValue;
		}

		int getValue()
		{
			return value;
		}
	}

	enum WeaknessAndResistance
	{
		FIRE      (0x80),
		GRASS     (0x40),
		LIGHTNING (0x20),
		WATER     (0x10),
		FIGHTING  (0x08),
		PSYCHIC   (0x04);

		private int value;
		WeaknessAndResistance(int inValue)
		{
			value = inValue;
		}

		int getValue()
		{
			return value;
		}
	}

	enum MoveCategory
	{
		DAMAGE_NORMAL (0x00),
		DAMAGE_PLUS   (0x01),
		DAMAGE_MINUS  (0x02),
		DAMAGE_X      (0x03),
		POKEMON_POWER (0x04);
		//TODO:RESIDUAL_F    EQU 7
		//RESIDUAL      EQU 1 << RESIDUAL_F

		private int value;
		MoveCategory(int inValue)
		{
			value = inValue;
		}

		int getValue()
		{
			return value;
		}
	}

	enum MoveEffect1
	{
		INFLICT_POISON           (1 << 0),
		INFLICT_SLEEP            (1 << 1),
		INFLICT_PARALYSIS        (1 << 2),
		INFLICT_CONFUSION        (1 << 3),
		LOW_RECOIL               (1 << 4),
		DAMAGE_TO_OPPONENT_BENCH (1 << 5),
		HIGH_RECOIL              (1 << 6),
		DRAW_CARD                (1 << 7);

		private int value;
		MoveEffect1(int inValue)
		{
			value = inValue;
		}

		int getValue()
		{
			return value;
		}
	}

	enum MoveEffect2
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

		private int value;
		MoveEffect2(int inValue)
		{
			value = inValue;
		}

		int getValue()
		{
			return value;
		}
	}

	enum MoveEffect3
	{
		// TODO: bit 1 covers a wide variety of effects
		// bits 2-7 are unused
		BOOST_IF_TAKEN_DAMAGE    (1 << 0),
		FLAG_3_BIT_1             (1 << 1);

		private int value;
		MoveEffect3(int inValue)
		{
			value = inValue;
		}

		int getValue()
		{
			return value;
		}
	}

	static final int RETREAT_COST_UNABLE_RETREAT = 0x64;
}
