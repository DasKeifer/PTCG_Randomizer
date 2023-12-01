package randomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import randomizer.actions.Action;

public class ActionTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	
	private static final String[] COLUMN_HEADERS = {
			"Id", "Action", "Description"
	};
	
	List<Action> data;
	private HashMap<Integer, Action> allActions;
	
	public ActionTableModel(HashMap<Integer, Action> allActions)
	{
		data = new ArrayList<>();
		data.add(null);
		this.allActions = allActions;
	}

	@Override
	public int getRowCount()
	{
		return data.size();
	}

	@Override
	public int getColumnCount() 
	{
		return COLUMN_HEADERS.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) 
	{
		if (data.get(rowIndex) != null)
		{
			switch (columnIndex)
			{
				case 0: return data.get(rowIndex).getId();
				case 1: return data.get(rowIndex).getName();
				case 2: return data.get(rowIndex).getDescription();
				default: return "ERROR";
			}
		}
		else
		{
			return "";
		}
	}    
	
	@Override
    public Class<?> getColumnClass(int column) 
	{
    	if (column < COLUMN_HEADERS.length && column >= 0) 
    	{
    		return String.class;
    	}
        return Object.class;
    }
	
    @Override
    public String getColumnName(int column) 
    {
    	if (column < COLUMN_HEADERS.length && column >= 0) 
    	{
    		return COLUMN_HEADERS[column];
    	}
    	return null;
    }
	
	public void removeRow(int index)
	{
		// If its not the end null, remove it
		if (data.get(index) != null)
		{
			data.remove(index);
	        fireTableRowsDeleted(index, index);
		}
	}
	
	public void removeRows(int[] indecies)
	{
		if (indecies.length > 0)
		{
			Arrays.sort(indecies); 
			for (int i = indecies.length - 1; i >=0; i--)
			{
				// If its not the end null, remove it
				if (data.get(i) != null)
				{
					data.remove(indecies[i]);
				}
			}
			fireTableRowsDeleted(indecies[0], indecies[indecies.length - 1]);
		}
	}
	
	public void insertRowById(int index, int id)
	{
		insertRow(index, allActions.get(id));
	}
	
	public void insertRow(int index, Action a)
	{
		// if its the null index or further, set it to the last index
		if (index >= data.size())
		{
			index = data.size() - 1;
		}
		data.add(index, a);
        fireTableRowsInserted(index, index);
	}
	
	public void addRow(Action a)
	{
		insertRow(0, a);
	}
}
