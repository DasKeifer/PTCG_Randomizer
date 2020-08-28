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
import java.awt.event.ActionEvent;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.ButtonGroup;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.FlowLayout;

public class RandomizerApp {

	private JFrame frmPokemonTradingCard;
    private JFileChooser openRomChooser;
    private JFileChooser saveRomChooser;

	private Randomizer randomizer;
	private final ButtonGroup moveRandStrategyGoup = new ButtonGroup();
	private final ButtonGroup pokePowersGroup = new ButtonGroup();
	
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
		movesEffectsPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel moveRandPanel = new JPanel();
		movesEffectsPanel.add(moveRandPanel);
		moveRandPanel.setBorder(new TitledBorder(null, "Pokemon Moves", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		moveRandPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel moveRandStrategyPanel = new JPanel();
		moveRandPanel.add(moveRandStrategyPanel);
		
		JRadioButton moveRandUnchangedButton = new JRadioButton("Unchanged");
		moveRandUnchangedButton.setSelected(true);
		moveRandStrategyGoup.add(moveRandUnchangedButton);
		
		JRadioButton moveRandRandomButtom = new JRadioButton("Random");
		moveRandStrategyGoup.add(moveRandRandomButtom);
		
		JRadioButton moveRandShuffleButton = new JRadioButton("Shuffle");
		moveRandStrategyGoup.add(moveRandShuffleButton);
		moveRandStrategyPanel.setLayout(new GridLayout(0, 1, 0, 0));
		moveRandStrategyPanel.add(moveRandUnchangedButton);
		moveRandStrategyPanel.add(moveRandShuffleButton);
		moveRandStrategyPanel.add(moveRandRandomButtom);
		
		JPanel moveRandOptionsPanel = new JPanel();
		moveRandPanel.add(moveRandOptionsPanel);
		
		JCheckBox moveRandExclTypeSpecBox = new JCheckBox("Exclude Type Specific");
		moveRandExclTypeSpecBox.setEnabled(false);
		moveRandOptionsPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JCheckBox moveRandWithinTypeBox = new JCheckBox("Within Type");
		moveRandOptionsPanel.add(moveRandWithinTypeBox);
		
		JCheckBox moveRandForceDamageBox = new JCheckBox("Force One Damaging");
		moveRandForceDamageBox.setEnabled(false);
		moveRandOptionsPanel.add(moveRandForceDamageBox);
		
		JCheckBox moveRandExclPokeSpecMovesBox = new JCheckBox("Exclude Poke Specific");
		moveRandExclPokeSpecMovesBox.setEnabled(false);
		moveRandOptionsPanel.add(moveRandExclPokeSpecMovesBox);
		moveRandOptionsPanel.add(moveRandExclTypeSpecBox);
		
		JPanel pokePowerPanel = new JPanel();
		movesEffectsPanel.add(pokePowerPanel);
		pokePowerPanel.setBorder(new TitledBorder(null, "Poke Powers", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pokePowerPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JPanel pokePowerStrategyPanel = new JPanel();
		pokePowerPanel.add(pokePowerStrategyPanel);
		pokePowerStrategyPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JRadioButton pokePowerInclWMovesButton = new JRadioButton("Include with Moves");
		pokePowerStrategyPanel.add(pokePowerInclWMovesButton);
		pokePowerInclWMovesButton.setSelected(true);
		pokePowersGroup.add(pokePowerInclWMovesButton);
		
		JRadioButton pokePowerUnchangedButton = new JRadioButton("Unchanged");
		pokePowerStrategyPanel.add(pokePowerUnchangedButton);
		pokePowersGroup.add(pokePowerUnchangedButton);
		
		JRadioButton pokePowerShuffleButton = new JRadioButton("Shuffle");
		pokePowerStrategyPanel.add(pokePowerShuffleButton);
		pokePowerShuffleButton.setEnabled(false);
		pokePowersGroup.add(pokePowerShuffleButton);
		
		JRadioButton pokePowerRandomButton = new JRadioButton("Random");
		pokePowerStrategyPanel.add(pokePowerRandomButton);
		pokePowerRandomButton.setEnabled(false);
		pokePowersGroup.add(pokePowerRandomButton);
		
		JPanel pokePowerOptionsPanel = new JPanel();
		pokePowerPanel.add(pokePowerOptionsPanel);
		pokePowerOptionsPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JCheckBox pokePowerWithinTypeBox = new JCheckBox("Within Type");
		pokePowerWithinTypeBox.setEnabled(false);
		pokePowerOptionsPanel.add(pokePowerWithinTypeBox);
		
		JCheckBox pokePowerExclPokeSpecBox = new JCheckBox("Exclude Poke Specific");
		pokePowerExclPokeSpecBox.setEnabled(false);
		pokePowerOptionsPanel.add(pokePowerExclPokeSpecBox);
		
		JCheckBox pokePowerExclTypeSpecBox = new JCheckBox("Exclude Type Specific");
		pokePowerExclTypeSpecBox.setEnabled(false);
		pokePowerOptionsPanel.add(pokePowerExclTypeSpecBox);
		
		JPanel trainerPanel = new JPanel();
		movesEffectsPanel.add(trainerPanel);
		trainerPanel.setBorder(new TitledBorder(null, "Trainer Effects", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		trainerPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblComingSoon = new JLabel("Coming Soon!");
		trainerPanel.add(lblComingSoon, BorderLayout.CENTER);
		
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
	        return settings;
	 }
}
