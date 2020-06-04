package data;

import java.util.HashSet;
import java.util.Set;

import constants.RomConstants;
import constants.CardDataConstants.*;
import rom.Texts;
import util.ByteUtils;

public class Move
{
	public static final int TOTAL_SIZE_IN_BYTES = 19;
	public static final Move EMPTY_MOVE = new Move();
	
	byte[] energyCost;
	OneLineText name;
	public EffectDescription description;
	public byte damage; // TODO: non multiple of 10?
	MoveCategory category;
	short effectPtr; // TODO: Make enum?
	Set<MoveEffect1> effect1;
	Set<MoveEffect2> effect2;
	Set<MoveEffect3> effect3;
	byte unknownByte;
	byte animation;

	public Move()
	{
		energyCost = new byte[8];
		name = new OneLineText();
		description = new EffectDescription();
		category = MoveCategory.DAMAGE_NORMAL;
		effect1 = new HashSet<>();
		effect2 = new HashSet<>();
		effect3 = new HashSet<>();
	}
	
	public Move(Move toCopy) 
	{
		energyCost = toCopy.energyCost;
		name = new OneLineText(toCopy.name);
		description = new EffectDescription(toCopy.description);
		damage = toCopy.damage;
		category = toCopy.category;
		effectPtr = toCopy.effectPtr;
		effect1 = new HashSet<>(toCopy.effect1);
		effect2 = new HashSet<>(toCopy.effect2);
		effect3 = new HashSet<>(toCopy.effect3);
		unknownByte = toCopy.unknownByte;
		animation = toCopy.animation;
	}

	public boolean isEmpty()
	{
		return name.isEmpty();
	}
	
	public boolean isPokePower()
	{
		for(byte cost : energyCost)
		{
			if (cost > 0)
			{
				return false;
			}
		}
		return true;
	}
	
	public String toString()
	{
		String tempString = "Move Name: " + name.toString() + "\nRequires:";
		
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
				"\nDescription: " + description.toString() + 
				"\nEffectPtr: " + effectPtr +
				"\nEffectFlags: " + effect1 + ", " + effect2 + ", " + effect3;
				
	}

	public byte getCost(EnergyType inType)
	{
		return energyCost[inType.getValue()];
	}
	
	public int getNonColorlessEnergyCosts()
	{
		int energyCount = 0;
		for (EnergyType energyType : EnergyType.values())
		{
			if (energyType != EnergyType.COLORLESS)
			{
				energyCount += getCost(energyType);
			}
		}
		
		return energyCount;
	}
	
	public void setCost(EnergyType inType, byte inCost)
	{
		if (inCost > ByteUtils.MAX_HEX_CHAR_VALUE || inCost < ByteUtils.MIN_BYTE_VALUE)
		{
			throw new IllegalArgumentException("Invalid value was passed for energy type " + inType + " cost: " + inCost);
		}
		energyCost[inType.getValue()] = inCost;
	}
	
	public int readDataAndConvertIds(byte[] moveBytes, int startIndex, RomText cardName, Texts idToText, Set<Short> textIdsUsed) 
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
		
		index = name.readDataAndConvertIds(moveBytes, index, idToText, textIdsUsed);
		
		int[] descIndexes = {index, index + RomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.readDataAndConvertIds(moveBytes, descIndexes, cardName, idToText, textIdsUsed);
		index += RomConstants.TEXT_ID_SIZE_IN_BYTES * descIndexes.length;
		
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

	public int convertToIdsAndWriteData(byte[] moveBytes, int startIndex, RomText cardName, Texts idToText) 
	{
		int index = startIndex;
		
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.FIRE), getCost(EnergyType.GRASS));
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.LIGHTNING), getCost(EnergyType.WATER));
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.FIGHTING), getCost(EnergyType.PSYCHIC));
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.COLORLESS), getCost(EnergyType.UNUSED_TYPE));

		name.convertToIdsAndWriteData(moveBytes, index, idToText);
		index += RomConstants.TEXT_ID_SIZE_IN_BYTES;

		int[] descIndexes = {index, index + RomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.convertToIdsAndWriteData(moveBytes, descIndexes, cardName, idToText);
		index += RomConstants.TEXT_ID_SIZE_IN_BYTES * descIndexes.length;
		
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
