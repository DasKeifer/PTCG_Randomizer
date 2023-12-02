package ptcgr.data;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import compiler.CodeBlock;
import compiler.InstructionParser;
import compiler.RawBytePacker;
import compiler.static_instructs.RawBytes;
import rom_packer.Blocks;
import rom_packer.MovableBlock;
import gbc_framework.utils.ByteUtils;
import ptcgr.constants.PtcgRomConstants;
import ptcgr.constants.CardDataConstants.*;
import ptcgr.data.custom_card_effects.CustomCardEffect;
import ptcgr.data.custom_card_effects.HardcodedEffects;
import ptcgr.data.romtexts.EffectDescription;
import ptcgr.data.romtexts.MoveName;
import ptcgr.data.romtexts.RomText;
import ptcgr.rom.Cards;
import ptcgr.rom.Texts;

public class Move
{
	public static final int TOTAL_SIZE_IN_BYTES = 19;
	public static final Move EMPTY_MOVE = new Move();
	public static final Comparator<Move> BASIC_SORTER = new BasicSorter();
	
	EnumMap<EnergyType, Byte> energyCost;
	public MoveName name;
	public EffectDescription description;
	public byte damage; // TODO later: non multiple of 10?
	public MoveCategory category;
	CardEffect effect;
	Set<MoveEffect1> effect1;
	Set<MoveEffect2> effect2;
	Set<MoveEffect3> effect3;
	byte unknownByte;
	byte animation;

	public Move()
	{
		energyCost = new EnumMap<>(EnergyType.class);
		name = new MoveName();
		description = new EffectDescription();
		category = MoveCategory.DAMAGE_NORMAL;
		effect = ExistingCardEffect.NONE;
		effect1 = new HashSet<>();
		effect2 = new HashSet<>();
		effect3 = new HashSet<>();
	}
	
	public Move(Move toCopy) 
	{
		energyCost = new EnumMap<>(toCopy.energyCost);
		name = new MoveName(toCopy.name);
		description = new EffectDescription(toCopy.description);
		damage = toCopy.damage;
		category = toCopy.category;
		effect = toCopy.effect.copy();
		effect1 = new HashSet<>(toCopy.effect1);
		effect2 = new HashSet<>(toCopy.effect2);
		effect3 = new HashSet<>(toCopy.effect3);
		unknownByte = toCopy.unknownByte;
		animation = toCopy.animation;
	}
	
	public static class BasicSorter implements Comparator<Move>
	{
		public int compare(Move m1, Move m2)
	    {   
			int compareVal = m1.name.toString().compareTo(m2.name.toString());
			
			if (compareVal == 0)
			{
				compareVal = m1.getDamageString().compareTo(m2.getDamageString());
			}
			
			if (compareVal == 0)
			{
				compareVal = m1.getEnergyCostString(true, "").compareTo(m2.getEnergyCostString(true, ""));
			}
			
			if (compareVal == 0)
			{
				return m1.effect.toString().compareTo(m2.effect.toString());
			}
			
			return compareVal;
	    }
	}

	public boolean isEmpty()
	{
		return name.isEmpty();
	}

	public boolean isAttack()
	{
		return !isEmpty() && !isPokePower();
	}

	public boolean doesDamage()
	{
		if (isAttack())
		{
			// If its listed as doing damage or is one of the moves that does damage just doesn't
			// have an associated damage number, this will return true
			return damage > 0 || PtcgRomConstants.ZERO_DAMAGE_DAMAGING_MOVES.contains(name.toString());
		}
		
		return false;
	}
	
	public boolean isPokePower()
	{
		return !isEmpty() && MoveCategory.POWER == category;
	}
	
	public boolean hasEffect()
	{
		return !description.isEmpty();
	}

	public String getEnergyCostString(boolean abbreviated, String separator)
	{
		StringBuilder sb = new StringBuilder();
		energyCostsAsString(sb, abbreviated, separator);
		return sb.toString();
	}
	
	public void energyCostsAsString(StringBuilder string, boolean abbreviated, String separator)
	{
		boolean foundEnergy = false;
		for (EnergyType energyType : EnergyType.values())
		{
			if (energyCost.get(energyType) != null && energyCost.get(energyType) > 0)
			{
				if (foundEnergy)
				{
					string.append(separator);
				}
				string.append(energyCost.get(energyType));
				string.append(" ");
				if (abbreviated)
				{
					string.append(energyType.getAbbreviation());
				}
				else
				{
					string.append(energyType);
				}
				foundEnergy = true;
			}
		}
		if (!foundEnergy)
		{
			string.append("None");
		}
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Move Name: "); 
		builder.append(name.toString());
		builder.append("\nMove Category: "); 
		builder.append(category);
		builder.append("\nEnergies Required:\n\t");
		
		// Full energy names, separate with newline and tab
		energyCostsAsString(builder, false, "\n\t");
				
		builder.append("\nDamage:");
		builder.append(damage);
		builder.append("\nDescription: ");
		builder.append(description.toString());
		builder.append("\nEffectPtr: ");
		builder.append(effect.toString());
		builder.append("\nEffectFlags: ");
		builder.append(effect1);
		builder.append(", ");
		builder.append(effect2);
		builder.append(", ");
		builder.append(effect3);
		
		return builder.toString();
	}

