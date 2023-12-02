package ptcgr.constants;

import gbc_framework.utils.ByteUtils;

public class DuelConstants 
{
	public enum EffectFunctionTypes
	{
		/// Executed right after attack or trainer card is used. Bypasses Smokescreen and Sand Attack effects.
		INITIAL_EFFECT_1      	(0x01),
		/// Executed right after attack, Power, or trainer card is used.
		INITIAL_EFFECT_2       	(0x02),
		/// Effect command of an attack executed prior to the damage step. For trainer card or Power, usually the main effect.
		BEFORE_DAMAGE           (0x03),
		/// Effect command executed after the damage step.
		AFTER_DAMAGE            (0x04),
		/// For attacks, Powers, or trainer cards requiring the user to select a card (from e.g. play area screen or card list).
		REQUIRE_SELECTION       (0x05),
		/// For attacks or trainer cards that require putting one or more attached energy cards into the discard pile.
		DISCARD_ENERGY          (0x06),
		/// Power effects that trigger the moment the card is played.
		POWER_TRIGGER       	(0x07),
		/// When AI is required to select a card
		AI_SELECTION            (0x08),
		/// Used for AI scoring
		AI_SCORING              (0x09),
		/// For attacks that may result in the defender being switched out. Called only for AI-executed attacks.
		AI_SWITCH_DEFENDER      (0x0a);
		
		private byte value;
		private EffectFunctionTypes(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "EffectFunctionType enum: " + inValue);
			}
			value = (byte) inValue;
		}
	
		public byte getValue()
		{
			return value;
		}
		
	    public static EffectFunctionTypes readFromByte(byte b)
	    {
	    	for (EffectFunctionTypes num : EffectFunctionTypes.values())
	    	{
	    		if (b == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid EffectFunctionTypes value " + b + " was passed");
	    }
	}
}
