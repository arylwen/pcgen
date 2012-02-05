/**
 * @(#)JTreeTable.java    1.2 98/10/27
 *
 * Copyright 1997, 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 *
 * Current Ver: $Revision: 5184 $
 * Last Editor: $Author: cpmeister $
 * Last Edited: $Date: 2008-02-13 17:03:32 -0800 (Wed, 13 Feb 2008) $
 *
 **/
package pcgen.gui2.util;

import java.util.Comparator;
import java.util.List;
import pcgen.gui2.util.treetable.TreeTableModel;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.EventObject;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import pcgen.gui2.util.table.SortableTableModel;
import pcgen.gui2.util.treetable.DefaultSortableTreeTableModel;
import pcgen.gui2.util.treetable.SortableTreeTableModel;
import pcgen.gui2.util.treetable.TreeTableNode;

/**
 * This example shows how to create a simple JTreeTable component,
 * by using a JTree as a renderer (and editor) for the cells in a
 * particular column in the JTable.
 *
 * @version 1.2 10/27/98
 *
 * @author Philip Milne
 * @author Scott Violet
 **/
public class JTreeTable extends JTableEx
{

	private static final long serialVersionUID = -3571248405124682593L;
	/** A subclass of JTree. */
	private TreeTableCellRenderer tree;
	private TreeTableModelAdapter adapter;

	static
	{
		/*
		JTreeTable's event handling assumes bad things about
		mouse pressed/released that are not true on MacOS X.
		For example, one gets NPEs thrown when the mouse is
		hit because the event manager is waiting for released
		and one never gets the release.
		It turns out that the MetalLAF handles this happily and
		thus we can use that to get appropriate line styles,
		without knackering Mac support.
		Fix done by LeeAnn Rucker, formerly at Apple for Javasoft.
		Added to pcgen by Scott Ellsworth
		 */
		UIManager.put("TreeTableUI", "javax.swing.plaf.metal.MetalTreeUI"); //$NON-NLS-1$ //$NON-NLS-2$
		UIManager.put("Tree.leftChildIndent", Integer.valueOf(3)); //$NON-NLS-1$
		UIManager.put("Tree.rightChildIndent", Integer.valueOf(8)); //$NON-NLS-1$
	}

	public JTreeTable()
	{
		this(null);
	}

	/**
	 * Constructor
	 * @param treeTableModel
	 */
	public JTreeTable(TreeTableModel treeTableModel)
	{
		super();
		tree = new TreeTableCellRenderer();
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		adapter = new TreeTableModelAdapter(tree);
		setTreeTableModel(treeTableModel);
		super.setModel(adapter);
		// Force the JTable and JTree to share row selection models.
		ListToTreeSelectionModelWrapper selectionWrapper =
				new ListToTreeSelectionModelWrapper();
		tree.setSelectionModel(selectionWrapper);
		setSelectionModel(selectionWrapper.getListSelectionModel());

		// Install the tree editor renderer and editor.
		setDefaultRenderer(TreeTableNode.class, tree);
		setDefaultEditor(TreeTableNode.class, new TreeTableCellEditor());

		// No grid.
		setShowGrid(false);

		// No intercell spacing
		setIntercellSpacing(new Dimension(0, 0));

		// And update the height of the trees row to match the table
		if (tree.getRowHeight() < 1)
		{
			// Metal looks better like this.
			setRowHeight(18);
		}
		else
		{
			// If the UI has specified a rowHeight,
			// we'd better all be using the same one!
			setRowHeight(tree.getRowHeight());
		}
	}

	@SuppressWarnings("unchecked")
	public TreeTableModel getTreeTableModel()
	{
		return (TreeTableModel) tree.getModel();
	}

	public void setTreeTableModel(TreeTableModel model)
	{
		if (model != null && !(model instanceof SortableTreeTableModel))
		{
			model = new DefaultSortableTreeTableModel(model);
		}
		tree.setModel(model);
		adapter.setTreeTableModel((SortableTreeTableModel) model);
	}

	/**
	 * Workaround for BasicTableUI anomaly. Make sure the UI never tries to
	 * paint the editor. The UI currently uses different techniques to
	 * paint the renderers and editors and overriding setBounds() below
	 * is not the right thing to do for an editor. Returning -1 for the
	 * editing row in this case, ensures the editor is never painted.
	 * @return editing row
	 **/
	@Override
	public int getEditingRow()
	{
		return (getColumnClass(editingColumn) == TreeTableNode.class) ? (-1)
				: editingRow;
	}

