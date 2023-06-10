package data.custom_card_effects;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.CharMapConstants;
import constants.DuelConstants.EffectFunctionTypes;
import data.Card;
import data.CardGroup;
import gbc_framework.utils.RomUtils;

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
	private Map<String, Map<String, CustomCardEffect>> cardNameUniqueEffects;
	// Effect name, energy type, effect
//	private Map<String, Map<EnergyType, CustomCardEffect>> energyTypeUniqueEffects;
	// Effect name, effect
//	private Map<String, CustomCardEffect> globallyUniqueEffects;
	
	private HardcodedEffects()
	{
		cardNameUniqueEffects = new HashMap<>();
//		energyTypeUniqueEffects = new HashMap<>();
//		globallyUniqueEffects = new HashMap<>();
	}
	
	public static void reset()
	{
		singleton = new HardcodedEffects();
	}
	
	public static HardcodedEffects getInstance() 
	{
		return singleton;
	}
	
	public CustomCardEffect tryGetCardNameUniqueEffect(String effectName, String cardName)
	{
		Map<String, CustomCardEffect> effectMap = cardNameUniqueEffects.get(effectName);
		if (effectMap == null)
		{
			return null;
		}
		
		return effectMap.get(cardName);
	}
	
	public void addCardNameUniqueEffect(String effectName, String cardName, CustomCardEffect effect)
	{
		Map<String, CustomCardEffect> effectMap = cardNameUniqueEffects.computeIfAbsent(effectName, k -> new HashMap<>());	
		effectMap.put(cardName, effect);
	}
	
	// TODO later: Make so this can be read in from a file. Have a Constants file for all these static things
	// then have more files that define the datablocks - i.e. their preferences/required locations and
	// the code itself
	
	// TODO later: Have a move effect class that contains a uniqueness aspect of globally, type, name, etc.
	// In that class have an effect command and effect pointers
	//
	// Have a higher level move effect tracker class that will pull out all the rom's move effects based on
	// card and move name. Add in/replace the custom ones with that. Then if we allow file based tweaking/specifying
	// of moves, we can refer to the ones already in the base rom
	// Then when we save, we can check the move name against our custom set of moves and write the data if needed
	// or get a reference to the existing data
	
	// Things might get trickier if move names are reused with different effects (i.e. discard 1 vs discard 2)
	


	
	
	
//	public static String CARD_NAME_PLACEHOLDER = CompilerUtils.createPlaceholder("cardname");
//	public static String CARD_ID_PLACEHOLDER = CompilerUtils.createPlaceholder("cardid");

//	public static String FUNC_CREATE_DECK_CARD_LIST = CompilerUtils.createPlaceholder("CreateDeckCardList");
	public static final String FUNC_CREATE_DECK_CARD_LIST_ADDR = "$11df";

//	public static String FUNC_GET_CARD_ID_FROM_DECK_INDEX = CompilerUtils.createPlaceholder("GetCardIDFromDeckIndex");
	public static final String FUNC_GET_CARD_ID_FROM_DECK_INDEX_ADDR = "$1324";
	
//	public static String FUNC_GET_TURN_DUELIST_VAR = CompilerUtils.createPlaceholder("GetTurnDuelistVariable");
	public static final String FUNC_GET_TURN_DUELIST_VAR_ADDR = "$160b";
	
//	public static String FUNC_LOOK_FOR_CARDS_IN_DECK = CompilerUtils.createPlaceholder("LookForCardsInDeck");
	public static final String FUNC_LOOK_FOR_CARDS_IN_DECK_ADDR = "$2c2ec";
	
//	public static String FUNC_DISPLAY_CARD_LIST = CompilerUtils.createPlaceholder("DisplayCardList");
	public static final String FUNC_DISPLAY_CARD_LIST_BANK1ADDR = "$55f0";
	
//	public static String FUNC_SET_CARD_LIST_HEADER_TEXT = CompilerUtils.createPlaceholder("SetCardListHeaderText");
	public static final String FUNC_SET_CARD_LIST_HEADER_TEXT_BANK1ADDR = "$5580";
	
	
