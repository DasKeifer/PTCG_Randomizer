package gameData;

import Util.IoUtils;

public class NonPokemonCard extends Card
{
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = 14 - CARD_COMMON_SIZE;
	
	short effect;
	short description;
	short descriptionCont;
	
	@Override
	public int readData(byte[] cardBytes, int startIndex) 
	{
		int index = readCommonData(cardBytes, startIndex);
		
		// reading non pokemon specific data
		effect = IoUtils.readShort(cardBytes, index);
		index += 2;
		description = IoUtils.readShort(cardBytes, index);
		index += 2;
		descriptionCont = IoUtils.readShort(cardBytes, index);
		return index + 2;
	}
	
	@Override
	public int writeData(byte[] cardBytes, int startIndex) 
	{
		int index = writeCommonData(cardBytes, startIndex);
		
		// write non pokemon specific data
		IoUtils.writeShort(effect, cardBytes, index);
		index += 2;
		IoUtils.writeShort(description, cardBytes, index);
		index += 2;
		IoUtils.writeShort(descriptionCont, cardBytes, index);		
		return index + 2;
	}
}