	/**
	 * Overridden to pass the new rowHeight to the tree.
	 * @param aRowHeight
	 **/
	@Override
	public void setRowHeight(int aRowHeight)
	{
		super.setRowHeight(aRowHeight);

		if ((tree != null) && (tree.getRowHeight() != aRowHeight))
		{
			tree.setRowHeight(getRowHeight());
		}
	}

	/**
	 * Returns the tree that is being shared between the model.
	 * @return JTree
	 **/
	public JTree getTree()
	{
		return tree;
	}

	public void setTreeCellRenderer(TreeCellRenderer renderer)
	{
		tree.setCellRenderer(renderer);
	}

	/**
	 * Forwards the <code>scrollRectToVisible()</code> message to the
	 * <code>JComponent</code>'s parent. Components that can service
	 * the request, such as <code>JViewport</code>,
	 * override this method and perform the scrolling.
	 *
	 * @param aRect the visible <code>Rectangle</code>
	 * @see javax.swing.JViewport
	 */
	@Override
	public void scrollRectToVisible(Rectangle aRect)
	{
		Container parent;
		int dx = getX();
		int dy = getY();

		for (parent = getParent(); !(parent == null) &&
				!(parent instanceof JComponent) &&
				!(parent instanceof CellRendererPane); parent =
						parent.getParent())
		{
			final Rectangle bounds = parent.getBounds();

			dx += bounds.x;
			dy += bounds.y;
		}

		if ((parent != null) && !(parent instanceof CellRendererPane))
		{
			aRect.x += dx;
			aRect.y += dy;

			((JComponent) parent).scrollRectToVisible(aRect);
			aRect.x -= dx;
			aRect.y -= dy;
		}
	}

