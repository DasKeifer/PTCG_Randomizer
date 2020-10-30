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

public class RandomizerApp {

	private JFrame frmPokemonTradingCard;
    private JFileChooser openRomChooser;
    private JFileChooser saveRomChooser;

	private Randomizer randomizer;
	private final ButtonGroup moveRandStrategyGoup = new ButtonGroup();
	private final ButtonGroup pokePowersStrategyGroup = new ButtonGroup();
	private JCheckBox moveRandWithinTypeBox;
	private JCheckBox moveRandForceDamageBox;
	private JCheckBox moveRandNumMovesBox;
	private JCheckBox generalRandExclPokeSpecMovesBox;
	private JCheckBox generalRandExclTypeSpecMovesBox;
	private JCheckBox powerWithinTypeBox;
	private JCheckBox powerRandNumPowersBox;
	
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
		frmPokemonTradingCard.getContentPane().add(saveRomPanel, BorderLayout.SOUTH);
		
		JButton randomizeButton = new JButton("Randomize!");
		randomizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
				    int returnVal = saveRomChooser.showSaveDialog(frmPokemonTradingCard);
				    if (returnVal == JFileChooser.APPROVE_OPTION) 
				    {
				    	Settings settings = createSettingsFromState();
						randomizer.randomize(settings);
						
						File saveFile = saveRomChooser.getSelectedFile();
				        if (!saveFile.getName().endsWith(randomizer.getFileExtension()))
				        {
				        	saveFile = new File(saveFile.getPath().concat(randomizer.getFileExtension()));
				        }
				    	randomizer.saveRom(saveFile);
				    }
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		saveRomPanel.add(randomizeButton);
		
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
		generalRandPanel.setBorder(new TitledBorder(null, "Pokemon Moves", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		generalRandPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		generalRandExclPokeSpecMovesBox = new JCheckBox("Exclude Poke Specific");
		generalRandPanel.add(generalRandExclPokeSpecMovesBox);
		generalRandExclPokeSpecMovesBox.setEnabled(false);
		
		generalRandExclTypeSpecMovesBox = new JCheckBox("Exclude Type Specific");
		generalRandExclTypeSpecMovesBox.setEnabled(false);
		generalRandPanel.add(generalRandExclTypeSpecMovesBox);
		
		JPanel specificRandPanel = new JPanel();
		movesEffectsPanel.add(specificRandPanel);
		specificRandPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel moveRandPanel = new JPanel();
		specificRandPanel.add(moveRandPanel);
		moveRandPanel.setBorder(new TitledBorder(null, "Pokemon Moves", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		moveRandPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
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
		
		moveRandNumMovesBox = new JCheckBox("Random Num of Moves");
		moveRandOptionsPanel.add(moveRandNumMovesBox);
		moveRandNumMovesBox.setEnabled(false);
		
		JPanel panel = new JPanel();
		moveRandOptionsPanel.add(panel);
		
		JButton btnNewButton = new JButton("Details");
		btnNewButton.setEnabled(false);
		panel.add(btnNewButton);
		
		JPanel pokePowerPanel = new JPanel();
		specificRandPanel.add(pokePowerPanel);
		pokePowerPanel.setBorder(new TitledBorder(null, "Poke Powers", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pokePowerPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel pokePowerStrategyPanel = new JPanel();
		pokePowerPanel.add(pokePowerStrategyPanel);
		pokePowerStrategyPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JRadioButton pokePowerUnchangedButton = new JRadioButton("Unchanged");
		pokePowerUnchangedButton.setSelected(true);
		pokePowerUnchangedButton.setActionCommand("UNCHANGED");
		pokePowersStrategyGroup.add(pokePowerUnchangedButton);
		pokePowerStrategyPanel.add(pokePowerUnchangedButton);
		
		JRadioButton pokePowerShuffleButton = new JRadioButton("Shuffle");
		pokePowerShuffleButton.setActionCommand("SHUFFLE");
		pokePowerStrategyPanel.add(pokePowerShuffleButton);
		pokePowersStrategyGroup.add(pokePowerShuffleButton);
		
		JRadioButton pokePowerRandomButton = new JRadioButton("Random");
		pokePowerRandomButton.setActionCommand("RANDOM");
		pokePowerStrategyPanel.add(pokePowerRandomButton);
		pokePowersStrategyGroup.add(pokePowerRandomButton);
		
		JPanel pokePowerOptionsPanel = new JPanel();
		pokePowerPanel.add(pokePowerOptionsPanel);
		pokePowerOptionsPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		powerWithinTypeBox = new JCheckBox("Within Type");
		pokePowerOptionsPanel.add(powerWithinTypeBox);
		
		powerRandNumPowersBox = new JCheckBox("Random Num of Poke Powers");
		powerRandNumPowersBox.setEnabled(false);
		pokePowerOptionsPanel.add(powerRandNumPowersBox);
		
		JPanel panel_1 = new JPanel();
		pokePowerOptionsPanel.add(panel_1);
		
		JButton powerNumMovesDetailsButton = new JButton("Details");
		powerNumMovesDetailsButton.setEnabled(false);
		panel_1.add(powerNumMovesDetailsButton);
		
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
	        
	        settings.setTypeSpecificData(typeData);
	        settings.setMoves(movesData);
	        settings.setPokePowers(powersData);
	        settings.setMovesMatchPokeSpecific(generalRandExclPokeSpecMovesBox.isSelected());
	        settings.setMovesMatchTypeSpecific(generalRandExclTypeSpecMovesBox.isSelected());
	        
	        movesData.setMovesAttacksWithinType(moveRandWithinTypeBox.isSelected());
	        movesData.setMovesForceOneDamaging(moveRandForceDamageBox.isSelected());
	        movesData.setMovesRandomNumberOfAttacks(moveRandNumMovesBox.isSelected());
	        movesData.setMovesStrat(moveRandStrategyGoup.getSelection().getActionCommand());
	        
	        powersData.setMovesPowersWithinType(powerWithinTypeBox.isSelected());
	        powersData.setMovesRandomNumberOfPowers(powerRandNumPowersBox.isSelected());
	        powersData.setMovesPokePowerStrat(pokePowersStrategyGroup.getSelection().getActionCommand());
	        
	        return settings;
	 }
}