	public String getDamageString()
	{
		if (hasEffect())
		{
			if (damage == 0)
			{
				return "-*";
			}
			return damage + "*";
		}
		return damage + " ";
	}
	
	public byte getCost(EnergyType inType)
	{
		if (energyCost.get(inType) != null)
		{
			return energyCost.get(inType);
		}
		else
		{
			return 0;
		}
	}
	
	public byte getNonColorlessEnergyCosts()
	{
		byte energyCount = 0;
		for (EnergyType energyType : EnergyType.values())
		{
			if (energyType != EnergyType.COLORLESS)
			{
				energyCount += getCost(energyType);
			}
		}
		
		return energyCount;
	}
	
	public void clearCosts()
	{
		energyCost.clear();
	}
	
	public void setCost(EnergyType inType, byte inCost)
	{
		if (inCost > ByteUtils.MAX_HEX_CHAR_VALUE || inCost < ByteUtils.MIN_BYTE_VALUE)
		{
			throw new IllegalArgumentException("Invalid value was passed for energy type " + inType + " cost: " + inCost);
		}
		energyCost.put(inType, inCost);
	}
	
	public int readDataAndConvertIds(byte[] moveBytes, int startIndex, RomText cardName, Texts idToText) 
	{
		int index = startIndex;
		
		// They are stored in octects corresponding to their energy type. Since we
		// read them as bytes, we mask each byte and increment the index every other time
		energyCost = new EnumMap<>(EnergyType.class);
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
		
		index = name.readDataAndConvertIds(moveBytes, index, idToText);
		
		int[] descIndexes = {index, index + PtcgRomConstants.TEXT_ID_SIZE_IN_BYTES};
		description.readDataAndConvertIds(moveBytes, descIndexes, cardName, idToText);
		index += PtcgRomConstants.TEXT_ID_SIZE_IN_BYTES * descIndexes.length;
		
		damage = moveBytes[index++];
		category = MoveCategory.readFromByte(moveBytes[index++]);
		effect = new ExistingCardEffect(ByteUtils.readAsShort(moveBytes, index));
		index += 2;
		effect1 = MoveEffect1.readFromByte(moveBytes[index++]);
		effect2 = MoveEffect2.readFromByte(moveBytes[index++]);
		effect3 = MoveEffect3.readFromByte(moveBytes[index++]);
		unknownByte = moveBytes[index++];
		animation = moveBytes[index++];
		
		return index;
	}

	public void finalizeAndAddData(Cards cards, Texts texts, Blocks blocks, MonsterCard hostCard, InstructionParser parser)
	{
		name.finalizeAndAddTexts(texts);

		if (name.toString().compareToIgnoreCase("call for family") == 0 && false)
		{
			CardGroup<Card> basics = cards.cards().determineBasicEvolutionOfCard(hostCard);
			if (basics.count() <= 0)
			{
				throw new IllegalArgumentException("Failed to find basic card for " + hostCard.name.toString());
			}
			
			CustomCardEffect custEffect = HardcodedEffects.CallForFamily.createMoveEffect(/*cards,*/ basics, parser);
			List<MovableBlock> effectBlocks = custEffect.convertToBlocks();
			for (MovableBlock block : effectBlocks)
			{
				blocks.addMovableBlock(block);
			}
			
			effect = custEffect;
			// The effect description will only talk about the poke to search for so we
			// replace it with the basic poke's name
			description.finalizeAndAddTexts(texts, basics.listOrderedByCardId().get(0).name.toString());
		}
		else
		{
			description.finalizeAndAddTexts(texts, hostCard.name.toString());
		}
	}

	public void appendToCodeBlock(CodeBlock block)
	{
		RawBytePacker bytes = new RawBytePacker();
		bytes.append(
				ByteUtils.packHexCharsToByte(getCost(EnergyType.FIRE), getCost(EnergyType.GRASS)),
				ByteUtils.packHexCharsToByte(getCost(EnergyType.LIGHTNING), getCost(EnergyType.WATER)),
				ByteUtils.packHexCharsToByte(getCost(EnergyType.FIGHTING), getCost(EnergyType.PSYCHIC)),
				ByteUtils.packHexCharsToByte(getCost(EnergyType.COLORLESS), getCost(EnergyType.UNUSED_TYPE))
		);
		bytes.append(ByteUtils.shortToLittleEndianBytes(name.getTextId()));
		bytes.append(ByteUtils.shortListToLittleEndianBytes(description.getTextIds(PtcgRomConstants.MAX_BLOCKS_EFFECT_DESC)));
		bytes.append(
				damage,
				category.getValue()
		);
		block.appendInstruction(bytes.createRawByteInsruct());
		
		effect.appendToCodeBlock(block);
		
		block.appendInstruction(new RawBytes(
				MoveEffect1.storeAsByte(effect1),
				MoveEffect2.storeAsByte(effect2),
				MoveEffect3.storeAsByte(effect3),
				unknownByte,
				animation
		));
	}
}
