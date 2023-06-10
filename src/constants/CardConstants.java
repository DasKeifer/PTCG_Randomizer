package constants;

import gbc_framework.utils.ByteUtils;

public class CardConstants 
{
	public enum CardId
	{
		NO_CARD                 (0x00, false), // Not a real cad - needed for spacing
		ENERGY_GRASS            (0x01),
		ENERGY_FIRE             (0x02),
		ENERGY_WATER            (0x03),
		ENERGY_LIGHTNING        (0x04),
		ENERGY_FIGHTING         (0x05),
		ENERGY_PSYCHIC          (0x06),
		ENERGY_DOUBLE_COLORLESS (0x07),
		MONSTER_001             (0x08),
		MONSTER_002             (0x09),
		MONSTER_003_1           (0x0a),
		MONSTER_003_2           (0x0b),
		MONSTER_010             (0x0c),
		MONSTER_011             (0x0d),
		MONSTER_012             (0x0e),
		MONSTER_013             (0x0f),
		MONSTER_014             (0x10),
		MONSTER_015             (0x11),
		MONSTER_023             (0x12),
		MONSTER_024             (0x13),
		MONSTER_029             (0x14),
		MONSTER_030             (0x15),
		MONSTER_031             (0x16),
		MONSTER_032             (0x17),
		MONSTER_033             (0x18),
		MONSTER_034             (0x19),
		MONSTER_041             (0x1a),
		MONSTER_042             (0x1b),
		MONSTER_043             (0x1c),
		MONSTER_044             (0x1d),
		MONSTER_045             (0x1e),
		MONSTER_046             (0x1f),
		MONSTER_047             (0x20),
		MONSTER_048             (0x21),
		MONSTER_049             (0x22),
		MONSTER_069             (0x23),
		MONSTER_070             (0x24),
		MONSTER_071             (0x25),
		MONSTER_088             (0x26),
		MONSTER_089             (0x27),
		MONSTER_102             (0x28),
		MONSTER_103             (0x29),
		MONSTER_109             (0x2a),
		MONSTER_110             (0x2b),
		MONSTER_114_1           (0x2c),
		MONSTER_114_2           (0x2d),
		MONSTER_123             (0x2e),
		MONSTER_127             (0x2f),
		MONSTER_004             (0x30),
		MONSTER_005             (0x31),
		MONSTER_006             (0x32),
		MONSTER_037             (0x33),
		MONSTER_038_1           (0x34),
		MONSTER_038_2           (0x35),
		MONSTER_058             (0x36),
		MONSTER_059_1           (0x37),
		MONSTER_059_2           (0x38),
		MONSTER_077             (0x39),
		MONSTER_078             (0x3a),
		MONSTER_126_1           (0x3b),
		MONSTER_126_2           (0x3c),
		MONSTER_136_1           (0x3d),
		MONSTER_136_2           (0x3e),
		MONSTER_146_1           (0x3f),
		MONSTER_146_2           (0x40),
		MONSTER_007             (0x41),
		MONSTER_008             (0x42),
		MONSTER_009             (0x43),
		MONSTER_054             (0x44),
		MONSTER_055             (0x45),
		MONSTER_060             (0x46),
		MONSTER_061             (0x47),
		MONSTER_062             (0x48),
		MONSTER_072             (0x49),
		MONSTER_073             (0x4a),
		MONSTER_086             (0x4b),
		MONSTER_087             (0x4c),
		MONSTER_090             (0x4d),
		MONSTER_091             (0x4e),
		MONSTER_098             (0x4f),
		MONSTER_099             (0x50),
		MONSTER_116             (0x51),
		MONSTER_117             (0x52),
		MONSTER_118             (0x53),
		MONSTER_119             (0x54),
		MONSTER_120             (0x55),
		MONSTER_121             (0x56),
		MONSTER_129             (0x57),
		MONSTER_130             (0x58),
		MONSTER_131             (0x59),
		MONSTER_134_1           (0x5a),
		MONSTER_134_2           (0x5b),
		MONSTER_138             (0x5c),
		MONSTER_139             (0x5d),
		MONSTER_144_1           (0x5e),
		MONSTER_144_2           (0x5f),
		MONSTER_025_1           (0x60),
		MONSTER_025_2           (0x61),
		MONSTER_025_3           (0x62),
		MONSTER_025_4           (0x63),
		MONSTER_025_5           (0x64),
		MONSTER_025_6           (0x65),
		MONSTER_025_7           (0x66),
		MONSTER_026_1           (0x67),
		MONSTER_026_2           (0x68),
		MONSTER_081_1           (0x69),
		MONSTER_081_2           (0x6a),
		MONSTER_082_1           (0x6b),
		MONSTER_082_2           (0x6c),
		MONSTER_100             (0x6d),
		MONSTER_101_1           (0x6e),
		MONSTER_101_2           (0x6f),
		MONSTER_125_1           (0x70),
		MONSTER_125_2           (0x71),
		MONSTER_135_1           (0x72),
		MONSTER_135_2           (0x73),
		MONSTER_145_1           (0x74),
		MONSTER_145_2           (0x75),
		MONSTER_145_3           (0x76),
		MONSTER_027             (0x77),
		MONSTER_028             (0x78),
		MONSTER_050             (0x79),
		MONSTER_051             (0x7a),
		MONSTER_056             (0x7b),
		MONSTER_057             (0x7c),
		MONSTER_066             (0x7d),
		MONSTER_067             (0x7e),
		MONSTER_068             (0x7f),
		MONSTER_074             (0x80),
		MONSTER_075             (0x81),
		MONSTER_076             (0x82),
		MONSTER_095             (0x83),
		MONSTER_104             (0x84),
		MONSTER_105_1           (0x85),
		MONSTER_105_3           (0x86),
		MONSTER_106             (0x87),
		MONSTER_107             (0x88),
		MONSTER_111             (0x89),
		MONSTER_112             (0x8a),
		MONSTER_140             (0x8b),
		MONSTER_141             (0x8c),
		MONSTER_142             (0x8d),
		MONSTER_063             (0x8e),
		MONSTER_064             (0x8f),
		MONSTER_065             (0x90),
		MONSTER_079_1           (0x91),
		MONSTER_079_2           (0x92),
		MONSTER_080             (0x93),
		MONSTER_092_1           (0x94),
		MONSTER_092_2           (0x95),
		MONSTER_093_1           (0x96),
		MONSTER_093_2           (0x97),
		MONSTER_094             (0x98),
		MONSTER_096             (0x99),
		MONSTER_097             (0x9a),
		MONSTER_122             (0x9b),
		MONSTER_124             (0x9c),
		MONSTER_150_1           (0x9d),
		MONSTER_150_2           (0x9e),
		MONSTER_150_3           (0x9f),
		MONSTER_151_1           (0xa0),
		MONSTER_151_2           (0xa1),
		MONSTER_151_3           (0xa2),
		MONSTER_016             (0xa3),
		MONSTER_017             (0xa4),
		MONSTER_018_1           (0xa5),
		MONSTER_018_2           (0xa6),
		MONSTER_019             (0xa7),
		MONSTER_020             (0xa8),
		MONSTER_021             (0xa9),
		MONSTER_022             (0xaa),
		MONSTER_035             (0xab),
		MONSTER_036             (0xac),
		MONSTER_039_1           (0xad),
		MONSTER_039_2           (0xae),
		MONSTER_039_3           (0xaf),
		MONSTER_040             (0xb0),
		MONSTER_052_1           (0xb1),
		MONSTER_052_2           (0xb2),
		MONSTER_053             (0xb3),
		MONSTER_083             (0xb4),
		MONSTER_084             (0xb5),
		MONSTER_085             (0xb6),
		MONSTER_108             (0xb7),
		MONSTER_113             (0xb8),
		MONSTER_115             (0xb9),
		MONSTER_128             (0xba),
		MONSTER_132             (0xbb),
		MONSTER_133             (0xbc),
		MONSTER_137             (0xbd),
		MONSTER_143             (0xbe),
		MONSTER_147             (0xbf),
		MONSTER_148             (0xc0),
		MONSTER_149_1           (0xc1),
		MONSTER_149_2           (0xc2),
		TRAINER_B_88           (0xc3),
		TRAINER_B_73           (0xc4),
		TRAINER_B_91           (0xc5),
		TRAINER_F_58            (0xc6),
		TRAINER_B_75           (0xc7),
		TRAINER_PROMO_1         (0xc8),
		TRAINER_B_77           (0xc9),
		TRAINER_B_76           (0xca),
		TRAINER_B_70           (0xcb),
		TRAINER_F_62            (0xcc),
		TRAINER_B_81           (0xcd),
		TRAINER_PROMO_2         (0xce),
		TRAINER_F_59            (0xcf),
		TRAINER_B_92           (0xd0),
		TRAINER_B_79           (0xd1),
		TRAINER_B_95           (0xd2),
		TRAINER_B_85           (0xd3),
		TRAINER_J_64            (0xd4),
		TRAINER_B_78           (0xd5),
		TRAINER_B_71           (0xd6),
		TRAINER_B_87           (0xd7),
		TRAINER_B_84           (0xd8),
		TRAINER_B_80           (0xd9),
		TRAINER_B_74           (0xda),
		TRAINER_B_93           (0xdb),
		TRAINER_B_72           (0xdc),
		TRAINER_B_94           (0xdd),
		TRAINER_B_90           (0xde),
		TRAINER_B_82           (0xdf),
		TRAINER_B_89           (0xe0),
		TRAINER_B_83           (0xe1),
		TRAINER_B_86           (0xe2),
		TRAINER_F_60            (0xe3),
		TRAINER_F_61            (0xe4);

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
