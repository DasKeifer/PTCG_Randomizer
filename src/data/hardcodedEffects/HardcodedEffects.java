package data.hardcodedEffects;

import java.util.HashMap;
import java.util.Map;

import compiler.CompilerUtils;
import compiler.DataBlock;
import constants.CardDataConstants.EnergyType;
import constants.CharMapConstants;
import constants.DuelConstants.EffectCommandTypes;
import data.Card;
import data.CardEffect;
import rom.Cards;
import util.RomUtils;

public class HardcodedEffects 
{
	public enum MoveUniqueness
	{
		GLOBALLY,
		PER_ENERGY_TYPE,
		PER_CARD_NAME,
		PER_CARD_ID,
	}
	
	private static HardcodedEffects singleton = new HardcodedEffects();
	
	// Effect name, card name, effect
	public Map<String, Map<String, CardEffect>> cardNameUniqueEffects;
	// Effect name, energy type, effect
	public Map<String, Map<EnergyType, CardEffect>> energyTypeUniqueEffects;
	// Effect name, effect
	public Map<String, CardEffect> globallyUniqueEffects;
	
	private HardcodedEffects()
	{
		cardNameUniqueEffects = new HashMap<>();
		energyTypeUniqueEffects = new HashMap<>();
		globallyUniqueEffects = new HashMap<>();
	}
	
	public static void reset()
	{
		singleton = new HardcodedEffects();
	}
	
	public static HardcodedEffects getInstance() 
	{
		return singleton;
	}
	
	public CardEffect tryGetCardNameUniqueEffect(String effectName, String cardName)
	{
		Map<String, CardEffect> effectMap = cardNameUniqueEffects.get(effectName);
		if (effectMap == null)
		{
			return null;
		}
		
		return effectMap.get(cardName);
	}
	
	public void addCardNameUniqueEffect(String effectName, String cardName, CardEffect effect)
	{
		Map<String, CardEffect> effectMap = cardNameUniqueEffects.get(effectName);
		if (effectMap == null)
		{
			effectMap = new HashMap<>();
			cardNameUniqueEffects.put(effectName, effectMap);
		}
		
		effectMap.put(cardName, effect);
	}
	
	// TODO: Make so this can be read in from a file. Have a Constants file for all these static things
	// then have more files that define the datablocks - i.e. their preferences/required locations and
	// the code itself
	
	// TODO: Have a move effect class that contains a uniqueness aspect of globally, type, pokemonname, etc.
	// In that class have an effect command and effect pointers
	//
	// Have a higher level move effect tracker class that will pull out all the rom's move effects based on pokemon
	// card and move name. Add in/replace the custom ones with that. Then if we allow file based tweaking/specifying
	// of moves, we can refer to the ones already in the base rom
	// Then when we save, we can check the move name against our custom set of moves and write the data if needed
	// or get a reference to the existing data
	
	// Things might get trickier if move names are reused with different effects (i.e. discard 1 vs discard 2)
	


	
	
	
	public static String CARD_NAME_PLACEHOLDER = CompilerUtils.createPlaceholder("cardname");
	public static String CARD_ID_PLACEHOLDER = CompilerUtils.createPlaceholder("cardid");

	public static String FUNC_CREATE_DECK_CARD_LIST = CompilerUtils.createPlaceholder("CreateDeckCardList");
	public static String FUNC_CREATE_DECK_CARD_LIST_ADDR = "$11df";

	public static String FUNC_GET_CARD_ID_FROM_DECK_INDEX = CompilerUtils.createPlaceholder("GetCardIDFromDeckIndex");
	public static String FUNC_GET_CARD_ID_FROM_DECK_INDEX_ADDR = "$1324";
	
	public static String FUNC_GET_TURN_DUELIST_VAR = CompilerUtils.createPlaceholder("GetTurnDuelistVariable");
	public static String FUNC_GET_TURN_DUELIST_VAR_ADDR = "$160b";
	
