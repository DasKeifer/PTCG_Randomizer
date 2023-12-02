package ptcgr.randomizer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

public class CategoryChangedCBListener implements ActionListener 
{
	private ActionTableModel toUpdate;
	
	public CategoryChangedCBListener(ActionTableModel toUpdate)
	{
		this.toUpdate = toUpdate;
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() instanceof JComboBox)
		{
			toUpdate.setRowsByCategory((String)((JComboBox<?>)e.getSource()).getSelectedItem());
		}
	}
}
