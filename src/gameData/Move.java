package gameData;

import java.util.Set;

import constants.CardDataConstants.*;
import util.IoUtils;

public class Move implements GameData 
{
	public static int moveSizeInBytes = 19;
	
	EnergyType energyType1;
	byte numOfEnergyType1; // TODO: Max allowed?
	EnergyType energyType2;
	byte numOfEnergyType2; // TODO: Max allowed?
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

	@Override
	public int readData(byte[] moveBytes, int startIndex) 
	{
		int index = startIndex;
		
		energyType1 = EnergyType.readFromByte(moveBytes[index++]);
		numOfEnergyType1 = moveBytes[index++];
		energyType2 = EnergyType.readFromByte(moveBytes[index++]);
		numOfEnergyType2 = moveBytes[index++];
		name = IoUtils.readShort(moveBytes, index);
		index += 2;
		description = IoUtils.readShort(moveBytes, index);
		index += 2;
		descriptionExtended = IoUtils.readShort(moveBytes, index);
		index += 2;
		damage = moveBytes[index++];
		category = MoveCategory.readFromByte(moveBytes[index++]);
		effectCommands = IoUtils.readShort(moveBytes, index);
		index += 2;
		effect1 = MoveEffect1.readFromByte(moveBytes[index++]);
		effect2 = MoveEffect2.readFromByte(moveBytes[index++]);
		effect3 = MoveEffect3.readFromByte(moveBytes[index++]);
		unknownByte = moveBytes[index++];
		animation = moveBytes[index++];
		
		return index;
	}

	@Override
	public int writeData(byte[] moveBytes, int startIndex) 
	{
		int index = startIndex;
		
		moveBytes[index++] = energyType1.getValue();
		moveBytes[index++] = numOfEnergyType1;
		moveBytes[index++] = energyType2.getValue();
		moveBytes[index++] = numOfEnergyType2;
		IoUtils.writeShort(name, moveBytes, index);
		index += 2;
		IoUtils.writeShort(description, moveBytes, index);
		index += 2;
		IoUtils.writeShort(descriptionExtended, moveBytes, index);
		index += 2;
		moveBytes[index++] = damage;
		moveBytes[index++] = category.getValue();
		IoUtils.writeShort(effectCommands, moveBytes, index);
		index += 2;
		moveBytes[index++] = MoveEffect1.storeAsByte(effect1);
		moveBytes[index++] = MoveEffect2.storeAsByte(effect2);
		moveBytes[index++] = MoveEffect3.storeAsByte(effect3);
		moveBytes[index++] = unknownByte;
		moveBytes[index++] = animation;
		
		return index;
	}
}
