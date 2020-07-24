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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JTree;
import java.awt.Insets;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import java.awt.FlowLayout;
import javax.swing.ButtonGroup;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class RandomizerApp {

	private JFrame frmPokemonTradingCard;
    private JFileChooser openRomChooser;
    private JFileChooser saveRomChooser;

	private Randomizer randomizer;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final Action action = new SwingAction();
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	private final ButtonGroup buttonGroup_2 = new ButtonGroup();
	
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
		frmPokemonTradingCard.setBounds(100, 100, 650, 700);
		frmPokemonTradingCard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPokemonTradingCard.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		frmPokemonTradingCard.getContentPane().add(panel, BorderLayout.SOUTH);
		
		JButton btnRandomize = new JButton("Randomize!");
		btnRandomize.addActionListener(new ActionListener() {
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
		panel.add(btnRandomize);
		
		JPanel panel_1 = new JPanel();
		frmPokemonTradingCard.getContentPane().add(panel_1, BorderLayout.NORTH);
		
		JButton btnOpenRom = new JButton("Open ROM");
		btnOpenRom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			    int returnVal = openRomChooser.showOpenDialog(frmPokemonTradingCard);
			    if (returnVal == JFileChooser.APPROVE_OPTION) {
			    	randomizer.openRom(openRomChooser.getSelectedFile());
			    }
			}
		});
		panel_1.add(btnOpenRom);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmPokemonTradingCard.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel panel_2 = new JPanel();
		tabbedPane.addTab("Moves/Effects", null, panel_2, null);
		panel_2.setLayout(new GridLayout(0, 1, 0, 0));
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "Pokemon Moves", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.add(panel_4);
		panel_4.setLayout(new GridLayout(0, 3, 0, 0));
		
		JPanel panel_3 = new JPanel();
		panel_4.add(panel_3);
		
		JRadioButton rdbtnNewRadioButton = new JRadioButton("Unchanged");
		rdbtnNewRadioButton.setSelected(true);
		buttonGroup.add(rdbtnNewRadioButton);
		
		JRadioButton rdbtnNewRadioButton_3 = new JRadioButton("Random");
		buttonGroup.add(rdbtnNewRadioButton_3);
		
		JRadioButton rdbtnNewRadioButton_2 = new JRadioButton("Shuffle");
		buttonGroup.add(rdbtnNewRadioButton_2);
		panel_3.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblNewLabel = new JLabel("Card Attacks");
		panel_3.add(lblNewLabel);
		panel_3.add(rdbtnNewRadioButton);
		panel_3.add(rdbtnNewRadioButton_2);
		panel_3.add(rdbtnNewRadioButton_3);
		
		JPanel panel_5_1_2 = new JPanel();
		panel_4.add(panel_5_1_2);
		panel_5_1_2.setLayout(new GridLayout(4, 1, 0, 0));
		
		JLabel lblMoveEnergyTypes = new JLabel("Energy Types");
		panel_5_1_2.add(lblMoveEnergyTypes);
		
		JRadioButton rdbtnNewRadioButton_4 = new JRadioButton("Unchanged");
		rdbtnNewRadioButton_4.setSelected(true);
		buttonGroup_1.add(rdbtnNewRadioButton_4);
		panel_5_1_2.add(rdbtnNewRadioButton_4);
		
		JRadioButton rdbtnNewRadioButton_4_1 = new JRadioButton("Change to Poke Type");
		rdbtnNewRadioButton_4_1.setEnabled(false);
		buttonGroup_1.add(rdbtnNewRadioButton_4_1);
		panel_5_1_2.add(rdbtnNewRadioButton_4_1);
		
		JRadioButton rdbtnNewRadioButton_4_2 = new JRadioButton("All Colorless");
		rdbtnNewRadioButton_4_2.setEnabled(false);
		buttonGroup_1.add(rdbtnNewRadioButton_4_2);
		panel_5_1_2.add(rdbtnNewRadioButton_4_2);
		
		JPanel panel_7 = new JPanel();
		panel_4.add(panel_7);
		
		JLabel lblComingSoon_1 = new JLabel("Miscellaneous");
		
		JCheckBox chckbxExcludeTypeSpecific = new JCheckBox("Exclude Type Specific Moves");
		chckbxExcludeTypeSpecific.setEnabled(false);
		panel_7.setLayout(new GridLayout(4, 1, 0, 0));
		panel_7.add(lblComingSoon_1);
		
		JCheckBox chckbxNewCheckBox_1 = new JCheckBox("Force one damaging move");
		chckbxNewCheckBox_1.setEnabled(false);
		panel_7.add(chckbxNewCheckBox_1);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("Exclude Poke Specific Moves");
		chckbxNewCheckBox.setEnabled(false);
		panel_7.add(chckbxNewCheckBox);
		panel_7.add(chckbxExcludeTypeSpecific);
		
		JPanel panel_4_1 = new JPanel();
		panel_4_1.setBorder(new TitledBorder(null, "Poke Powers", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.add(panel_4_1);
		panel_4_1.setLayout(new GridLayout(1, 2, 0, 0));
		
		JPanel panel_5 = new JPanel();
		panel_4_1.add(panel_5);
		
		JCheckBox chckbxIncludeWithMoves = new JCheckBox("Include with Moves");
		chckbxIncludeWithMoves.setEnabled(false);
		chckbxIncludeWithMoves.setSelected(true);
		panel_5.add(chckbxIncludeWithMoves);
		
		JPanel panel_6 = new JPanel();
		panel_4_1.add(panel_6);
		panel_6.setLayout(new GridLayout(3, 1, 0, 0));
		
		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("Unchanged");
		rdbtnNewRadioButton_1.setEnabled(false);
		rdbtnNewRadioButton_1.setSelected(true);
		buttonGroup_2.add(rdbtnNewRadioButton_1);
		panel_6.add(rdbtnNewRadioButton_1);
		
		JRadioButton rdbtnNewRadioButton_2_1 = new JRadioButton("Shuffle");
		rdbtnNewRadioButton_2_1.setEnabled(false);
		buttonGroup_2.add(rdbtnNewRadioButton_2_1);
		panel_6.add(rdbtnNewRadioButton_2_1);
		
		JRadioButton rdbtnNewRadioButton_3_1 = new JRadioButton("Random");
		rdbtnNewRadioButton_3_1.setEnabled(false);
		buttonGroup_2.add(rdbtnNewRadioButton_3_1);
		panel_6.add(rdbtnNewRadioButton_3_1);
		
		JPanel panel_4_1_1 = new JPanel();
		panel_4_1_1.setBorder(new TitledBorder(null, "Trainer Effects", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.add(panel_4_1_1);
		panel_4_1_1.setLayout(new BorderLayout(0, 0));
		
		JLabel lblComingSoon = new JLabel("Coming Soon!");
		panel_4_1_1.add(lblComingSoon, BorderLayout.CENTER);
		
		JTabbedPane tabbedPane_2 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Comming Soon!", null, tabbedPane_2, null);
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
