package rom;

import java.io.File;

import compiler.CodeBlock;
import compiler.static_instructs.RawBytes;
import data.PtcgInstructionParser;
import data.custom_card_effects.CustomCardEffect;
import data.custom_card_effects.HardcodedEffects;
import data.romtexts.LdtxInstruct;
import gbc_framework.rom_addressing.AddressRange;
import gbc_framework.rom_addressing.AssignedAddresses;
import gbc_framework.rom_addressing.BankAddress;
import rom_packer.Blocks;
import rom_packer.DataManager;
import rom_packer.FixedBlock;
import rom_packer.MovableBlock;

public class Rom
{
	// TODO later: with tweak to allow 11 cards in pack, make this private
	private byte[] rawBytes;
	
	// Make public - we will be modifying these
	public Cards allCards;
	public Texts idsToText;
	public Blocks blocks;
	
	private boolean dirtyBit;
	
	public Rom(byte[] romRaw)
	{
		rawBytes = romRaw;
		dirtyBit = true;
		resetRom();
	}

	public void resetRom()
	{
		if (dirtyBit)
		{
			dirtyBit = false;
			
			allCards = new Cards();
			idsToText = new Texts();
			blocks = new Blocks();
			
			readRomData();
		}
	}
	
	public void resetAndPrepareForModification()
	{
		if (dirtyBit)
		{
			resetRom();
		}
		dirtyBit = true;
	}

	private void readRomData()
	{	
		idsToText = RomIO.readTextsFromData(rawBytes, blocks);
		allCards = RomIO.readCardsFromData(rawBytes, idsToText, blocks);
	}
	
	public void writePatch(File patchFile)
	{
		// Create the custom parser and set the data blocks to use it
		PtcgInstructionParser parser = new PtcgInstructionParser();
		CodeBlock.setInstructionParserSingleton(parser);
		
		// TODO later: Need to handle tweak blocks somehow. Should these all be
		// file defined and selected via a menu? could also include if they default
		// to on or not. Also for now we can handle these after the other blocks
		// are generated but we arbitrarily do it before. Is there any reason to
		// do one or the other?
		CustomCardEffect.addTweakToAllowEffectsInMoreBanks(blocks);
		
		// Finalize all the data to prepare for writing
		finalizeDataAndGenerateBlocks(parser);
		
		// Now assign locations for the data
		DataManager manager = new DataManager();		
		AssignedAddresses assignedAddresses = manager.allocateBlocks(rawBytes, blocks);
			
		
		blocks = new Blocks();
		assignedAddresses = new AssignedAddresses();
//		CodeBlock code = new CodeBlock("ChangeVenusaurName");
//		code.appendInstruction(new RawBytes((byte)84, (byte)101, (byte)115, (byte)116));
//		FixedBlock block = new FixedBlock(code, 358127);
//		blocks.addFixedBlock(block);
	
//		BankAddress address = block.getFixedAddress();
//		block.assignBank(address.getBank(), assignedAddresses);
//		block.assignAddresses(address, assignedAddresses);
//		CodeBlock venuName = new CodeBlock("ChangeVenusaurName");
//		code.appendInstruction(new RawBytes("Test-A-Saur".getBytes()));
//		MovableBlock nameBlock = new MovableBlock(venuName, 1, (byte)0xd, (byte)0x1c);
//		blocks.addMovableBlock(nameBlock);
		
		// Now change the card to point to the new block
		
//		if (!freeSpace.get(address.getBank()).addFixedBlock(block, assignedAddresses))
//		{
//            throw new RuntimeException(String.format("There was not space from 0x%x to 0x%x in bank 0x%x for FixedBlock %s", 
//            		address.getAddressInBank(), address.getAddressInBank() + block.getWorstCaseSize(assignedAddresses),
//            		address.getBank(), block.getId())); 
//		}
		
		RomIO.writeBpsPatch(patchFile, rawBytes, blocks, assignedAddresses);
	}
	
	private void finalizeDataAndGenerateBlocks(PtcgInstructionParser parser)
	{
		// Reset the singleton -- TODO later: Needed?
		HardcodedEffects.reset();
		
		// Finalize the card data, texts and blocks
		allCards.finalizeConvertAndAddData(idsToText, blocks);
		
		// Now add all the text from the custom parser instructions
		parser.finalizeAndAddTexts(idsToText);
		
		// Convert the text to blocks
		idsToText.convertAndAddBlocks(blocks);
		
		// Sort them and combine values to make things easier elsewhere in the code
		// TODO later: if adding custom blanking, we should call this afterwards
		AddressRange.sortAndCombine(blocks.getAllBlankedBlocks());
	}
}
