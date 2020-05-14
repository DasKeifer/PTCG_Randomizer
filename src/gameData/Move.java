package gameData;

import constants.CardDataConstants.*;

public class Move implements GameData 
{
	EnergyType energyType1;
	byte numOfEnergyType1; // TODO: Max allowed?
	EnergyType energyType2;
	byte numOfEnergyType2; // TODO: Max allowed?
	int name;
	int description;
	int descriptionExtended;
	byte damage; // TODO: non multiple of 10?
	MoveCategory category;
	int effectCommands; // TODO: Make enum
	MoveEffect1 effect1;
	MoveEffect2 effect2;
	MoveEffect3 effect3;
	byte unknownByte;
	byte animation;

	@Override
	public void readData(byte[] rom, int startIndex) {
		// TODO:
	}

	@Override
	public void writeData(byte[] rom, int startIndex) {
		// TODO:
	}
}
