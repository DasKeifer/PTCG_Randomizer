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
		frmPokemonTradingCard.setBounds(100, 100, 500, 600);
		frmPokemonTradingCard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPokemonTradingCard.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel saveRomPanel = new JPanel();
		frmPokemonTradingCard.getContentPane().add(saveRomPanel, BorderLayout.SOUTH);
		
		JButton randomizeButton = new JButton("Randomize!");
		randomizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
				    int returnVal = saveRomChooser.showSaveDialog(frmPokemonTradingCard);
				    if (returnVal == JFileChooser.APPROVE_OPTION) {
						randomizer.randomize();
						
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
		
		JCheckBox moveRandExclTypeSpecBox = new JCheckBox("Exclude Type Specific Moves");
		moveRandExclTypeSpecBox.setEnabled(false);
		moveRandOptionsPanel.setLayout(new GridLayout(3, 1, 0, 0));
		
		JCheckBox moveRandForceDamageBox = new JCheckBox("Force one damaging move");
		moveRandForceDamageBox.setEnabled(false);
		moveRandOptionsPanel.add(moveRandForceDamageBox);
		
		JCheckBox moveRandExclPokeSpecMovesBox = new JCheckBox("Exclude Poke Specific Moves");
		moveRandExclPokeSpecMovesBox.setEnabled(false);
		moveRandOptionsPanel.add(moveRandExclPokeSpecMovesBox);
		moveRandOptionsPanel.add(moveRandExclTypeSpecBox);
		
		JPanel panel_10 = new JPanel();
		movesEffectsPanel.add(panel_10);
		panel_10.setBorder(new TitledBorder(null, "Move Types", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_10.setLayout(new GridLayout(0, 1, 0, 0));
		
		JRadioButton rdbtnNewRadioButton_4 = new JRadioButton("Unchanged");
		panel_10.add(rdbtnNewRadioButton_4);
		rdbtnNewRadioButton_4.setSelected(true);
		
		JRadioButton rdbtnNewRadioButton_4_1 = new JRadioButton("Change to Poke Type");
		panel_10.add(rdbtnNewRadioButton_4_1);
		rdbtnNewRadioButton_4_1.setEnabled(false);
		
		JRadioButton rdbtnNewRadioButton_4_2 = new JRadioButton("All Colorless");
		panel_10.add(rdbtnNewRadioButton_4_2);
		rdbtnNewRadioButton_4_2.setEnabled(false);
		
		JPanel panel_4_1 = new JPanel();
		movesEffectsPanel.add(panel_4_1);
		panel_4_1.setBorder(new TitledBorder(null, "Poke Powers", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4_1.setLayout(new GridLayout(0, 1, 0, 0));
		
		JRadioButton rdbtnNewRadioButton_5 = new JRadioButton("Include with Moves");
		rdbtnNewRadioButton_5.setSelected(true);
		pokePowersGroup.add(rdbtnNewRadioButton_5);
		panel_4_1.add(rdbtnNewRadioButton_5);
		
		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("Unchanged");
		panel_4_1.add(rdbtnNewRadioButton_1);
		pokePowersGroup.add(rdbtnNewRadioButton_1);
		
		JRadioButton rdbtnNewRadioButton_2_1 = new JRadioButton("Shuffle");
		panel_4_1.add(rdbtnNewRadioButton_2_1);
		rdbtnNewRadioButton_2_1.setEnabled(false);
		pokePowersGroup.add(rdbtnNewRadioButton_2_1);
		
		JRadioButton rdbtnNewRadioButton_3_1 = new JRadioButton("Random");
		panel_4_1.add(rdbtnNewRadioButton_3_1);
		rdbtnNewRadioButton_3_1.setEnabled(false);
		pokePowersGroup.add(rdbtnNewRadioButton_3_1);
		
		JPanel panel_4_1_1 = new JPanel();
		movesEffectsPanel.add(panel_4_1_1);
		panel_4_1_1.setBorder(new TitledBorder(null, "Trainer Effects", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4_1_1.setLayout(new BorderLayout(0, 0));
		
		JLabel lblComingSoon = new JLabel("Coming Soon!");
		panel_4_1_1.add(lblComingSoon, BorderLayout.CENTER);
		
		JTabbedPane tabbedPane_2 = new JTabbedPane(JTabbedPane.TOP);
		movesEffectsTab.addTab("Comming Soon!", null, tabbedPane_2, null);
	}

	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
}
