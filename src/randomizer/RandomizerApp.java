package randomizer;

import java.awt.EventQueue;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JPanel;

import rom.RomHandler;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

public class RandomizerApp {

	private JFrame frmPokemonTradingCard;
    private JFileChooser openRomChooser;

	private Randomizer randomizer;
	
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
		openRomChooser.setCurrentDirectory(new File(".")); // Jar location
	    openRomChooser.setSelectedFile(null);
		
		frmPokemonTradingCard = new JFrame();
		frmPokemonTradingCard.setTitle("Pokemon Trading Card Game Randomizer");
		frmPokemonTradingCard.setBounds(100, 100, 700, 500);
		frmPokemonTradingCard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPokemonTradingCard.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		frmPokemonTradingCard.getContentPane().add(panel, BorderLayout.SOUTH);
		
		JButton btnRandomize = new JButton("Randomize!");
		btnRandomize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					randomizer.randomize();
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
	}

}
