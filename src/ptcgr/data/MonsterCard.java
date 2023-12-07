package ptcgr.data;

import java.util.ArrayList;
import java.util.List;

import compiler.CodeBlock;
import compiler.InstructionParser;
import compiler.RawBytePacker;
import rom_packer.Blocks;
import gbc_framework.utils.ByteUtils;
import ptcgr.constants.CardDataConstants.*;
import ptcgr.data.romtexts.CardName;
import ptcgr.data.romtexts.MonsterCategory;
import ptcgr.data.romtexts.PokeDescription;
import ptcgr.rom.Cards;
import ptcgr.rom.Texts;

public class MonsterCard extends Card 
{
	public static final int TOTAL_SIZE_IN_BYTES = 65;
	public static final int SIZE_OF_PAYLOAD_IN_BYTES = TOTAL_SIZE_IN_BYTES - CARD_COMMON_SIZE;
	public static final int MAX_NUM_MOVES = 2;
	
	private byte hp; // TODO later: non multiples of 10?
	public EvolutionStage stage; // TODO later: Encaspsulate?
	public CardName prevEvoName; // TODO later: Encaspsulate?
	
	private Move[] moves;
	
	public byte retreatCost; // TODO later: max allowed?
	public WeaknessResistanceType weakness; // Allows multiple
	public WeaknessResistanceType resistance; // Allows multiple
	public MonsterCategory monsterCategory; // TODO later: Investigate? Any gameplay impact?
	public byte dexNumber;
	public byte unknownByte1; // TODO later: Always 0?
	public byte level; // TODO later: Investigate No gameplay impact?
	public byte lengthFt; // TODO later: Investigate No gameplay impact?
	public byte lengthIn; // TODO later: Investigate No gameplay impact?
	public short weight; // TODO later: Investigate No gameplay impact?
	public PokeDescription description;
	public byte unknownByte2; // TODO later: At least somewhat tracks with evo stage in asm files - 19 for first stage, 16 for second stage, 0 for final stage?	
	
