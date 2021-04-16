package constants;

import constants.CardDataConstants.CardRarity;
import util.ByteUtils;

public class DuelConstants 
{

	public enum EffectCommandTypes
	{
		// effect command constants (TryExecuteEffectCommandFunction)
		// ordered by (roughly) execution time
		InitialEffect1         	(0x01),
		InitialEffect2         	(0x02),
		DiscardEnergy           (0x06),
		RequireSelection        (0x05),
		BeforeDamage            (0x03),
		AfterDamage             (0x04),
		AiSelection             (0x08),
		AiSwitchDefendingPkmn	(0x0a),
		PkmnPowerTrigger      	(0x07),
		Ai                      (0x09);
		
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
