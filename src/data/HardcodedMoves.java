package data;

import constants.CardConstants.CardId;
import constants.DuelConstants.EffectCommandTypes;
import rom.Texts;
import util.ByteUtils;
import util.RomUtils;

public class HardcodedMoves 
{
	static void replaceAllInByteArray(byte[] array, byte toFind, byte replaceWith)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == toFind)
			{
				array[i] = replaceWith;
			}
		}
	}
	
	public static class CallForFamily
	{
		// farcall (1031b - 4:431b) bytes:
		// EF
		// [BankNumber] - 4
		// [address] 1b, 43
		// EF, 4, 1B, 43
		
		// For saving to locations in the ROM
		static final int[] AVAIL_PLAYER_SEL_START_ADDRS = {0x2cc50};
		static final int[] AVAIL_AI_SEL_START_ADDRS = {0x2ccad};
		static int nextIndexToUse = 0;
		
		

		static final int INITIAL_EFFECT_ADDRESS = 0x2cc40; // Bellsprout's but they are all the same even for nidoran
		static final int PUT_IN_PLAY_AREA_EFFECT_ADDRESS = 0x2ccc2; // Bellsprout's but they are all the same even for nidoran

		// I've debating hardcoding the effect like this vs reading it from the ROM. This seems the "safer" way to do it
		// but if we assume we can overwrite and reference the locations of the previous functions, I'm not sure it really 
		// is any safer
		
		// Taken from bellsprout (id 0x23)
		final static byte[] playerSelectEffect = new byte[] {
				(byte) 0x3e, (byte) 0xff, (byte) 0xe0, (byte) 0xa0, (byte) 0xcd, (byte) 0xdf, (byte) 0x11, 
				(byte) 0x21, (byte) 0x2e, (byte) 0x1, (byte) 0x1, (byte) 0x41, (byte) 0x1, (byte) 0x11, 
				(byte) 0x23, (byte) 0x0, (byte) 0xcd, (byte) 0xec, (byte) 0x42, (byte) 0xd8, (byte) 0xdf, 
				(byte) 0x91, (byte) 0x55, (byte) 0x21, (byte) 0x2f, (byte) 0x1, (byte) 0x11, (byte) 0xa9, 
				(byte) 0x0, (byte) 0xdf, (byte) 0x80, (byte) 0x55, (byte) 0xdf, (byte) 0xf0, (byte) 0x55, 
				(byte) 0x38, (byte) 0x16, (byte) 0xcd, (byte) 0x24, (byte) 0x13, (byte) 0x1, (byte) 0x23, 
				(byte) 0x0, (byte) 0xcd, (byte) 0x90, (byte) 0x30, (byte) 0x20, (byte) 0x6, (byte) 0xf0, 
				(byte) 0x98, (byte) 0xe0, (byte) 0xa0, (byte) 0xb7, (byte) 0xc9, (byte) 0xcd, (byte) 0x94, 
				(byte) 0x37, (byte) 0x18, (byte) 0xe5, (byte) 0x3e, (byte) 0x0, (byte) 0xcd, (byte) 0xb, 
				(byte) 0x16, (byte) 0x7e, (byte) 0xfe, (byte) 0x0, (byte) 0x20, (byte) 0xc, (byte) 0x7d, 
				(byte) 0xcd, (byte) 0x24, (byte) 0x13, (byte) 0x1, (byte) 0x23, (byte) 0x0, (byte) 0xcd, 
				(byte) 0x90, (byte) 0x30, (byte) 0x28, (byte) 0xe5, (byte) 0x2c, (byte) 0x7d, (byte) 0xfe, 
				(byte) 0x3c, (byte) 0x38, (byte) 0xe9, (byte) 0x3e, (byte) 0xff, (byte) 0xe0, (byte) 0xa0, 
				(byte) 0xb7, (byte) 0xc9
		};
		
		final static byte[] aiSelectEffect = new byte[] {
				(byte) 0xcd, (byte) 0xdf, (byte) 0x11, (byte) 0x21, (byte) 0x10, (byte) 0xc5, (byte) 0x2a,
				(byte) 0xe0, (byte) 0xa0, (byte) 0xfe, (byte) 0xff, (byte) 0xc8, (byte) 0xcd, (byte) 0x24,
				(byte) 0x13, (byte) 0x7b, (byte) 0xfe, (byte) 0x23, (byte) 0x20, (byte) 0xf2, (byte) 0xc9
		};
		
		public static void resetTracker()
		{
			nextIndexToUse = 0;
		}
		
		public static void writeEffectCommand(byte[] romBytes, int effectCommandStartAddress, int playerSelectStartAddress, int aiSelectStartAddress)
		{
			// Same for all call for families
			romBytes[effectCommandStartAddress++] = EffectCommandTypes.InitialEffect1.getValue();
			ByteUtils.writeAsShort(RomUtils.convertToInBankOffset(INITIAL_EFFECT_ADDRESS), romBytes, effectCommandStartAddress);
			effectCommandStartAddress += 2;

			// Same for all call for families
			romBytes[effectCommandStartAddress++] = EffectCommandTypes.AfterDamage.getValue();
			ByteUtils.writeAsShort(RomUtils.convertToInBankOffset(PUT_IN_PLAY_AREA_EFFECT_ADDRESS), romBytes, effectCommandStartAddress);
			effectCommandStartAddress += 2;
			
			// Unique to card
			romBytes[effectCommandStartAddress++] = EffectCommandTypes.RequireSelection.getValue();
			ByteUtils.writeAsShort(RomUtils.convertToInBankOffset(playerSelectStartAddress), romBytes, effectCommandStartAddress);
			effectCommandStartAddress += 2;

			// Unique to card
			romBytes[effectCommandStartAddress++] = EffectCommandTypes.AiSelection.getValue();
			ByteUtils.writeAsShort(RomUtils.convertToInBankOffset(aiSelectStartAddress), romBytes, effectCommandStartAddress);
			effectCommandStartAddress += 2;
			
			// Ends the effect
			romBytes[effectCommandStartAddress] = 0;
		}
		
		public static int writeEffectToMemory(byte[] romBytes, OneLineText cardName, CardId cardId, Texts idToText) 
		{
			// Identify the open location
			int playerSelectStartAddress;
			int aiSelectStartAddress;
			final int bank6FreeSpace = 0x1baf8;
			int effectCommandAddress = bank6FreeSpace;
			if (nextIndexToUse < AVAIL_PLAYER_SEL_START_ADDRS.length)
			{
				playerSelectStartAddress = AVAIL_PLAYER_SEL_START_ADDRS[nextIndexToUse];
				aiSelectStartAddress = AVAIL_AI_SEL_START_ADDRS[nextIndexToUse];
				nextIndexToUse++;
			}
			else
			{
				throw new IllegalArgumentException("TODO: Not yet implemented looking for free space!");
			}

			// Write the base function to the location in memory then modify them
			ByteUtils.copyBytes(romBytes, playerSelectStartAddress, playerSelectEffect);
			ByteUtils.copyBytes(romBytes, aiSelectStartAddress, aiSelectEffect);

			// Change the card Ids
			
			// Replace the BellsproutID at 14, 41, 74 so it actually looks for and
			// selects the right card
			romBytes[playerSelectStartAddress + 14] = cardId.getValue();
			romBytes[playerSelectStartAddress + 41] = cardId.getValue();
			romBytes[playerSelectStartAddress + 74] = cardId.getValue();
			
			// 17 is where bellsprout ID is
			romBytes[aiSelectStartAddress + 17] = cardId.getValue();
			
			// TODO come back to this
			// Create the texts
//			OneLineText chosePoke = new OneLineText();
//			chosePoke.setTextVerbatim("Choose a " + cardName.getText());
//			
//			PokeDescription chosePokeFromDeck = new PokeDescription();
//			chosePokeFromDeck.setTextVerbatim("Choose a " + cardName.getText() + " from the deck.");
//
//			// Write the indexes of the text to the effect bytes and save the texts if needed
//			chosePokeFromDeck.convertToIdsAndWriteData(romBytes, playerSelectStartAddress + 8, idToText);
//			cardName.convertToIdsAndWriteData(romBytes, playerSelectStartAddress + 11, idToText);
//			chosePoke.convertToIdsAndWriteData(romBytes, playerSelectStartAddress + 24, idToText);

			// Finally, create and write the effect command
			writeEffectCommand(romBytes, effectCommandAddress, playerSelectStartAddress, aiSelectStartAddress);
			
			return effectCommandAddress;
		}
	}
}