	/**
	 * Overridden to message super and forward the method to the tree.
	 * Since the tree is not actually in the component hieachy it will
	 * never receive this unless we forward it in this manner.
	 **/
	@Override
	public void updateUI()
	{
		super.updateUI();

		if (tree != null)
		{
			tree.updateUI();
		}

		// Use the tree's default foreground and background
		// colors in the table
		LookAndFeel.installColorsAndFont(this,
										 "Tree.background", "Tree.foreground",
										 "Tree.font"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
	}

	/**
	 * Makes sure all the path components in path are expanded (except
	 * for the last path component) and scrolls so that the
	 * node identified by the path is displayed. Only works when this
	 * <code>JTree</code> is contained in a <code>JScrollPane</code>.
	 *
	 * @param path  the <code>TreePath</code> identifying the node to
	 *         bring into view
	 */
	private void scrollPathToVisible(TreePath path)
	{
		if (path != null)
		{
			tree.makeVisible(path);

			Rectangle bounds = tree.getPathBounds(path);

			if (bounds != null)
			{
				scrollRectToVisible(bounds);
			}
		}
	}

	/**
	 * This is a wrapper class takes a TreeTableModel and implements
	 * the table model interface. The implementation is trivial, with
	 * all of the event dispatching support provided by the superclass:
	 * the AbstractTableModel.
	 *
	 * @version 1.2 10/27/98
	 *
	 * @author Philip Milne
	 * @author Scott Violet
	 */
	private static class TreeTableModelAdapter extends AbstractTableModel
			implements SortableTableModel, TreeModelListener,
			TreeExpansionListener
	{

		private JTree tree;
		private SortableTreeTableModel treeTableModel;

		/**
		 * Constructor
		 * @param treeTableModel
		 * @param tree
		 */
		TreeTableModelAdapter(JTree tree)
		{
			this.tree = tree;
			tree.addTreeExpansionListener(this);
		}

		/**
		 * Install a TreeModelListener that can update the table when
		 * tree changes. We use delayedFireTableDataChanged as we can
		 * not be guaranteed the tree will have finished processing
		 * the event before us.
		 **/
		public void setTreeTableModel(SortableTreeTableModel model)
		{
			if (treeTableModel != null)
			{
				treeTableModel.removeTreeModelListener(this);
			}
			treeTableModel = model;
			if (treeTableModel != null)
			{
				treeTableModel.addTreeModelListener(this);
			}
			fireTableStructureChanged();
		}

		@Override
		public boolean isCellEditable(int row, int column)
		{
			if (treeTableModel == null)
			{
				return false;
			}
			return treeTableModel.isCellEditable(nodeForRow(row), column);
		}

		@Override
		public Class<?> getColumnClass(int column)
		{
			if (treeTableModel == null)
			{
				return Object.class;
			}
			return treeTableModel.getColumnClass(column);
		}

		// Wrappers, implementing TableModel interface.
		public int getColumnCount()
		{
			if (treeTableModel == null)
			{
				return 0;
			}
			return treeTableModel.getColumnCount();
		}

		@Override
		public String getColumnName(int column)
		{
			if (treeTableModel == null)
			{
				return null;
			}
			return treeTableModel.getColumnName(column);
		}

		public int getRowCount()
		{
			return tree.getRowCount();
		}

		@Override
		public void setValueAt(Object value, int row, int column)
		{
			if (treeTableModel == null)
			{
				return;
			}
			treeTableModel.setValueAt(value, nodeForRow(row), column);
		}

		public Object getValueAt(int row, int column)
		{
			if (treeTableModel == null)
			{
				return null;
			}
			return treeTableModel.getValueAt(nodeForRow(row), column);
		}

		private Object nodeForRow(int row)
		{
			TreePath treePath = tree.getPathForRow(row);
			if (treePath != null)
			{
				return treePath.getLastPathComponent();
			}
			return null;
		}

		public void sortModel(Comparator<List<?>> comparator)
		{
			if (treeTableModel == null)
			{
				return;
			}
			Enumeration<TreePath> paths = tree.getExpandedDescendants(new TreePath(
					treeTableModel.getRoot()));
			TreePath[] selectionPaths = tree.getSelectionPaths();
			treeTableModel.sortModel(comparator);
			if (paths != null)
			{
				while (paths.hasMoreElements())
				{
					tree.expandPath(paths.nextElement());
				}
			}
			tree.setSelectionPaths(selectionPaths);
		}

		public void treeNodesChanged(TreeModelEvent e)
		{
			TreePath parentPath = e.getTreePath();
			int leadingRow = Integer.MAX_VALUE;
			int trailingRow = -1;
			if (e.getChildren() != null)
			{
				for (Object node : e.getChildren())
				{
					TreePath childPath = parentPath.pathByAddingChild(node);
					int row = tree.getRowForPath(childPath);
					leadingRow = Math.min(leadingRow, row);
					trailingRow = Math.max(trailingRow, row);
				}
			}
			fireTableRowsUpdated(leadingRow, trailingRow);
		}

		public void treeNodesInserted(TreeModelEvent e)
		{
			fireTableDataChanged();
		}

		public void treeNodesRemoved(TreeModelEvent e)
		{
			fireTableDataChanged();
		}

		public void treeStructureChanged(TreeModelEvent e)
		{
			fireTableStructureChanged();
		}
		// Don't use fireTableRowsInserted() here;
		// the selection model would get updated twice.

		public void treeExpanded(TreeExpansionEvent event)
		{
			fireTableDataChanged();
		}

		public void treeCollapsed(TreeExpansionEvent event)
		{
			fireTableDataChanged();
		}

	}

	/**
	 * A TreeCellRenderer that displays a JTree.
	 **/
	final class TreeTableCellRenderer extends JTree implements
			TableCellRenderer
	{
		// Last table/tree row asked to render

		private int visibleRow;
		private DefaultTableCellRenderer tableCellRenderer;

		public TreeTableCellRenderer()
		{
			this.tableCellRenderer = new DefaultTableCellRenderer()
			{

				@Override
				public void setBounds(int x, int y, int width, int height)
				{
					super.setBounds(x, y, width, height);
					TreeTableCellRenderer.this.setBounds(x, y, width, height);
				}

				@Override
				public void paint(final Graphics g)
				{
					g.setColor(getBackground());
					g.fillRect(0, 0, getWidth(), getHeight());
					TreeTableCellRenderer.this.paint(g);
					paintBorder(g);
				}

				@Override
				protected void setValue(Object value)
				{
					super.setValue(value);
					setToolTipText(getText());
				}

			};
			//TODO: this code doesn't belong here
			DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
			renderer.setClosedIcon(null);
			renderer.setLeafIcon(null);
			renderer.setOpenIcon(null);
			this.setCellRenderer(renderer);
			this.setOpaque(false);
		}

		/**
		 * This is overridden to set the height
		 * to match that of the JTable.
		 * @param x
		 * @param y
		 * @param w
		 * @param h
		 **/
		@Override
		public void setBounds(int x,
							  @SuppressWarnings("unused") int y, int w,
							  @SuppressWarnings("unused") int h)
		{
			super.setBounds(x, 0, w, JTreeTable.this.getHeight());
		}

		/**
		 * Sets the row height of the tree and forwards
		 * the row height to the table.
		 * @param aRowHeight
		 **/
		@Override
		public void setRowHeight(int aRowHeight)
		{
			super.setRowHeight(aRowHeight);
			if (aRowHeight > 0)
			{
				if ((JTreeTable.this != null) &&
						(JTreeTable.this.getRowHeight() != aRowHeight))
				{
					JTreeTable.this.setRowHeight(JTreeTable.this.getRowHeight());
				}
			}
		}

		/**
		 * TreeCellRenderer method.
		 * Overridden to update the visible row.
		 * @param table
		 * @param value
		 * @param isSelected
		 * @param hasFocus
		 * @param row
		 * @param column
		 * @return Component
		 **/
		public Component getTableCellRendererComponent(JTable table,
													   Object value,
													   boolean isSelected,
													   boolean hasFocus,
													   int row,
													   int column)
		{
			if (isSelected)
			{
				this.setBackground(table.getSelectionBackground());
			}
			else
			{
				this.setBackground(table.getBackground());
			}

			visibleRow = row;

			return tableCellRenderer.getTableCellRendererComponent(table, value,
																   isSelected,
																   hasFocus, row,
																   column);
		}

		/**
		 * Sublcassed to translate the graphics such
		 * that the last visible row will be drawn at 0,0.
		 * @param g
		 **/
		@Override
		public void paint(final Graphics g)
		{
			Rectangle rect = JTreeTable.this.getCellRect(visibleRow, 0, true);
			int offset = -rect.y;
			g.translate(0, offset);
			try
			{
				super.paint(g);
			}
			catch (Exception e)
			{
				// TODO Handle this?
			}
			finally
			{
				g.translate(0, -offset);
			}
		}

		/**
		 * updateUI is overridden to set the colors
		 * of the Trees renderer to match that of the table.
		 **/
		@Override
		public void updateUI()
		{
			super.updateUI();

			// Make the tree's cell renderer use the
			// table's cell selection colors.
			TreeCellRenderer tcr = getCellRenderer();

			if (tcr instanceof DefaultTreeCellRenderer)
			{
				DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
				dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground")); //$NON-NLS-1$
				dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground")); //$NON-NLS-1$
			}
		}

	}

	/**
	 * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
	 * to listen for changes in the ListSelectionModel it maintains. Once
	 * a change in the ListSelectionModel happens, the paths are updated
	 * in the DefaultTreeSelectionModel.
	 **/
	private final class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
	{

		static final long serialVersionUID = -3571248405124682593L;
		// Set to true when we are updating the ListSelectionModel
		private boolean updatingListSelectionModel;

		private ListToTreeSelectionModelWrapper()
		{
			super();
			getListSelectionModel().
					addListSelectionListener(
					createListSelectionListener());
		}

		/**
		 * This is overridden to set updatingListSelectionModel
		 * and message super. This is the only place
		 * DefaultTreeSelectionModel alters the ListSelectionModel.
		 **/
		@Override
		public void resetRowSelection()
		{
			if (!updatingListSelectionModel)
			{
				updatingListSelectionModel = true;

				try
				{
					super.resetRowSelection();
				}
				finally
				{
					updatingListSelectionModel = false;
				}
			}

			// Notice how we don't message super if
			// updatingListSelectionModel is true. If
			// updatingListSelectionModel is true, it implies the
			// ListSelectionModel has already been updated and the
			// paths are the only thing that needs to be updated.
		}

		/**
		 * Returns the list selection model.
		 * ListToTreeSelectionModelWrapper listens for changes
		 * to this model and updates the selected paths accordingly.
		 * @return ListSelectionModel
		 **/
		private ListSelectionModel getListSelectionModel()
		{
			return listSelectionModel;
		}

		/**
		 * Creates and returns an instance of ListSelectionHandler.
		 * @return ListSelectionListener
		 **/
		private ListSelectionListener createListSelectionListener()
		{
			return new ListSelectionHandler();
		}

		/**
		 * If <code>updatingListSelectionModel</code> is false,
		 * this will reset the selected paths from the selected
		 * rows in the list selection model.
		 **/
		private void updateSelectedPathsFromSelectedRows()
		{
			if (!updatingListSelectionModel)
			{
				updatingListSelectionModel = true;

				try
				{
					int[] sRows = getSelectedRows();

					if ((sRows == null) || (sRows.length == 0))
					{
						return;
					}

					int count = 0;

					for (int i = 0; i < sRows.length; i++)
					{
						if (tree.getPathForRow(sRows[i]) != null)
						{
							count++;
						}
					}

					if (count == 0)
					{
						return;
					}

					TreePath[] tps = new TreePath[count];
					count = 0;

					for (int i = 0; i < sRows.length; i++)
					{
						TreePath tp = tree.getPathForRow(sRows[i]);

						if (tp != null)
						{
							tps[count++] = tp;
						}
					}

					// don't ned a clear as we are
					// using setSelectionPaths()
					//clearSelection();
					setSelectionPaths(tps);
				}
				finally
				{
					updatingListSelectionModel = false;
				}
			}
		}

		/**
		 * Class responsible for calling
		 * updateSelectedPathsFromSelectedRows when the
		 * selection of the list changse.
		 **/
		final class ListSelectionHandler implements ListSelectionListener
		{

			/**
			 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
			 */
			public void valueChanged(
					@SuppressWarnings("unused") ListSelectionEvent e)
			{
				updateSelectedPathsFromSelectedRows();
			}

		}

	}

	/**
	 * TreeTableCellEditor implementation.
	 * Component returned is the JTree.
	 **/
	private final class TreeTableCellEditor implements TableCellEditor
	{

		/**
		 * Overridden to return false, and if the event is a mouse event
		 * it is forwarded to the tree.<p>
		 * The behavior for this is debatable, and should really be offered
		 * as a property. By returning false, all keyboard actions are
		 * implemented in terms of the table. By returning true, the
		 * tree would get a chance to do something with the keyboard
		 * events. For the most part this is ok. But for certain keys,
		 * such as left/right, the tree will expand/collapse where as
		 * the table focus should really move to a different column. Page
		 * up/down should also be implemented in terms of the table.
		 * By returning false this also has the added benefit that clicking
		 * outside of the bounds of the tree node, but still in the tree
		 * column will select the row, whereas if this returned true
		 * that wouldn't be the case.
		 * <p>By returning false we are also enforcing the policy that
		 * the tree will never be editable (at least by a key sequence).
		 * @param e
		 * @return true if cell editable
		 */
		public boolean isCellEditable(EventObject e)
		{
			if (e instanceof MouseEvent)
			{
				for (int counter = getColumnCount() - 1; counter >= 0; counter--)
				{
					if (getColumnClass(counter) == TreeTableNode.class)
					{
						MouseEvent me = (MouseEvent) e;
						MouseEvent newME =
								new MouseEvent(tree, me.getID(), me.getWhen(),
											   me.getModifiers(), me.getX(),
											   me.getY(), me.getClickCount(),
											   me.isPopupTrigger());
						tree.dispatchEvent(newME);

						break;
					}
				}
			}

			return false;
		}

		/**
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		public Component getTableCellEditorComponent(
				@SuppressWarnings("unused") JTable table,
				@SuppressWarnings("unused") Object value,
				@SuppressWarnings("unused") boolean isSelected,
				@SuppressWarnings("unused") int r,
				@SuppressWarnings("unused") int c)
		{
			return tree;
		}

		public Object getCellEditorValue()
		{
			return null;
		}

		public boolean shouldSelectCell(EventObject anEvent)
		{
			return false;
		}

		public boolean stopCellEditing()
		{
			return true;
		}

		public void cancelCellEditing()
		{
		}

		public void addCellEditorListener(CellEditorListener l)
		{
		}

		public void removeCellEditorListener(CellEditorListener l)
		{
		}

	}

	/**
	 * Associates a popup menu with the tree table.
	 *
	 * <p>This handles showing the popup based on a right click and also handles
	 * any menu accelerators.
	 *
	 * @param aPopupMenu Menu to associate.
	 */
	public void addPopupMenu(final JPopupMenu aPopupMenu)
	{
		addMouseListener(new PopupListener(this, aPopupMenu));
	}

	private class PopupListener extends MouseAdapter
	{

		private JPopupMenu theMenu;
		private JTree theTree;

		private PopupListener(final JTreeTable treeTable,
							  final JPopupMenu aMenu)
		{
			theTree = treeTable.getTree();
			theMenu = aMenu;
		}

		/**
		 * Overridden to potential show the popup menu.
		 *
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent evt)
		{
			maybeShowPopup(evt);
		}

		/**
		 * Overridden to potentially show the popup menu.
		 *
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent evt)
		{
			maybeShowPopup(evt);
		}

		private void maybeShowPopup(MouseEvent evt)
		{
			if (evt.isPopupTrigger())
			{
				final TreePath selPath =
						theTree.getClosestPathForLocation(evt.getX(), evt.getY());

				if (selPath == null)
				{
					return;
				}

				if (theTree.isSelectionEmpty())
				{
					theTree.setSelectionPath(selPath);
					theMenu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
				else if (!theTree.isPathSelected(selPath))
				{
					theTree.setSelectionPath(selPath);
					theMenu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
				else
				{
					theTree.addSelectionPath(selPath);
					theMenu.show(evt.getComponent(), evt.getX(), evt.getY());
				}
			}
		}

	}

}