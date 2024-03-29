package constants;

import util.ByteUtils;

public class CardConstants 
{
	public enum CardId
	{
		// TODO later: Expunge poke names?
		NO_CARD                 (0x00, false), // Not a real cad - needed for spacing
		GRASS_ENERGY            (0x01),
		FIRE_ENERGY             (0x02),
		WATER_ENERGY            (0x03),
		LIGHTNING_ENERGY        (0x04),
		FIGHTING_ENERGY         (0x05),
		PSYCHIC_ENERGY          (0x06),
		DOUBLE_COLORLESS_ENERGY (0x07),
		BULBASAUR               (0x08),
		IVYSAUR                 (0x09),
		VENUSAUR1               (0x0a),
		VENUSAUR2               (0x0b),
		CATERPIE                (0x0c),
		METAPOD                 (0x0d),
		BUTTERFREE              (0x0e),
		WEEDLE                  (0x0f),
		KAKUNA                  (0x10),
		BEEDRILL                (0x11),
		EKANS                   (0x12),
		ARBOK                   (0x13),
		NIDORANF                (0x14),
		NIDORINA                (0x15),
		NIDOQUEEN               (0x16),
		NIDORANM                (0x17),
		NIDORINO                (0x18),
		NIDOKING                (0x19),
		ZUBAT                   (0x1a),
		GOLBAT                  (0x1b),
		ODDISH                  (0x1c),
		GLOOM                   (0x1d),
		VILEPLUME               (0x1e),
		PARAS                   (0x1f),
		PARASECT                (0x20),
		VENONAT                 (0x21),
		VENOMOTH                (0x22),
		BELLSPROUT              (0x23),
		WEEPINBELL              (0x24),
		VICTREEBEL              (0x25),
		GRIMER                  (0x26),
		MUK                     (0x27),
		EXEGGCUTE               (0x28),
		EXEGGUTOR               (0x29),
		KOFFING                 (0x2a),
		WEEZING                 (0x2b),
		TANGELA1                (0x2c),
		TANGELA2                (0x2d),
		SCYTHER                 (0x2e),
		PINSIR                  (0x2f),
		CHARMANDER              (0x30),
		CHARMELEON              (0x31),
		CHARIZARD               (0x32),
		VULPIX                  (0x33),
		NINETAILS1              (0x34),
		NINETAILS2              (0x35),
		GROWLITHE               (0x36),
		ARCANINE1               (0x37),
		ARCANINE2               (0x38),
		PONYTA                  (0x39),
		RAPIDASH                (0x3a),
		MAGMAR1                 (0x3b),
		MAGMAR2                 (0x3c),
		FLAREON1                (0x3d),
		FLAREON2                (0x3e),
		MOLTRES1                (0x3f),
		MOLTRES2                (0x40),
		SQUIRTLE                (0x41),
		WARTORTLE               (0x42),
		BLASTOISE               (0x43),
		PSYDUCK                 (0x44),
		GOLDUCK                 (0x45),
		POLIWAG                 (0x46),
		POLIWHIRL               (0x47),
		POLIWRATH               (0x48),
		TENTACOOL               (0x49),
		TENTACRUEL              (0x4a),
		SEEL                    (0x4b),
		DEWGONG                 (0x4c),
		SHELLDER                (0x4d),
		CLOYSTER                (0x4e),
		KRABBY                  (0x4f),
		KINGLER                 (0x50),
		HORSEA                  (0x51),
		SEADRA                  (0x52),
		GOLDEEN                 (0x53),
		SEAKING                 (0x54),
		STARYU                  (0x55),
		STARMIE                 (0x56),
		MAGIKARP                (0x57),
		GYARADOS                (0x58),
		LAPRAS                  (0x59),
		VAPOREON1               (0x5a),
		VAPOREON2               (0x5b),
		OMANYTE                 (0x5c),
		OMASTAR                 (0x5d),
		ARTICUNO1               (0x5e),
		ARTICUNO2               (0x5f),
		PIKACHU1                (0x60),
		PIKACHU2                (0x61),
		PIKACHU3                (0x62),
		PIKACHU4                (0x63),
		FLYING_PIKACHU          (0x64),
		SURFING_PIKACHU1        (0x65),
		SURFING_PIKACHU2        (0x66),
		RAICHU1                 (0x67),
		RAICHU2                 (0x68),
		MAGNEMITE1              (0x69),
		MAGNEMITE2              (0x6a),
		MAGNETON1               (0x6b),
		MAGNETON2               (0x6c),
		VOLTORB                 (0x6d),
		ELECTRODE1              (0x6e),
		ELECTRODE2              (0x6f),
		ELECTABUZZ1             (0x70),
		ELECTABUZZ2             (0x71),
		JOLTEON1                (0x72),
		JOLTEON2                (0x73),
		ZAPDOS1                 (0x74),
		ZAPDOS2                 (0x75),
		ZAPDOS3                 (0x76),
		SANDSHREW               (0x77),
		SANDSLASH               (0x78),
		DIGLETT                 (0x79),
		DUGTRIO                 (0x7a),
		MANKEY                  (0x7b),
		PRIMEAPE                (0x7c),
		MACHOP                  (0x7d),
		MACHOKE                 (0x7e),
		MACHAMP                 (0x7f),
		GEODUDE                 (0x80),
		GRAVELER                (0x81),
		GOLEM                   (0x82),
		ONIX                    (0x83),
		CUBONE                  (0x84),
		MAROWAK1                (0x85),
		MAROWAK2                (0x86),
		HITMONLEE               (0x87),
		HITMONCHAN              (0x88),
		RHYHORN                 (0x89),
		RHYDON                  (0x8a),
		KABUTO                  (0x8b),
		KABUTOPS                (0x8c),
		AERODACTYL              (0x8d),
		ABRA                    (0x8e),
		KADABRA                 (0x8f),
		ALAKAZAM                (0x90),
		SLOWPOKE1               (0x91),
		SLOWPOKE2               (0x92),
		SLOWBRO                 (0x93),
		GASTLY1                 (0x94),
		GASTLY2                 (0x95),
		HAUNTER1                (0x96),
		HAUNTER2                (0x97),
		GENGAR                  (0x98),
		DROWZEE                 (0x99),
		HYPNO                   (0x9a),
		MR_MIME                 (0x9b),
		JYNX                    (0x9c),
		MEWTWO1                 (0x9d),
		MEWTWO2                 (0x9e),
		MEWTWO3                 (0x9f),
		MEW1                    (0xa0),
		MEW2                    (0xa1),
		MEW3                    (0xa2),
		PIDGEY                  (0xa3),
		PIDGEOTTO               (0xa4),
		PIDGEOT1                (0xa5),
		PIDGEOT2                (0xa6),
		RATTATA                 (0xa7),
		RATICATE                (0xa8),
		SPEAROW                 (0xa9),
		FEAROW                  (0xaa),
		CLEFAIRY                (0xab),
		CLEFABLE                (0xac),
		JIGGLYPUFF1             (0xad),
		JIGGLYPUFF2             (0xae),
		JIGGLYPUFF3             (0xaf),
		WIGGLYTUFF              (0xb0),
		MEOWTH1                 (0xb1),
		MEOWTH2                 (0xb2),
		PERSIAN                 (0xb3),
		FARFETCHD               (0xb4),
		DODUO                   (0xb5),
		DODRIO                  (0xb6),
		LICKITUNG               (0xb7),
		CHANSEY                 (0xb8),
		KANGASKHAN              (0xb9),
		TAUROS                  (0xba),
		DITTO                   (0xbb),
		EEVEE                   (0xbc),
		PORYGON                 (0xbd),
		SNORLAX                 (0xbe),
		DRATINI                 (0xbf),
		DRAGONAIR               (0xc0),
		DRAGONITE1              (0xc1),
		DRAGONITE2              (0xc2),
		PROFESSOR_OAK           (0xc3),
		IMPOSTER_PROFESSOR_OAK  (0xc4),
		BILL                    (0xc5),
		MR_FUJI                 (0xc6),
		LASS                    (0xc7),
		IMAKUNI_CARD            (0xc8),
		POKEMON_TRADER          (0xc9),
		POKEMON_BREEDER         (0xca),
		CLEFAIRY_DOLL           (0xcb),
		MYSTERIOUS_FOSSIL       (0xcc),
		ENERGY_RETRIEVAL        (0xcd),
		SUPER_ENERGY_RETRIEVAL  (0xce),
		ENERGY_SEARCH           (0xcf),
		ENERGY_REMOVAL          (0xd0),
		SUPER_ENERGY_REMOVAL    (0xd1),
		SWITCH                  (0xd2),
		POKEMON_CENTER          (0xd3),
		POKE_BALL               (0xd4),
		SCOOP_UP                (0xd5),
		COMPUTER_SEARCH         (0xd6),
		POKEDEX                 (0xd7),
		PLUSPOWER               (0xd8),
		DEFENDER                (0xd9),
		ITEM_FINDER             (0xda),
		GUST_OF_WIND            (0xdb),
		DEVOLUTION_SPRAY        (0xdc),
		POTION                  (0xdd),
		SUPER_POTION            (0xde),
		FULL_HEAL               (0xdf),
		REVIVE                  (0xe0),
		MAINTENANCE             (0xe1),
		POKEMON_FLUTE           (0xe2),
		GAMBLER                 (0xe3),
		RECYCLE                 (0xe4);

		private byte value;
		static byte numCards = 0;
		
		private CardId(int inValue)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "DeckValues enum: " + inValue);
			}
			value = (byte) inValue;
			incrementNumberOfCards();
		}
		
		private CardId(int inValue, boolean isValidCard)
		{
			if (inValue > ByteUtils.MAX_BYTE_VALUE || inValue < ByteUtils.MIN_BYTE_VALUE)
			{
				throw new IllegalArgumentException("Invalid constant input for "
						+ "DeckValues enum: " + inValue);
			}
			value = (byte) inValue;
			
			if (isValidCard)
			{
				incrementNumberOfCards();
			}
		}
		
		private static void incrementNumberOfCards()
		{
			numCards++;
		}
		
		public byte getValue()
		{
			return value;
		}
		
		public int getNumberOfCards()
		{
			return numCards;
		}
		
	    public static CardId readFromByte(byte b)
	    {
	    	for(CardId num : CardId.values())
	    	{
	    		if(b == num.getValue())
	    		{
	    			return num;
	    		}
	    	}
	    	throw new IllegalArgumentException("Invalid CardId value " + b + " was passed");
	    }
	}
}
