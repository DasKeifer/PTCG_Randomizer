package data;

import java.util.Comparator;
import java.util.EnumMap;
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
	public static final Comparator<Move> BASIC_SORTER = new BasicSorter();
	
	EnumMap<EnergyType, Byte> energyCost;
	public OneLineText name;
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
		energyCost = new EnumMap<>(EnergyType.class);
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
	
	public static class BasicSorter implements Comparator<Move>
	{
		public int compare(Move m1, Move m2)
	    {   
			int compareVal = m1.name.getText().compareTo(m2.name.getText());
			
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
				compareVal = m2.effectPtr - m1.effectPtr;
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
			return damage > 0 || RomConstants.ZERO_DAMAGE_DAMAGING_MOVES.contains(name.getText());
		}
		
		return false;
	}
	
	public boolean isPokePower()
	{
		return !isEmpty() && MoveCategory.POKEMON_POWER == category;
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
		builder.append(effectPtr);
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
	
	public int readDataAndConvertIds(byte[] moveBytes, int startIndex, RomText cardName, Texts idToText, Set<Short> textIdsUsed) 
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