//	public static String VAR_DUEL_TEMP_LIST = CompilerUtils.createPlaceholder("wDuelTempList");
	public static final String VAR_DUEL_TEMP_LIST_ADDR = "$c510";
	
//	public static String CONST_DECK_SIZE = CompilerUtils.createPlaceholder("DECK_SIZE");
	public static final String CONST_DECK_SIZE_VAL = "$3C"; // 60 in hex
	
//	public static String CONST_CARD_LOCATION_DECK = CompilerUtils.createPlaceholder("CARD_LOCATION_DECK");
	public static final String CONST_CARD_LOCATION_DECK_VAL = "$0";

//	public static String CONST_DUELVARS_CARD_LOCATIONS = CompilerUtils.createPlaceholder("DUELVARS_CARD_LOCATIONS");
	public static final String CONST_DUELVARS_CARD_LOCATIONS_VAL = "$0";
	
//	public static String CONST_SEARCHEFFECT_CARD_ID = CompilerUtils.createPlaceholder("SEARCHEFFECT_CARD_ID");
	public static final String CONST_SEARCHEFFECT_CARD_ID_VAL = "$0";
	
//	public static final String FUNC_SEARCH_CARD_IN_DECK_AND_ADD_TO_HAND = CompilerUtils.createPlaceholder("SearchCardInDeckAndAddToHand");
	public static final String FUNC_SEARCH_CARD_IN_DECK_AND_ADD_TO_HAND_ADDR = "$10fc";

//	public static final String FUNC_ADD_CARD_TO_HAND = CompilerUtils.createPlaceholder("AddCardToHand");
	public static final String FUNC_ADD_CARD_TO_HAND_ADDR = "$1123";

//	public static final String FUNC_PUT_HAND_MONSTER_IN_PLAY_ADDR = CompilerUtils.createPlaceholder("PutHandMonsterCardInPlayArea");
	public static final String FUNC_PUT_HAND_MONSTER_IN_PLAY_ADDR = "$1485";

//	public static final String FUNC_IS_PLAYER_TURN = CompilerUtils.createPlaceholder("IsPlayerTurn");
	public static final String FUNC_IS_PLAYER_TURN_ADDR = "$2c0c7";

