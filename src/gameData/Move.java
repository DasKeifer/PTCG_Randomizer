package gameData;

import java.util.Set;

import constants.CardDataConstants.*;
import util.ByteUtils;

public class Move implements GameData 
{
	public static int moveSizeInBytes = 19;
	
	// TODO these need to be only 8 large at most
	byte[] energyCost;
	short name;
	short description;
	short descriptionExtended;
	byte damage; // TODO: non multiple of 10?
	MoveCategory category;
	short effectCommands; // TODO: Make enum
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
				"\nEffectCommands: " + effectCommands +
				"\nEffectFlags: " + effect1 + ", " + effect2 + ", " + effect3;
				
	}
	
	@Override
	public int readData(byte[] moveBytes, int startIndex) 
	{
		int index = startIndex;
		
		// They are stored in octects corresponding to their energy type. Since we
		// read them as bytes, we mask each byte and increment the index every other time
		System.out.println(moveBytes[index] + ", " + moveBytes[index+1] + ", " + moveBytes[index+2] + ", " + moveBytes[index+3]);
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
		
		name = ByteUtils.readAsShort(moveBytes, index);
		index += 2;
		description = ByteUtils.readAsShort(moveBytes, index);
		index += 2;
		descriptionExtended = ByteUtils.readAsShort(moveBytes, index);
		index += 2;
		damage = moveBytes[index++];
		category = MoveCategory.readFromByte(moveBytes[index++]);
		effectCommands = ByteUtils.readAsShort(moveBytes, index);
		index += 2;
		effect1 = MoveEffect1.readFromByte(moveBytes[index++]);
		effect2 = MoveEffect2.readFromByte(moveBytes[index++]);
		effect3 = MoveEffect3.readFromByte(moveBytes[index++]);
		unknownByte = moveBytes[index++];
		animation = moveBytes[index++];
		
		return index;
	}
	
	public void setCost(EnergyType inType, byte inCost)
	{
		energyCost[inType.getValue()] = inCost;
	}

	@Override
	public int writeData(byte[] moveBytes, int startIndex) 
	{
		int index = startIndex;
		
		// TODO
		ByteUtils.writeAsShort(name, moveBytes, index);
		index += 2;
		ByteUtils.writeAsShort(description, moveBytes, index);
		index += 2;
		ByteUtils.writeAsShort(descriptionExtended, moveBytes, index);
		index += 2;
		moveBytes[index++] = damage;
		moveBytes[index++] = category.getValue();
		ByteUtils.writeAsShort(effectCommands, moveBytes, index);
		index += 2;
		moveBytes[index++] = MoveEffect1.storeAsByte(effect1);
		moveBytes[index++] = MoveEffect2.storeAsByte(effect2);
		moveBytes[index++] = MoveEffect3.storeAsByte(effect3);
		moveBytes[index++] = unknownByte;
		moveBytes[index++] = animation;
		
		return index;
	}
}
