package ptcgr.randomizer.gui;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;


public class ActionTransferHandler extends TransferHandler
{
	private static final long serialVersionUID = 1L;
	int[] movedRows;

	@Override
	public int getSourceActions(JComponent c) 
	{
		return TransferHandler.COPY_OR_MOVE;
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
				ActionTableModel ate = (ActionTableModel) table.getModel();
				int[] rows = table.getSelectedRows();
				for (int row : rows)
				{
					sb.append(ate.getRow(row).getId()).append(",");
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
			if (table.getModel() instanceof ActionTableModel)
			{
				ActionTableModel model = (ActionTableModel) table.getModel();
				if (act == TransferHandler.MOVE)
				{
					if (movedRows != null)
					{
						model.removeRows(movedRows);
						movedRows = null;
					}
					else
					{
						model.removeRows(table.getSelectedRows());
					}
				}
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
		if (canImport(info))
		{
			JTable target = (JTable)info.getComponent();
			ActionTableModel model = (ActionTableModel)target.getModel();
			
			int index = -1; // max unless set otherwise
			if (info.isDrop())
			{
				JTable.DropLocation dl = (JTable.DropLocation) info.getDropLocation();
				index = dl.getRow();
			}
			else
			{
				// Insert behind the last selected row (or end if there is none)
				int[] selectedRows = target.getSelectedRows();
				if (selectedRows.length != 0)
				{
					index = selectedRows[selectedRows.length - 1] + 1;
				}
			}
			
			int max = target.getModel().getRowCount();
			if (index < 0 || index > max)
			{
				index = max;
			}
			target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			
			String[] copied;
			try {
				copied = ((String) info.getTransferable().getTransferData(DataFlavor.stringFlavor)).split(",");
				 
				// Add the new rows first so select indexes update correctly
				int indexStart = index;
				for (String row : copied)
				{
					model.insertRowById(index++, Integer.parseInt(row));
				}

				// store the selected rows if its a move within ourselves so we can
				// delete them in exportDone
				if (this == target.getTransferHandler() && info.isDrop() && info.getDropAction() == TransferHandler.MOVE)
				{
					movedRows = target.getSelectedRows();
				}
				
				// Now select what was just copied
				target.getSelectionModel().clearSelection();
				target.getSelectionModel().setSelectionInterval(indexStart, index - 1);
				return true;
			} catch (UnsupportedFlavorException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return false;
	}
}
