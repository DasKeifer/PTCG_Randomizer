package gameData;

import java.util.Set;

import constants.CardDataConstants.*;
import util.ByteUtils;

public class Move implements GameData
{
	public static final int TOTAL_SIZE_IN_BYTES = 19;

	// Internal pointers used when reading and storing data to the rom
	private short namePtr;
	private short descriptionPtr;
	private short descriptionExtendedPtr;
	
	byte[] energyCost;
	String name;
	String desciption;
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
				"\nEffectPtr: " + effectPtr +
				"\nEffectFlags: " + effect1 + ", " + effect2 + ", " + effect3;
				
	}
	
	@Override
	public int readData(byte[] moveBytes, int startIndex) 
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
		
		namePtr = ByteUtils.readAsShort(moveBytes, index);
		index += 2;
		descriptionPtr = ByteUtils.readAsShort(moveBytes, index);
		index += 2;
		descriptionExtendedPtr = ByteUtils.readAsShort(moveBytes, index);
		index += 2;
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

	@Override
	public int writeData(byte[] moveBytes, int startIndex) 
	{
		int index = startIndex;
		
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.FIRE), getCost(EnergyType.GRASS));
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.LIGHTNING), getCost(EnergyType.WATER));
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.FIGHTING), getCost(EnergyType.PSYCHIC));
		moveBytes[index++] = ByteUtils.packHexCharsToByte(getCost(EnergyType.COLORLESS), getCost(EnergyType.UNUSED_TYPE));
		
		ByteUtils.writeAsShort(namePtr, moveBytes, index);
		index += 2;
		ByteUtils.writeAsShort(descriptionPtr, moveBytes, index);
		index += 2;
		ByteUtils.writeAsShort(descriptionExtendedPtr, moveBytes, index);
		index += 2;
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
