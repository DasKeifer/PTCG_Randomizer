package datamanager;

import java.util.HashMap;
import java.util.Map;

import compiler.CodeSnippit;

public abstract class FlexibleBlock 
{
	protected CodeSnippit toAdd;
	protected Map<Byte, int[]> allowableAddressPreferences;
	
	protected FlexibleBlock(CodeSnippit toPlaceInBank)
	{
		toAdd = new CodeSnippit(toPlaceInBank);
		allowableAddressPreferences = new HashMap<>();
	}
	
	public void addAllowableAddressRange(byte priority, int globalAddressStart, int globalAddressEnd)
	{
		if (globalAddressStart > globalAddressEnd)
		{
			throw new UnsupportedOperationException("Start address is after the end address!");
		}
		
		allowableAddressPreferences.put(priority, new int[] {globalAddressStart, globalAddressEnd});
	}
}