	public static String FUNC_LOOK_FOR_CARDS_IN_DECK = CompilerUtils.createPlaceholder("LookForCardsInDeck");
	public static String FUNC_LOOK_FOR_CARDS_IN_DECK_ADDR = "$2c2ec";
	
	public static String FUNC_DISPLAY_CARD_LIST = CompilerUtils.createPlaceholder("DisplayCardList");
	public static String FUNC_DISPLAY_CARD_LIST_BANK1ADDR = "$55f0";
	
	public static String FUNC_SET_CARD_LIST_HEADER_TEXT = CompilerUtils.createPlaceholder("SetCardListHeaderText");
	public static String FUNC_SET_CARD_LIST_HEADER_TEXT_BANK1ADDR = "$5580";
	
	
	public static String VAR_DUEL_TEMP_LIST = CompilerUtils.createPlaceholder("wDuelTempList");
	public static String VAR_DUEL_TEMP_LIST_ADDR = "$c510";
	
	public static String CONST_DECK_SIZE = CompilerUtils.createPlaceholder("DECK_SIZE");
	public static String CONST_DECK_SIZE_VAL = "$3C"; // 60 in hex
	
	public static String CONST_CARD_LOCATION_DECK = CompilerUtils.createPlaceholder("CARD_LOCATION_DECK");
	public static String CONST_CARD_LOCATION_DECK_VAL = "$0";

	public static String CONST_DUELVARS_CARD_LOCATIONS = CompilerUtils.createPlaceholder("DUELVARS_CARD_LOCATIONS");
	public static String CONST_DUELVARS_CARD_LOCATIONS_VAL = "$0";
	
	public static String CONST_SEARCHEFFECT_CARD_ID = CompilerUtils.createPlaceholder("SEARCHEFFECT_CARD_ID");
	public static String CONST_SEARCHEFFECT_CARD_ID_VAL = "$0";
	
	
	
	

	public static String FUNC_SEARCH_CARD_IN_DECK_AND_ADD_TO_HAND = CompilerUtils.createPlaceholder("SearchCardInDeckAndAddToHand");
	public static String FUNC_SEARCH_CARD_IN_DECK_AND_ADD_TO_HAND_ADDR = "$10fc";

	public static String FUNC_ADD_CARD_TO_HAND = CompilerUtils.createPlaceholder("AddCardToHand");
	public static String FUNC_ADD_CARD_TO_HAND_ADDR = "$1123";

	public static String FUNC_PUT_HAND_POKEMON_IN_PLAY = CompilerUtils.createPlaceholder("PutHandPokemonCardInPlayArea");
	public static String FUNC_PUT_HAND_POKEMON_IN_PLAY_ADDR = "$1485";

	public static String FUNC_IS_PLAYER_TURN = CompilerUtils.createPlaceholder("IsPlayerTurn");
	public static String FUNC_IS_PLAYER_TURN_ADDR = "$2c0c7";

	public static String FUNC_DISPLAY_CARD_DETAILS = CompilerUtils.createPlaceholder("DisplayCardDetailScreen");
	public static String FUNC_DISPLAY_CARD_DETAILS_BANK1ADDR = "$4b31";
	
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
	
	// TODO: Handle cards with multiple IDs (e.g. 4 different pikachus)
	// TODO: Handle multiple cards (e.g. nidoran male or female)
	public static class CallForFamily
	{		
		static final String effectName = "CallForFamily";
		
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

