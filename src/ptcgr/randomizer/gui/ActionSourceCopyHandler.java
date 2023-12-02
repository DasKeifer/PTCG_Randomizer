package ptcgr.randomizer.gui;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;


public class ActionSourceCopyHandler extends TransferHandler
{
	private static final long serialVersionUID = 1L;

	@Override
	public int getSourceActions(JComponent c) 
	{
		return TransferHandler.COPY;
	}

	@Override
	protected Transferable createTransferable(JComponent c) 
	{
		StringBuilder sb = new StringBuilder();
		if (c instanceof JTable)
		{
			JTable table = (JTable) c;
			if (table.getModel() instanceof ActionTableModel)
			{
				int[] rows = table.getSelectedRows();
				for (int row : rows)
				{
					sb.append(table.getValueAt(row, 0)).append(",");
				}
			}
		}
		return new StringSelection(sb.toString());
	}

	@Override
	protected void exportDone(JComponent c, Transferable t, int act) 
	{
		if (c instanceof JTable)
		{
			JTable table = (JTable) c;
			if (act == TransferHandler.COPY || act == TransferHandler.NONE)
			{
				table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
	
	@Override
	public boolean canImport(TransferHandler.TransferSupport info) 
	{
		boolean ok = info.getComponent() instanceof JTable &&
				((JTable) info.getComponent()).getModel() instanceof ActionTableModel &&
				info.isDataFlavorSupported(DataFlavor.stringFlavor);
		((JTable) info.getComponent()).setCursor(ok ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
		return ok;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport info)
	{
		// Do nothing
		return canImport(info);
	}
}
