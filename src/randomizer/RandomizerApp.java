package randomizer;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JPanel;


import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;

import randomizer.Settings.*;

import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

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
	private JTextField saveSetSeedVal;
	
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
		frmPokemonTradingCard.setBounds(100, 100, 550, 500);
		frmPokemonTradingCard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPokemonTradingCard.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel saveRomPanel = new JPanel();
		saveRomPanel.setBorder(new EmptyBorder(4, 7, 4, 7));
		frmPokemonTradingCard.getContentPane().add(saveRomPanel, BorderLayout.SOUTH);
		saveRomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 7, 0));
		
		saveLogDetailsBox = new JCheckBox("Log Randomizations");
		saveRomPanel.add(saveLogDetailsBox);
		saveLogDetailsBox.setSelected(true);
		
		saveLogSeedBox = new JCheckBox("Log Seed");
		saveRomPanel.add(saveLogSeedBox);
		saveLogSeedBox.setSelected(true);
		
		JPanel saveSetSeedPanel = new JPanel();
		saveRomPanel.add(saveSetSeedPanel);
		saveSetSeedPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel saveSetSeedLbl = new JLabel("Seed: ");
		saveSetSeedPanel.add(saveSetSeedLbl, BorderLayout.WEST);
		saveSetSeedLbl.setHorizontalAlignment(SwingConstants.TRAILING);
		
		saveSetSeedVal = new JTextField();
		saveSetSeedVal.setToolTipText("Leave blank or put \"random\" for a random seed to be chosen. If the seed is a valid int, it will be used; Otherwise it is treated as a string and hashed into an int. The seed will be changed each time the rom is saved");
		saveSetSeedPanel.add(saveSetSeedVal);
		saveSetSeedVal.setText("Random");
		saveSetSeedVal.setColumns(10);
		
		JPanel randomizeBtnPanel = new JPanel();
		saveRomPanel.add(randomizeBtnPanel);
		randomizeBtnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton randomizeButton = new JButton("Randomize!");
		randomizeBtnPanel.add(randomizeButton);
		randomizeButton.addActionListener(
				event ->
				{
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
						// TODO later: Auto-generated catch block
						e1.printStackTrace();
					}
				});
		
		JPanel openRomPanel = new JPanel();
		frmPokemonTradingCard.getContentPane().add(openRomPanel, BorderLayout.NORTH);
		
		JButton openRomButton = new JButton("Open ROM");
		openRomButton.addActionListener(
				event ->
				{
				    int returnVal = openRomChooser.showOpenDialog(frmPokemonTradingCard);
				    if (returnVal == JFileChooser.APPROVE_OPTION) {
				    	randomizer.openRom(openRomChooser.getSelectedFile());
				    }
				});
		openRomPanel.add(openRomButton);
		
		JTabbedPane movesEffectsTab = new JTabbedPane(JTabbedPane.TOP);
		frmPokemonTradingCard.getContentPane().add(movesEffectsTab, BorderLayout.CENTER);
		
		JPanel movesEffectsPanel = new JPanel();
		movesEffectsTab.addTab("Move Set", null, movesEffectsPanel, null);
		movesEffectsPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel generalRandPanel = new JPanel();
		movesEffectsPanel.add(generalRandPanel, BorderLayout.NORTH);
		generalRandPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "General", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		generalRandPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		
		generalRandKeepPokeSpecMovesBox = new JCheckBox("Keep Poke Specific Moves with Poke");
		generalRandKeepTypeSpecMovesBox = new JCheckBox("Keep Type Specific Moves in Type");
		generalRandNumMovesBox = new JCheckBox("Random Num of Moves");
		JButton generalRandNumMovesButton = new JButton("Details");
		
		JRadioButton moveRandUnchangedButton = new JRadioButton("Unchanged");
		JRadioButton moveRandShuffleButton = new JRadioButton("Shuffle");
		JRadioButton moveRandRandomButton = new JRadioButton("Random");
		JRadioButton moveRandGenerateButton = new JRadioButton("Generate");
		moveRandWithinTypeBox = new JCheckBox("Within Type");
		moveRandForceDamageBox = new JCheckBox("Force One Damaging");
		JRadioButton moveRandTypeUnchangedButton = new JRadioButton("Unchanged");
		JRadioButton moveRandTypeMatchCardTypeButton = new JRadioButton("Match Card Type");
		JRadioButton moveRandTypeAllColorlessButton = new JRadioButton("All Colorless");
		
		pokePowerIncludeWithMovesBox = new JCheckBox("Include with Attacks");
		JRadioButton pokePowerUnchangedButton = new JRadioButton("Unchanged");
		JRadioButton pokePowerShuffleButton = new JRadioButton("Shuffle");
		JRadioButton pokePowerRandomButton = new JRadioButton("Random");
		
		
		generalRandKeepPokeSpecMovesBox.setToolTipText("Some moves are specific to a specific pokemon like Call for Family. Checking this will keep those types of moves on the same pokemon. Note that if there are multiple versions of the card it, it can go with any of them");
		generalRandPanel.add(generalRandKeepPokeSpecMovesBox);
		generalRandKeepPokeSpecMovesBox.setEnabled(false);
		
		generalRandKeepTypeSpecMovesBox.setToolTipText("Some moves are Energy type specific (such as ember requiring a Fire Energy discard). This will keep it so they will only be allowed for the Energy type that matches their effect");
		generalRandKeepTypeSpecMovesBox.setEnabled(false);
		generalRandPanel.add(generalRandKeepTypeSpecMovesBox);
		
		JPanel generalRandNumMovesPanel = new JPanel();
		generalRandPanel.add(generalRandNumMovesPanel);
		generalRandNumMovesPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		generalRandNumMovesPanel.add(generalRandNumMovesBox);
		generalRandNumMovesBox.setEnabled(false);
		
		JPanel generalRandNumMovesButtonPanel = new JPanel();
		generalRandNumMovesPanel.add(generalRandNumMovesButtonPanel);
		
		generalRandNumMovesButtonPanel.add(generalRandNumMovesButton);
		generalRandNumMovesButton.setEnabled(false);
		
		JPanel specificRandPanel = new JPanel();
		movesEffectsPanel.add(specificRandPanel);
		specificRandPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel moveRandPanel = new JPanel();
		specificRandPanel.add(moveRandPanel);
		moveRandPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Attacks", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		moveRandPanel.setLayout(new GridLayout(0, 3, 0, 0));
		
		JPanel moveRandStrategyPanel = new JPanel();
		moveRandPanel.add(moveRandStrategyPanel);
		
		moveRandUnchangedButton.addItemListener(
				event ->
				{
				    if (event.getStateChange() == ItemEvent.SELECTED)
				    {
				    	generalRandNumMovesBox.setEnabled(false);
				    }
				    else if (event.getStateChange() == ItemEvent.DESELECTED && 
				    		(pokePowerIncludeWithMovesBox.isSelected() || !pokePowerUnchangedButton.isSelected()))
			    	{
				    	generalRandNumMovesBox.setEnabled(true);
			    	}
				});
		moveRandUnchangedButton.setActionCommand("UNCHANGED");
		moveRandUnchangedButton.setSelected(true);
		moveRandStrategyGoup.add(moveRandUnchangedButton);
		
		moveRandShuffleButton.setActionCommand("SHUFFLE");
		moveRandStrategyGoup.add(moveRandShuffleButton);
		moveRandStrategyPanel.setLayout(new GridLayout(0, 1, 0, 0));
		moveRandStrategyPanel.add(moveRandUnchangedButton);

		moveRandRandomButton.setActionCommand("RANDOM");
		moveRandStrategyGoup.add(moveRandRandomButton);
		moveRandStrategyPanel.add(moveRandRandomButton);
		moveRandStrategyPanel.add(moveRandShuffleButton);

		moveRandGenerateButton.setActionCommand("GENERATE");
		moveRandGenerateButton.setEnabled(false);
		moveRandStrategyPanel.add(moveRandGenerateButton);
		
		JPanel moveRandOptionsPanel = new JPanel();
		moveRandPanel.add(moveRandOptionsPanel);
		moveRandOptionsPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		moveRandOptionsPanel.add(moveRandWithinTypeBox);

		moveRandOptionsPanel.add(moveRandForceDamageBox);
		
		JPanel moveRandTypePanel = new JPanel();
		moveRandTypePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Attack Type Changes", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		moveRandPanel.add(moveRandTypePanel);
		moveRandTypePanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		moveRandTypeUnchangedButton.setActionCommand("UNCHANGED");
		moveRandTypeUnchangedButton.setSelected(true);
		moveRandTypeGroup.add(moveRandTypeUnchangedButton);
		moveRandTypePanel.add(moveRandTypeUnchangedButton);

		moveRandTypeMatchCardTypeButton.setActionCommand("MATCH_CARD_TYPE");
		moveRandTypeGroup.add(moveRandTypeMatchCardTypeButton);
		moveRandTypePanel.add(moveRandTypeMatchCardTypeButton);
		
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

		pokePowerUnchangedButton.addItemListener(
				event ->
				{
					// Only need to change if this is separate from attack randomization
					if (!pokePowerIncludeWithMovesBox.isSelected())
					{
						// In the future we may want to allow partial moveset randomization
					    if (event.getStateChange() == ItemEvent.SELECTED)
					    {
					    	generalRandNumMovesBox.setEnabled(false);
					    }
					    // If both this and the attacks are not unchanged, enable move number randomization
					    else if (event.getStateChange() == ItemEvent.DESELECTED && !moveRandUnchangedButton.isSelected())
				    	{
					    	generalRandNumMovesBox.setEnabled(true);
				    	}
					}
				});
		pokePowerUnchangedButton.setSelected(true);
		pokePowerUnchangedButton.setEnabled(false);
		pokePowerUnchangedButton.setActionCommand("UNCHANGED");
		pokePowersStrategyGroup.add(pokePowerUnchangedButton);

		pokePowerShuffleButton.setEnabled(false);
		pokePowerShuffleButton.setActionCommand("SHUFFLE");
		pokePowersStrategyGroup.add(pokePowerShuffleButton);

		pokePowerRandomButton.setEnabled(false);
		pokePowerRandomButton.setActionCommand("RANDOM");
		pokePowersStrategyGroup.add(pokePowerRandomButton);
		
		JPanel pokePowerOptionsPanel = new JPanel();
		pokePowerPanel.add(pokePowerOptionsPanel);
		pokePowerOptionsPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		powerWithinTypeBox = new JCheckBox("Within Type");
		powerWithinTypeBox.setEnabled(false);
		pokePowerOptionsPanel.add(powerWithinTypeBox);
		
		pokePowerIncludeWithMovesBox.addItemListener(
				event ->
				{
				    if (event.getStateChange() == ItemEvent.SELECTED)
				    {
				    		pokePowerUnchangedButton.setEnabled(false);
				    		pokePowerShuffleButton.setEnabled(false);
				    		pokePowerRandomButton.setEnabled(false);
				    		powerWithinTypeBox.setEnabled(false);

				    		// Set it based only on the attacks
				    		generalRandNumMovesBox.setEnabled(!moveRandUnchangedButton.isSelected());
				    		
				    }
				    else if (event.getStateChange() == ItemEvent.DESELECTED)
			    	{
			    		pokePowerUnchangedButton.setEnabled(true);
			    		pokePowerShuffleButton.setEnabled(true);
			    		pokePowerRandomButton.setEnabled(true);
			    		powerWithinTypeBox.setEnabled(true);

			    		// Set it based off of if both the attacks and the poke powers are not unchanged
			    		generalRandNumMovesBox.setEnabled(
			    				!moveRandUnchangedButton.isSelected() && !pokePowerUnchangedButton.isSelected());
			    	}
				});
		pokePowerIncludeWithMovesBox.setSelected(true);
		pokePowerStrategyPanel.add(pokePowerIncludeWithMovesBox);
		pokePowerStrategyPanel.add(pokePowerUnchangedButton);
		pokePowerStrategyPanel.add(pokePowerRandomButton);
		pokePowerStrategyPanel.add(pokePowerShuffleButton);
		
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

	private Settings createSettingsFromState() 
	{
	        Settings settings = new Settings();
	        SpecificDataPerType typeData = new SpecificDataPerType();
	        AttacksData attacksData = new AttacksData();
	        PokePowersData powersData = new PokePowersData();
	        
	        settings.setSeed(saveSetSeedVal.getText());
	        settings.setLogSeed(saveLogSeedBox.isSelected());
	        settings.setLogDetails(saveLogDetailsBox.isSelected());
	        
	        settings.setTypeSpecificData(typeData);
	        settings.setAttacks(attacksData);
	        settings.setPokePowers(powersData);
	        settings.setMovesMatchPokeSpecific(generalRandKeepPokeSpecMovesBox.isSelected());
	        settings.setMovesMatchTypeSpecific(generalRandKeepTypeSpecMovesBox.isSelected());
	        settings.setMovesRandomNumberOfAttacks(generalRandNumMovesBox.isSelected());
	        
	        settings.setApplyMultibankEffectsTweak(true); //TODO - add a setting for this? Any reason why this
	        												// wouldn't be allowed? Probably not
	        
	        attacksData.setRandomizationWithinType(moveRandWithinTypeBox.isSelected());
	        attacksData.setRandomizationStrat(moveRandStrategyGoup.getSelection().getActionCommand());
	        attacksData.setForceOneDamagingAttack(moveRandForceDamageBox.isSelected());
	        attacksData.setMoveTypeChanges(moveRandTypeGroup.getSelection().getActionCommand());

	        powersData.setRandomizationWithinType(powerWithinTypeBox.isSelected());
	        powersData.setRandomizationStrat(pokePowersStrategyGroup.getSelection().getActionCommand());
	        powersData.setIncludeWithMoves(pokePowerIncludeWithMovesBox.isSelected());
	        
	        return settings;
	 }
}
