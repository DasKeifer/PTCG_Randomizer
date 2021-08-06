package constants;

import util.ByteUtils;

public class DuelConstants 
{
	public enum EffectCommandTypes
	{
		// effect command constants (TryExecuteEffectCommandFunction)
		// Note: the game assumes they are ordered in the effect command in increasing
		// numeric order. If an effect command does not obey this, things will not work
		// correctly.
		// Rough execution order and notes on effects:
		/*; Commands are associated to a time or a scope (EFFECTCMDTYPE_*) that determines when their function is executed during the turn.
		; - EFFECTCMDTYPE_INITIAL_EFFECT_1: Executed right after attack or trainer card is used. Bypasses Smokescreen and Sand Attack effects.
		; - EFFECTCMDTYPE_INITIAL_EFFECT_2: Executed right after attack, Pokemon Power, or trainer card is used.
		; - EFFECTCMDTYPE_DISCARD_ENERGY: For attacks or trainer cards that require putting one or more attached energy cards into the discard pile.
		; - EFFECTCMDTYPE_REQUIRE_SELECTION: For attacks, Pokemon Powers, or trainer cards requiring the user to select a card (from e.g. play area screen or card list).
		; - EFFECTCMDTYPE_BEFORE_DAMAGE: Effect command of an attack executed prior to the damage step. For trainer card or Pokemon Power, usually the main effect.
		; - EFFECTCMDTYPE_AFTER_DAMAGE: Effect command executed after the damage step.
		; - EFFECTCMDTYPE_AI_SWITCH_DEFENDING_PKMN: For attacks that may result in the defending Pokemon being switched out. Called only for AI-executed attacks.
		; - EFFECTCMDTYPE_PKMN_POWER_TRIGGER: Pokemon Power effects that trigger the moment the Pokemon card is played.
		; - EFFECTCMDTYPE_AI: Used for AI scoring.
		; - EFFECTCMDTYPE_AI_SELECTION: When AI is required to select a card*/
				
		InitialEffect1         	(0x01),
		InitialEffect2         	(0x02),
		BeforeDamage            (0x03),
		AfterDamage             (0x04),
		RequireSelection        (0x05),
		DiscardEnergy           (0x06),
		PkmnPowerTrigger      	(0x07),
		AiSelection             (0x08),
		Ai                      (0x09),
		AiSwitchDefendingPkmn	(0x0a);
		
		private byte value;
		private EffectCommandTypes(int inValue)
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
		
	    public static EffectCommandTypes readFromByte(byte b)
	    {
	    	for (EffectCommandTypes num : EffectCommandTypes.values())
	    	{
	    		if (b == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid EffectCommandTypes value " + b + " was passed");
	    }
	}
}