//	public static final String FUNC_DISPLAY_CARD_DETAILS = CompilerUtils.createPlaceholder("DisplayCardDetailScreen");
	public static final String FUNC_DISPLAY_CARD_DETAILS_BANK1ADDR = "$4b31";
	
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
	
	// TODO later: Handle cards with multiple IDs (e.g. 4 different pikachus)
	// TODO later: Handle multiple cards (e.g. nidoran male or female)
	public static class CallForFamily
	{		
		private CallForFamily() {}
		
		static final String EFFECT_NAME = "CallForFamily";
		
		// TODO later: Read from rom?
		static final int INITIAL_EFFECT_ADDRESS = 0x2cc40; // Bellsprout's but they are all the same even for nidoran
		static final int PUT_IN_PLAY_AREA_EFFECT_ADDRESS = 0x2ccc2; // Bellsprout's but they are all the same even for nidoran

		// Basics may not always be monster cards - take the fossil trainer for example
		public static CustomCardEffect createMoveEffect(/*Cards<Card> cards,*/ CardGroup<Card> basics) 
		{
			// Just assume the first one for now. This probably won't work well for cards where there are versions of them
			// TODO later: can flying 25 evolve into 26? Looks like no since it is comparing
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
			Card toFindBasicOf = basics.listOrderedByCardId().get(0);
			
			CustomCardEffect effect = HardcodedEffects.getInstance().tryGetCardNameUniqueEffect(EFFECT_NAME, toFindBasicOf.name.toString());
			if (effect != null)
			{
				return effect;
			}
			
			String cardName = toFindBasicOf.name.toString();
			String cardId = String.format("$%x", toFindBasicOf.id.getValue());	

			List<String> playerSelectCode = Arrays.asList(
					"CallForFamilyPS" + cardName + ":",
						"ld a, $ff",
						"ldh [$ffa0], a",
						"call " + FUNC_CREATE_DECK_CARD_LIST_ADDR,
						"ldtx hl, halfTextBox:Choose a " + cardName + " from the Deck.",
						"ldtx bc, cardName:" + cardName,
						"lb de, " + CONST_SEARCHEFFECT_CARD_ID_VAL + ", " + cardId,
						"call " + FUNC_LOOK_FOR_CARDS_IN_DECK_ADDR,
						"ret c",

					// draw Deck list interface and print text
						"bank1call $5591",
						"ldtx hl, halfTextBox:Choose a " + cardName + ".",
						"ldtx de, textbox:" + CharMapConstants.RAMNAME + "'s Deck", 
						"bank1call " + FUNC_SET_CARD_LIST_HEADER_TEXT_BANK1ADDR,

					".loop",
						"bank1call " + FUNC_DISPLAY_CARD_LIST_BANK1ADDR,
						"jr c, .pressed_b",
						"call " + FUNC_GET_CARD_ID_FROM_DECK_INDEX_ADDR,
						"ld bc, " + cardId,
						"call $3090", // compare DE to BC
						"jr nz, .play_sfx",

					// Monster was selected
						"ldh a, [$ff98]", 
						"ldh [$ffa0], a",
						"or a",
						"ret",

					".play_sfx",
						// play SFX and loop back
						"call $3794", // bank 0
						"jr .loop",

					".pressed_b",
					// figure if Player can exit the screen without selecting,
					// that is, if the Deck has no Bellsprout card.
						"ld a, " + CONST_DUELVARS_CARD_LOCATIONS_VAL,
						"call " + FUNC_GET_TURN_DUELIST_VAR_ADDR,
					".loop_b_press",
						"ld a, [hl]",    								
						"cp " + CONST_CARD_LOCATION_DECK_VAL,
						"jr nz, .next",
						"ld a, l",
						"call " + FUNC_GET_CARD_ID_FROM_DECK_INDEX_ADDR,
						"ld bc, " + cardId,
						"call $3090", // compare DE to BC
						"jr z, .play_sfx", // found Bellsprout, go back to top loop
					".next",
						"inc l",
						"ld a, l",
						"cp " + CONST_DECK_SIZE_VAL,
						"jr c, .loop_b_press",

					// no Bellsprout in Deck, can safely exit screen
						"ld a, $ff",
						"ldh [$ffa0], a",
						"or a",
						"ret"
			);
				
			List<String> aiSelectCode = Arrays.asList(
					"CallForFamilyAIS" + cardName + ":",
						"call " + FUNC_CREATE_DECK_CARD_LIST_ADDR,
						"ld hl, " + VAR_DUEL_TEMP_LIST_ADDR,
					".loop_deck",
						"ld a, [hli]",
						"ldh [$ffa0], a",
						"cp $ff",
						"ret z", // no Bellsprout
						"call " + FUNC_GET_CARD_ID_FROM_DECK_INDEX_ADDR,
						"ld a, e",
						"cp " + cardId,
						"jr nz, .loop_deck",
						"ret" // Bellsprout found
			);

			effect = new CustomCardEffect(EFFECT_NAME + toFindBasicOf.name.toString());
			effect.addEffectFunction(EffectFunctionTypes.INITIAL_EFFECT_1, RomUtils.convertToLoadedBankOffset(INITIAL_EFFECT_ADDRESS));
			effect.addEffectFunction(EffectFunctionTypes.AFTER_DAMAGE, RomUtils.convertToLoadedBankOffset(PUT_IN_PLAY_AREA_EFFECT_ADDRESS));
			effect.addEffectFunction(EffectFunctionTypes.REQUIRE_SELECTION, playerSelectCode);
			effect.addEffectFunction(EffectFunctionTypes.AI_SELECTION, aiSelectCode);
			
			HardcodedEffects.getInstance().addCardNameUniqueEffect(EFFECT_NAME, toFindBasicOf.name.toString(), effect);
			return effect;
		}
	}
}
