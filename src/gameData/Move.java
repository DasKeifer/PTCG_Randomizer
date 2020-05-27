package gameData;

import java.util.Set;

import constants.RomConstants;
import constants.CardDataConstants.*;
import rom.Texts;
import util.ByteUtils;

public class Move
{
	public static final int TOTAL_SIZE_IN_BYTES = 19;
	
	byte[] energyCost;
	String name;
	public EffectDescription description = new EffectDescription();
	byte damage; // TODO: non multiple of 10?
	MoveCategory category;
	short effectPtr; // TODO: Make enum?
	Set<MoveEffect1> effect1;
	Set<MoveEffect2> effect2;
	Set<MoveEffect3> effect3;
	byte unknownByte;
	byte animation;

	public String toString()
	{
		String tempString = "Move Name: " + name + "\nRequires:";
		
		boolean foundEnergy = false;
		for (EnergyType energyType : EnergyType.values())
		{
			if (energyCost[energyType.getValue()] > 0)
			{
				tempString += "\n\t" + energyCost[energyType.getValue()] + " " + energyType;
				foundEnergy = true;
			}
		}
		if (!foundEnergy)
		{
			tempString += " No energies";
		}
				
		return tempString + "\nDamage: " + damage +
				"\nDescription: " + description + 
				"\nEffectPtr: " + effectPtr +
				"\nEffectFlags: " + effect1 + ", " + effect2 + ", " + effect3;
				
	}
	
	public int readNameAndDataAndConvertIds(byte[] moveBytes, int startIndex, String cardName, Texts ptrToText, Set<Short> ptrsUsed) 
	{
		int index = startIndex;
		
		// They are stored in octects corresponding to their energy type. Since we
		// read them as bytes, we mask each byte and increment the index every other time
		energyCost = new byte[8];
		setCost(EnergyType.FIRE, ByteUtils.readUpperHexChar(moveBytes[index]));
		setCost(EnergyType.GRASS, ByteUtils.readLowerHexChar(moveBytes[index]));
		index++;
		setCost(EnergyType.LIGHTNING, ByteUtils.readUpperHexChar(moveBytes[index]));
		setCost(EnergyType.WATER, ByteUtils.readLowerHexChar(moveBytes[index]));
		index++;
		setCost(EnergyType.FIGHTING, ByteUtils.readUpperHexChar(moveBytes[index]));
		setCost(EnergyType.PSYCHIC, ByteUtils.readLowerHexChar(moveBytes[index]));
		index++;
		setCost(EnergyType.COLORLESS, ByteUtils.readUpperHexChar(moveBytes[index]));
		setCost(EnergyType.UNUSED_TYPE, ByteUtils.readLowerHexChar(moveBytes[index]));
		index++;
		
		short namePtr = ByteUtils.readAsShort(moveBytes, index);
		name = ptrToText.getAtId(namePtr);
		ptrsUsed.add(namePtr);
		index += 2;		
		
		int[] descIndexes = {index, index + RomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.readTextFromIds(moveBytes, descIndexes, cardName, ptrToText, ptrsUsed);
		index += RomConstants.TEXT_ID_SIZE_IN_BYTES * 2;
		
		damage = moveBytes[index++];
		category = MoveCategory.readFromByte(moveBytes[index++]);
		effectPtr = ByteUtils.readAsShort(moveBytes, index);
		index += 2;
		effect1 = MoveEffect1.readFromByte(moveBytes[index++]);
		effect2 = MoveEffect2.readFromByte(moveBytes[index++]);
		effect3 = MoveEffect3.readFromByte(moveBytes[index++]);
		unknownByte = moveBytes[index++];
		animation = moveBytes[index++];
		
		return index;
	}

	public byte getCost(EnergyType inType)
	{
		return energyCost[inType.getValue()];
	}
	
	public void setCost(EnergyType inType, byte inCost)
	{
		if (inCost > ByteUtils.MAX_HEX_CHAR_VALUE || inCost < ByteUtils.MIN_BYTE_VALUE)
		{
			throw new IllegalArgumentException("Invalid value was passed for energy type " + inType + " cost: " + inCost);
		}
		energyCost[inType.getValue()] = inCost;
	}

	public int convertToIdsAndWriteData(byte[] moveBytes, int startIndex, String cardName, Texts ptrToText) 
	{
		int index = startIndex;
		
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.FIRE), getCost(EnergyType.GRASS));
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.LIGHTNING), getCost(EnergyType.WATER));
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.FIGHTING), getCost(EnergyType.PSYCHIC));
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.COLORLESS), getCost(EnergyType.UNUSED_TYPE));

		if (name == null || name.isEmpty())
		{
			ByteUtils.writeAsShort((short)0, moveBytes, index);
		}
		else
		{
			ByteUtils.writeAsShort(ptrToText.insertTextOrGetId(name), moveBytes, index);
		}
		index += 2;

		int[] descIndexes = {index, index + 2};
		description.convertToIdsAndWriteText(moveBytes, descIndexes, cardName, ptrToText);
		index += 4;
		
		moveBytes[index++] = damage;
		moveBytes[index++] = category.getValue();
		ByteUtils.writeAsShort(effectPtr, moveBytes, index);
		index += 2;
		moveBytes[index++] = MoveEffect1.storeAsByte(effect1);
		moveBytes[index++] = MoveEffect2.storeAsByte(effect2);
		moveBytes[index++] = MoveEffect3.storeAsByte(effect3);
		moveBytes[index++] = unknownByte;
		moveBytes[index++] = animation;
		
		return index;
	}
}
