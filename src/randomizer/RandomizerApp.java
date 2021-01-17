package randomizer;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JPanel;


import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.awt.event.ActionEvent;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.ButtonGroup;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;

import randomizer.Settings.*;

import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class RandomizerApp {

	private JFrame frmPokemonTradingCard;
    private JFileChooser openRomChooser;
    private JFileChooser saveRomChooser;

	private Randomizer randomizer;
	private final ButtonGroup moveRandStrategyGoup = new ButtonGroup();
	private final ButtonGroup pokePowersStrategyGroup = new ButtonGroup();
	private JCheckBox saveLogSeedBox;
	private JCheckBox saveLogDetailsBox;
	private JCheckBox moveRandWithinTypeBox;
	private JCheckBox moveRandForceDamageBox;
	private JCheckBox generalRandNumMovesBox;
	private JCheckBox generalRandKeepPokeSpecMovesBox;
	private JCheckBox generalRandKeepTypeSpecMovesBox;
	private JCheckBox powerWithinTypeBox;
	private JCheckBox pokePowerIncludeWithMovesBox;
	private final ButtonGroup moveRandTypeGroup = new ButtonGroup();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RandomizerApp window = new RandomizerApp();
					window.frmPokemonTradingCard.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public RandomizerApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		randomizer = new Randomizer();
		
		openRomChooser = new JFileChooser();
		openRomChooser.setCurrentDirectory(new File(".")); // Jar location by default
	    openRomChooser.setSelectedFile(null);
		
		saveRomChooser = new JFileChooser();
		saveRomChooser.setCurrentDirectory(new File(".")); // Jar location by default
		saveRomChooser.setSelectedFile(null);
		
		frmPokemonTradingCard = new JFrame();
		frmPokemonTradingCard.setTitle("Pokemon Trading Card Game Randomizer");
		frmPokemonTradingCard.setBounds(100, 100, 500, 500);
		frmPokemonTradingCard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPokemonTradingCard.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel saveRomPanel = new JPanel();
		saveRomPanel.setBorder(new EmptyBorder(4, 7, 4, 7));
		frmPokemonTradingCard.getContentPane().add(saveRomPanel, BorderLayout.SOUTH);
		saveRomPanel.setLayout(new GridLayout(0, 3, 0, 0));
		
		saveLogSeedBox = new JCheckBox("Log Seed");
		saveRomPanel.add(saveLogSeedBox);
		saveLogSeedBox.setSelected(true);
		
		saveLogDetailsBox = new JCheckBox("Log Randomizations");
		saveRomPanel.add(saveLogDetailsBox);
		saveLogDetailsBox.setSelected(true);
		
		JPanel randomizeBtnPanel = new JPanel();
		saveRomPanel.add(randomizeBtnPanel);
		randomizeBtnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton randomizeButton = new JButton("Randomize!");
		randomizeBtnPanel.add(randomizeButton);
		randomizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
				    int returnVal = saveRomChooser.showSaveDialog(frmPokemonTradingCard);
				    if (returnVal == JFileChooser.APPROVE_OPTION) 
				    {
				    	Settings settings = createSettingsFromState();
						
						File saveFile = saveRomChooser.getSelectedFile();
				        if (!saveFile.getName().endsWith(randomizer.getFileExtension()))
				        {
				        	saveFile = new File(saveFile.getPath().concat(randomizer.getFileExtension()));
				        }
				    	randomizer.randomizeAndSaveRom(saveFile, settings);
				    }
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		JPanel openRomPanel = new JPanel();
		frmPokemonTradingCard.getContentPane().add(openRomPanel, BorderLayout.NORTH);
		
		JButton openRomButton = new JButton("Open ROM");
		openRomButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			    int returnVal = openRomChooser.showOpenDialog(frmPokemonTradingCard);
			    if (returnVal == JFileChooser.APPROVE_OPTION) {
			    	randomizer.openRom(openRomChooser.getSelectedFile());
			    }
			}
		});
		openRomPanel.add(openRomButton);
		
		JTabbedPane movesEffectsTab = new JTabbedPane(JTabbedPane.TOP);
		frmPokemonTradingCard.getContentPane().add(movesEffectsTab, BorderLayout.CENTER);
		
		JPanel movesEffectsPanel = new JPanel();
		movesEffectsTab.addTab("Moves/Effects", null, movesEffectsPanel, null);
		movesEffectsPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel generalRandPanel = new JPanel();
		movesEffectsPanel.add(generalRandPanel, BorderLayout.NORTH);
		generalRandPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "General", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		generalRandPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		generalRandKeepPokeSpecMovesBox = new JCheckBox("Keep Poke Specific Moves/Powers with Poke");
		generalRandKeepPokeSpecMovesBox.setToolTipText("Some moves are specific to a specific pokemon like Call for Family. Checking this will keep those types of moves on the same pokemon. Note that if there are multiple versions of the card it, it can go with any of them");
		generalRandPanel.add(generalRandKeepPokeSpecMovesBox);
		generalRandKeepPokeSpecMovesBox.setEnabled(false);
		
		generalRandKeepTypeSpecMovesBox = new JCheckBox("Keep Type Specific Moves/Powers in Type");
		generalRandKeepTypeSpecMovesBox.setToolTipText("Some moves are Energy type specific (such as ember requiring a Fire Energy discard). This will keep it so they will only be allowed for the Energy type that matches their effect");
		generalRandKeepTypeSpecMovesBox.setEnabled(false);
		generalRandPanel.add(generalRandKeepTypeSpecMovesBox);
		
		JPanel generalRandNumMovesPanel = new JPanel();
		generalRandPanel.add(generalRandNumMovesPanel);
		generalRandNumMovesPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		generalRandNumMovesBox = new JCheckBox("Modify Num of Moves/Powers");
		generalRandNumMovesPanel.add(generalRandNumMovesBox);
		generalRandNumMovesBox.setEnabled(false);
		
		JPanel generalRandNumMovesButtonPanel = new JPanel();
		generalRandNumMovesPanel.add(generalRandNumMovesButtonPanel);
		
		JButton generalRandNumMovesButton = new JButton("Details");
		generalRandNumMovesButtonPanel.add(generalRandNumMovesButton);
		generalRandNumMovesButton.setEnabled(false);
		
		JPanel specificRandPanel = new JPanel();
		movesEffectsPanel.add(specificRandPanel);
		specificRandPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel moveRandPanel = new JPanel();
		specificRandPanel.add(moveRandPanel);
		moveRandPanel.setBorder(new TitledBorder(null, "Pokemon Moves", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		moveRandPanel.setLayout(new GridLayout(0, 3, 0, 0));
		
		JPanel moveRandStrategyPanel = new JPanel();
		moveRandPanel.add(moveRandStrategyPanel);
		
		JRadioButton moveRandUnchangedButton = new JRadioButton("Unchanged");
		moveRandUnchangedButton.setActionCommand("UNCHANGED");
		moveRandUnchangedButton.setSelected(true);
		moveRandStrategyGoup.add(moveRandUnchangedButton);
		
		JRadioButton moveRandRandomButton = new JRadioButton("Random");
		moveRandRandomButton.setActionCommand("RANDOM");
		moveRandStrategyGoup.add(moveRandRandomButton);
		
		JRadioButton moveRandShuffleButton = new JRadioButton("Shuffle");
		moveRandShuffleButton.setActionCommand("SHUFFLE");
		moveRandStrategyGoup.add(moveRandShuffleButton);
		moveRandStrategyPanel.setLayout(new GridLayout(0, 1, 0, 0));
		moveRandStrategyPanel.add(moveRandUnchangedButton);
		moveRandStrategyPanel.add(moveRandShuffleButton);
		moveRandStrategyPanel.add(moveRandRandomButton);
		
		JRadioButton moveRandGenerateButton = new JRadioButton("Generate");
		moveRandGenerateButton.setActionCommand("GENERATE");
		moveRandGenerateButton.setEnabled(false);
		moveRandStrategyPanel.add(moveRandGenerateButton);
		
		JPanel moveRandOptionsPanel = new JPanel();
		moveRandPanel.add(moveRandOptionsPanel);
		moveRandOptionsPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		moveRandWithinTypeBox = new JCheckBox("Within Type");
		moveRandOptionsPanel.add(moveRandWithinTypeBox);
		
		moveRandForceDamageBox = new JCheckBox("Force One Damaging");
		moveRandOptionsPanel.add(moveRandForceDamageBox);
		moveRandForceDamageBox.setEnabled(false);
		
		JPanel moveRandTypePanel = new JPanel();
		moveRandTypePanel.setBorder(new TitledBorder(null, "Move Type Changes", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		moveRandPanel.add(moveRandTypePanel);
		moveRandTypePanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JRadioButton moveRandTypeUnchangedButton = new JRadioButton("Unchanged");
		moveRandTypeUnchangedButton.setActionCommand("UNCHANGED");
		moveRandTypeUnchangedButton.setSelected(true);
		moveRandTypeGroup.add(moveRandTypeUnchangedButton);
		moveRandTypePanel.add(moveRandTypeUnchangedButton);
		
		JRadioButton moveRandTypeMatchCardTypeButton = new JRadioButton("Match Card Type");
		moveRandTypeMatchCardTypeButton.setActionCommand("MATCH_CARD_TYPE");
		moveRandTypeGroup.add(moveRandTypeMatchCardTypeButton);
		moveRandTypePanel.add(moveRandTypeMatchCardTypeButton);
		
		JRadioButton moveRandTypeAllColorlessButton = new JRadioButton("All Colorless");
		moveRandTypeAllColorlessButton.setActionCommand("ALL_COLORLESS");
		moveRandTypeGroup.add(moveRandTypeAllColorlessButton);
		moveRandTypePanel.add(moveRandTypeAllColorlessButton);
		
		JPanel pokePowerPanel = new JPanel();
		specificRandPanel.add(pokePowerPanel);
		pokePowerPanel.setBorder(new TitledBorder(null, "Poke Powers", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pokePowerPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel pokePowerStrategyPanel = new JPanel();
		pokePowerPanel.add(pokePowerStrategyPanel);
		pokePowerStrategyPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		pokePowerIncludeWithMovesBox = new JCheckBox("Include with Moves");
		pokePowerIncludeWithMovesBox.setSelected(true);
		pokePowerStrategyPanel.add(pokePowerIncludeWithMovesBox);
		
		JRadioButton pokePowerUnchangedButton = new JRadioButton("Unchanged");
		pokePowerUnchangedButton.setEnabled(false);
		pokePowerUnchangedButton.setSelected(true);
		pokePowerUnchangedButton.setActionCommand("UNCHANGED");
		pokePowersStrategyGroup.add(pokePowerUnchangedButton);
		pokePowerStrategyPanel.add(pokePowerUnchangedButton);
		
		JRadioButton pokePowerShuffleButton = new JRadioButton("Shuffle");
		pokePowerShuffleButton.setEnabled(false);
		pokePowerShuffleButton.setActionCommand("SHUFFLE");
		pokePowerStrategyPanel.add(pokePowerShuffleButton);
		pokePowersStrategyGroup.add(pokePowerShuffleButton);
		
		JRadioButton pokePowerRandomButton = new JRadioButton("Random");
		pokePowerRandomButton.setEnabled(false);
		pokePowerRandomButton.setActionCommand("RANDOM");
		pokePowerStrategyPanel.add(pokePowerRandomButton);
		pokePowersStrategyGroup.add(pokePowerRandomButton);
		
		JPanel pokePowerOptionsPanel = new JPanel();
		pokePowerPanel.add(pokePowerOptionsPanel);
		pokePowerOptionsPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		powerWithinTypeBox = new JCheckBox("Within Type");
		powerWithinTypeBox.setEnabled(false);
		pokePowerOptionsPanel.add(powerWithinTypeBox);
		
		JPanel typesPanel = new JPanel();
		movesEffectsTab.addTab("Types", null, typesPanel, null);
		typesPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel moveTypesPanel = new JPanel();
		typesPanel.add(moveTypesPanel);
		moveTypesPanel.setBorder(new TitledBorder(null, "Move Types", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		moveTypesPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel moveTypesStrategyPanel = new JPanel();
		moveTypesPanel.add(moveTypesStrategyPanel);
		moveTypesStrategyPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JRadioButton moveTypesUnchangedButton = new JRadioButton("Unchanged");
		moveTypesStrategyPanel.add(moveTypesUnchangedButton);
		moveTypesUnchangedButton.setSelected(true);
		
		JRadioButton moveTypesMatchPokeButton = new JRadioButton("Change to Poke Type");
		moveTypesStrategyPanel.add(moveTypesMatchPokeButton);
		moveTypesMatchPokeButton.setEnabled(false);
		
		JRadioButton moveTypesAllColorless = new JRadioButton("All Colorless");
		moveTypesStrategyPanel.add(moveTypesAllColorless);
		moveTypesAllColorless.setEnabled(false);
		
		JPanel moveTypesRandomPanel = new JPanel();
		moveTypesRandomPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		moveTypesPanel.add(moveTypesRandomPanel);
		moveTypesRandomPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel moveTypesRandomButtonPanel = new JPanel();
		FlowLayout fl_moveTypesRandomButtonPanel = (FlowLayout) moveTypesRandomButtonPanel.getLayout();
		fl_moveTypesRandomButtonPanel.setAlignment(FlowLayout.LEFT);
		moveTypesRandomPanel.add(moveTypesRandomButtonPanel);
		
		JRadioButton moveTypesRandomButton = new JRadioButton("Random");
		moveTypesRandomButtonPanel.add(moveTypesRandomButton);
		moveTypesRandomButton.setEnabled(false);
		
		JPanel moveTypesRandomOptionsPanel = new JPanel();
		moveTypesRandomPanel.add(moveTypesRandomOptionsPanel);
		moveTypesRandomOptionsPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JRadioButton moveTypeRandMoveButton = new JRadioButton("Per Move");
		moveTypeRandMoveButton.setSelected(true);
		moveTypeRandMoveButton.setEnabled(false);
		moveTypesRandomOptionsPanel.add(moveTypeRandMoveButton);
		
		JRadioButton moveTypeRandCardButton = new JRadioButton("Per Card");
		moveTypeRandCardButton.setEnabled(false);
		moveTypesRandomOptionsPanel.add(moveTypeRandCardButton);
		
		JRadioButton moveTypeRandPokeButton = new JRadioButton("Per Pokemon");
		moveTypeRandPokeButton.setEnabled(false);
		moveTypesRandomOptionsPanel.add(moveTypeRandPokeButton);
		
		JRadioButton moveTypeRandLineButton = new JRadioButton("Per Evo Line");
		moveTypeRandLineButton.setEnabled(false);
		moveTypesRandomOptionsPanel.add(moveTypeRandLineButton);
		
		JCheckBox moveTypeRandPreventWrongTypeBox = new JCheckBox("Prevent Wrong Type Specfic");
		moveTypeRandPreventWrongTypeBox.setSelected(true);
		moveTypeRandPreventWrongTypeBox.setEnabled(false);
		moveTypesRandomOptionsPanel.add(moveTypeRandPreventWrongTypeBox);
	}

	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
	
	 private Settings createSettingsFromState() 
	 {
	        Settings settings = new Settings();
	        SpecificDataPerType typeData = new SpecificDataPerType();
	        MovesData movesData = new MovesData();
	        PokePowersData powersData = new PokePowersData();
	        
	        settings.setLogSeed(saveLogSeedBox.isSelected());
	        settings.setLogDetails(saveLogDetailsBox.isSelected());
	        
	        settings.setTypeSpecificData(typeData);
	        settings.setMoves(movesData);
	        settings.setPokePowers(powersData);
	        settings.setMovesMatchPokeSpecific(generalRandKeepPokeSpecMovesBox.isSelected());
	        settings.setMovesMatchTypeSpecific(generalRandKeepTypeSpecMovesBox.isSelected());
	        settings.setMovesRandomNumberOfAttacks(generalRandNumMovesBox.isSelected());
	        
	        movesData.setMovesAttacksWithinType(moveRandWithinTypeBox.isSelected());
	        movesData.setMovesForceOneDamaging(moveRandForceDamageBox.isSelected());
	        movesData.setMovesStrat(moveRandStrategyGoup.getSelection().getActionCommand());
	        movesData.setMoveTypeChanges(moveRandTypeGroup.getSelection().getActionCommand());

	        powersData.setIncludeWithMoves(pokePowerIncludeWithMovesBox.isSelected());
	        if (!pokePowerIncludeWithMovesBox.isSelected())
	        {
		        powersData.setMovesPowersWithinType(powerWithinTypeBox.isSelected());
		        powersData.setMovesPokePowerStrat(pokePowersStrategyGroup.getSelection().getActionCommand());
	        }
	        
	        return settings;
	 }
}