		// Basics may not always be pokemon cards - take mysterious fossil for example
		public static CardEffect createMoveEffect(Cards<Card> cards, Cards<Card> basics) 
		{
			// Just assume the first one for now. This probably won't work well for things like
			// pikachu where there are versions of them
			// TODO: can flying pikachu evolve into raichu? Looks like no since it is comparing
			// the pointer and not the text
//			List<CardId> basicIds = new LinkedList<>();
//			String basicNames = "";
//			
//			// Form the lists of all the cards we can find
			// Later we compile a list and add a check for each one in a separate sub function
			// so we can do it multiple times easily
//			for (Card basic : basics.iterable())
//			{
//				basicIds.add(basic.id);
//				if (basicNames.isEmpty())
//				{
//					basicNames = basic.name.toString();
//				}
//				if (!basicNames.contains(basic.toString()))
//				{
//					basicNames += ", " + basic.name.toString();
//				}
//			}
			
			// For now assume only one id
			Card toFindBasicOf = basics.toList().get(0);
			
			CardEffect effect = HardcodedEffects.getInstance().tryGetCardNameUniqueEffect(effectName, toFindBasicOf.name.toString());
			if (effect != null)
			{
				return effect;
			}
			
			String[] playerSelectPlaceholderCode = new String[] {
					"ld a, $ff",
					"ldh [$ffa0], a",
					"call " + FUNC_CREATE_DECK_CARD_LIST,
					"ldtx hl, halfTextBox:Choose a " + CARD_NAME_PLACEHOLDER + " from the deck",
					"ldtx bc, cardName:" + CARD_NAME_PLACEHOLDER,
					"lb de, " + CONST_SEARCHEFFECT_CARD_ID + ", " + CARD_ID_PLACEHOLDER,
					"call " + FUNC_LOOK_FOR_CARDS_IN_DECK,
					"ret c",

				// draw Deck list interface and print text
					"bank1call $5591",
					"ldtx hl, halfTextBox:Choose a " + CARD_NAME_PLACEHOLDER + ".",
					"ldtx de, textbox:" + CharMapConstants.RAMNAME + "'s Deck", 
					"bank1call " + FUNC_SET_CARD_LIST_HEADER_TEXT,

				".loop",
					"bank1call " + FUNC_DISPLAY_CARD_LIST,
					"jr c, .pressed_b",
					"call " + FUNC_GET_CARD_ID_FROM_DECK_INDEX,
					"ld bc, " + CARD_ID_PLACEHOLDER,
					"call $3090", // compare DE to BC
					"jr nz, .play_sfx",

				// Pokemon was selected
					"ldh a, [$ff98]", 
					"ldh [$ffa0], a",
					"or a",
					"ret", 				// Matches through here

				".play_sfx",
					// play SFX and loop back
					"call $3794", // bank 0
					"jr .loop",

				".pressed_b",
				// figure if Player can exit the screen without selecting,
				// that is, if the Deck has no Bellsprout card.
					"ld a, " + CONST_DUELVARS_CARD_LOCATIONS,
					"call " + FUNC_GET_TURN_DUELIST_VAR,
				".loop_b_press",
					"ld a, [hl]",    								
					"cp " + CONST_CARD_LOCATION_DECK,
					"jr nz, .next",
					"ld a, l",
					"call " + FUNC_GET_CARD_ID_FROM_DECK_INDEX,
					"ld bc, " + CARD_ID_PLACEHOLDER,
					"call $3090", // compare DE to BC
					"jr z, .play_sfx", // found Bellsprout, go back to top loop
				".next",
					"inc l",
					"ld a, l",
					"cp " + CONST_DECK_SIZE,
					"jr c, .loop_b_press",

				// no Bellsprout in Deck, can safely exit screen
					"ld a, $ff",
					"ldh [$ffa0], a",
					"or a",
					"ret"
			};
			
			String[] aiSelectPlaceholderCode = new String[] {
					"call " + FUNC_CREATE_DECK_CARD_LIST,
					"ld hl, " + VAR_DUEL_TEMP_LIST,
				".loop_deck",
					"ld a, [hli]",
					"ldh [$ffa0], a",
					"cp $ff",
					"ret z", // no Bellsprout
					"call " + FUNC_GET_CARD_ID_FROM_DECK_INDEX,
					"ld a, e",
					"cp " + CARD_ID_PLACEHOLDER,
					"jr nz, .loop_deck",
					"ret" // Bellsprout found
			};
			
			DataBlock playerSelect = new DataBlock("CallForFamilyPS" + CARD_NAME_PLACEHOLDER, playerSelectPlaceholderCode);
			DataBlock aiSelect = new DataBlock("CallForFamilyAIS" + CARD_NAME_PLACEHOLDER, aiSelectPlaceholderCode);
			
			Map<String, String> placeholders = new HashMap<>();
			placeholders.put(CARD_NAME_PLACEHOLDER, toFindBasicOf.name.toString());
			placeholders.put(CARD_ID_PLACEHOLDER, String.format("$%x", toFindBasicOf.id.getValue()));
			placeholders.put(FUNC_CREATE_DECK_CARD_LIST, FUNC_CREATE_DECK_CARD_LIST_ADDR);
			placeholders.put(FUNC_GET_CARD_ID_FROM_DECK_INDEX, FUNC_GET_CARD_ID_FROM_DECK_INDEX_ADDR);
			placeholders.put(FUNC_GET_TURN_DUELIST_VAR, FUNC_GET_TURN_DUELIST_VAR_ADDR);
			placeholders.put(FUNC_LOOK_FOR_CARDS_IN_DECK, FUNC_LOOK_FOR_CARDS_IN_DECK_ADDR);
			placeholders.put(FUNC_DISPLAY_CARD_LIST, FUNC_DISPLAY_CARD_LIST_BANK1ADDR);
			placeholders.put(FUNC_SET_CARD_LIST_HEADER_TEXT, FUNC_SET_CARD_LIST_HEADER_TEXT_BANK1ADDR);
			placeholders.put(VAR_DUEL_TEMP_LIST, VAR_DUEL_TEMP_LIST_ADDR);
			placeholders.put(CONST_DECK_SIZE, CONST_DECK_SIZE_VAL);
			placeholders.put(CONST_CARD_LOCATION_DECK, CONST_CARD_LOCATION_DECK_VAL);
			placeholders.put(CONST_DUELVARS_CARD_LOCATIONS, CONST_DUELVARS_CARD_LOCATIONS_VAL);
			placeholders.put(CONST_SEARCHEFFECT_CARD_ID, CONST_SEARCHEFFECT_CARD_ID_VAL);
			
//			placeholders.put(FUNC_SEARCH_CARD_IN_DECK_AND_ADD_TO_HAND, FUNC_SEARCH_CARD_IN_DECK_AND_ADD_TO_HAND_ADDR);
//			placeholders.put(FUNC_ADD_CARD_TO_HAND, FUNC_ADD_CARD_TO_HAND_ADDR);
//			placeholders.put(FUNC_PUT_HAND_POKEMON_IN_PLAY, FUNC_PUT_HAND_POKEMON_IN_PLAY_ADDR);
//			placeholders.put(FUNC_IS_PLAYER_TURN, FUNC_IS_PLAYER_TURN_ADDR);
//			placeholders.put(FUNC_DISPLAY_CARD_DETAILS, FUNC_DISPLAY_CARD_DETAILS_BANK1ADDR);
		
			
			playerSelect.replacePlaceholderIds(placeholders);
			aiSelect.replacePlaceholderIds(placeholders);

			effect = new CardEffect(effectName + toFindBasicOf.name.toString(), (byte) 3);
			effect.addEffectCommand(EffectCommandTypes.RequireSelection, playerSelect);
			effect.addEffectCommand(EffectCommandTypes.AiSelection, aiSelect);
			effect.addEffectCommand(EffectCommandTypes.InitialEffect1, RomUtils.convertToLoadedBankOffset(INITIAL_EFFECT_ADDRESS));
			effect.addEffectCommand(EffectCommandTypes.AfterDamage, RomUtils.convertToLoadedBankOffset(PUT_IN_PLAY_AREA_EFFECT_ADDRESS));
			
			HardcodedEffects.getInstance().addCardNameUniqueEffect(effectName, toFindBasicOf.name.toString(), effect);
			return effect;
		}
	}
}