	public MonsterCard()
	{
		super();
		
		prevEvoName = new CardName(true); // Pokename
		moves = new Move[MAX_NUM_MOVES];
		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			moves[moveIndex] = new Move();
		}
		monsterCategory = new MonsterCategory();
		description = new PokeDescription();
	}
	
	public MonsterCard(MonsterCard toCopy)
	{
		super(toCopy);
		
		setHp(toCopy.getHp());
		stage = toCopy.stage;
		prevEvoName = new CardName(toCopy.prevEvoName);
		moves = new Move[MAX_NUM_MOVES];
		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			moves[moveIndex] = new Move(toCopy.moves[moveIndex]);
		}
		retreatCost = toCopy.retreatCost;
		weakness = toCopy.weakness;
		resistance = toCopy.resistance;
		monsterCategory = new MonsterCategory(toCopy.monsterCategory);
		dexNumber = toCopy.dexNumber;
		unknownByte1 = toCopy.unknownByte1;
		level = toCopy.level;
		lengthFt = toCopy.lengthFt;
		lengthIn = toCopy.lengthIn;
		weight = toCopy.weight;
		description = new PokeDescription(toCopy.description);
		unknownByte2 = toCopy.unknownByte2;
	}
	
	@Override
	protected CardName createCardName()
	{
		return new CardName(true); // a pokename
	}
	
	public MonsterCard copy()
	{
		return new MonsterCard(this);
	}

	public List<Move> getAllMovesIncludingEmptyOnes()
	{
		ArrayList<Move> movesList = new ArrayList<>();
		for (Move move : moves)
		{
			movesList.add(new Move(move));
		}
		return movesList;
	}
	
	public List<Move> getAllNonEmptyMoves()
	{
		ArrayList<Move> movesList = new ArrayList<>();
		for (Move move : moves)
		{
			if (!move.isEmpty())
			{
				movesList.add(new Move(move));
			}
		}
		return movesList;
	}
	
	public int getNumMoves()
	{
		int numMoves = 0;
		for (Move move : moves)
		{
			if (!move.isEmpty())
			{
				numMoves++;
			}
		}
		return numMoves;
	}

	// TODO later: investigate return immutable object instead of a copy
	public Move getMove(int moveIndex)
	{
		if (moveIndex > moves.length)
		{
			return Move.EMPTY_MOVE;
		}
		
		return new Move(moves[moveIndex]);
	}

	public Move getMoveWithName(String moveName)
	{
		// For each move see if the name matches and if it does, return the move
		for (Move move : moves)
		{
			if (move.name.toString().equals(moveName))
			{
				return new Move(move);
			}
		}
		
		// If we didn't find a match, return null
		return null;
	}
	
	public boolean setMove(Move move, int moveSlot)
	{
		boolean okay = moveSlot > moves.length;
		if (okay)
		{
			moves[moveSlot] = new Move(move);
		}
		return okay;
	}
	
	public void setMoves(List<Move> newMoves)
	{
		if (newMoves.size() != moves.length)
		{
			throw new IllegalArgumentException("Bad number of moves (" + newMoves.size() + ") was passed!");
		}
		
		for (int moveIndex = 0; moveIndex < moves.length; moveIndex++)
		{
			moves[moveIndex] = new Move(newMoves.get(moveIndex));
		}
	}
	
	public void sortMoves()
	{
		Move tempMove;
		boolean needsSwap;
		for (int moveIndex = 0; moveIndex < moves.length - 1; moveIndex++)
		{
			needsSwap = false;
			// Move empty moves to the end
			if (moves[moveIndex].isEmpty() || moves[moveIndex + 1].isEmpty())
			{
				if (moves[moveIndex].isEmpty() && !moves[moveIndex + 1].isEmpty() )
				{
					needsSwap = true;
				}
			}
			// Move poke powers first
			else if (!moves[moveIndex].isPokePower() && moves[moveIndex + 1].isPokePower())
			{
				needsSwap = true;
			}
			else
			{
				int numColorless1 = moves[moveIndex].getCost(EnergyType.COLORLESS);
				int numColorless2 = moves[moveIndex + 1].getCost(EnergyType.COLORLESS);
				int numNonColorless1 = moves[moveIndex].getNonColorlessEnergyCosts();
				int numNonColorless2 = moves[moveIndex + 1].getNonColorlessEnergyCosts();
				
				// Move higher total energies last
				if (numColorless1 + numNonColorless1 > numColorless2 + numNonColorless2)
				{
					needsSwap = true;
				}
				else if (numColorless1 + numNonColorless1 == numColorless2 + numNonColorless2)
				{
					// If equal num, move more non-colorless last
					if (numNonColorless1 > numNonColorless2)
					{
						needsSwap = true;
					}
					else if (numNonColorless1 == numNonColorless2)
					{
						// If equal move higher damage last
						if (moves[moveIndex].damage > moves[moveIndex + 1].damage)
						{
							needsSwap = true;
						}
						// If equal, moves with effects last
						else if (moves[moveIndex].damage == moves[moveIndex + 1].damage &&
								!moves[moveIndex].description.isEmpty() && moves[moveIndex + 1].description.isEmpty())
						{
							needsSwap = true;
						}
					}
				}
			}
				
			if (needsSwap)
			{
				tempMove = moves[moveIndex];
				moves[moveIndex] = moves[moveIndex + 1];
				moves[moveIndex + 1] = tempMove;
				moveIndex = 0; // restart sort loop
			}
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString() + 
				"\nPokedex Number = " + dexNumber + 
				"\nDesciption = " + description.toString() + 
				"\nHP = " + getHp() +
				"\nStage = " + stage + 
				"\nPrevEvolution = " + prevEvoName.toString() +
				"\nRetreatCost = " + retreatCost +
				"\nWeakness = " + weakness +
				"\nResistance = " + resistance  +
				"\nMoves");

		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			builder.append("\n" + moves[moveIndex].toString());
		}
		return builder.toString();
	}
	
	@Override
	public int readAndConvertIds(byte[] cardBytes, int startIndex, Texts idToText) 
	{
		commonReadAndConvertIds(cardBytes, startIndex, idToText);
		
		int index = startIndex + Card.CARD_COMMON_SIZE;
		setHp(cardBytes[index++]);
		stage = EvolutionStage.readFromByte(cardBytes[index++]);
		
		// Read the prev evolution
		index = prevEvoName.readDataAndConvertIds(cardBytes, index, idToText);

		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			index = moves[moveIndex].readDataAndConvertIds(cardBytes, index, name, idToText);
		}
		
		retreatCost = cardBytes[index++];
		weakness = WeaknessResistanceType.readFromByte(cardBytes[index++]);
		resistance = WeaknessResistanceType.readFromByte(cardBytes[index++]);

		index = monsterCategory.readDataAndConvertIds(cardBytes, index, idToText);
		
		dexNumber = cardBytes[index++];
		unknownByte1 = cardBytes[index++];
		level = cardBytes[index++];
		lengthFt = cardBytes[index++];
		lengthIn = cardBytes[index++];
		weight = ByteUtils.readAsShort(cardBytes, index);
		index += 2;
		
		index = description.readDataAndConvertIds(cardBytes, index, idToText);
		
		unknownByte2 = cardBytes[index++];

		return TOTAL_SIZE_IN_BYTES;
	}
	
	@Override
	public void finalizeAndAddData(Cards cards, Texts texts, Blocks blocks, InstructionParser parser)
	{
		commonFinalizeAndAddData(texts);
		
		prevEvoName.finalizeAndAddTexts(texts);
		monsterCategory.finalizeAndAddTexts(texts);
		description.finalizeAndAddTexts(texts);

		sortMoves();
		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			moves[moveIndex].finalizeAndAddData(cards, texts, blocks, this, parser);
		}
	}

	@Override
	protected CodeBlock convertToCodeBlock() 
	{
		CodeBlock block = convertCommonDataToCodeBlock();
		
		RawBytePacker bytes = new RawBytePacker();
		bytes.append(
				getHp(),
				stage.getValue()
		);
		bytes.append(ByteUtils.shortToLittleEndianBytes(prevEvoName.getTextId()));
		block.appendInstruction(bytes.createRawByteInsruct());
				
		for (int moveIndex = 0; moveIndex < MAX_NUM_MOVES; moveIndex++)
		{
			moves[moveIndex].appendToCodeBlock(block);
		}

		bytes = new RawBytePacker();
		bytes.append(
				retreatCost,
				weakness.getValue(),
				resistance.getValue()
		);
		bytes.append(ByteUtils.shortToLittleEndianBytes(monsterCategory.getTextId()));
		bytes.append(
				dexNumber,
				unknownByte1,
				level
		);
		bytes.append(
				lengthFt, 
				lengthIn
		);
		bytes.append(ByteUtils.shortToLittleEndianBytes(weight));
		bytes.append(ByteUtils.shortToLittleEndianBytes(description.getTextId()));
		bytes.append(unknownByte2);
		block.appendInstruction(bytes.createRawByteInsruct());
		
		return block;
	}
	
	@Override
	public int getSize()
	{
		return TOTAL_SIZE_IN_BYTES;
	}

	public byte getHp() 
	{
		return hp;
	}

	public boolean setHp(int hp) 
	{
		// TODO: Enforce multiple of 10 and not too large (<= 120?)
		this.hp = (byte)hp;
		return true;
	}
}
