/*
 * CharacterTabs.java
 * Copyright 2009 Connor Petty <cpmeister@users.sourceforge.net>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Created on Aug 29, 2009, 2:59:53 PM
 */
package pcgen.gui2;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.lang.StringUtils;
import pcgen.core.facade.CharacterFacade;
import pcgen.core.facade.ReferenceFacade;
import pcgen.core.facade.event.ListEvent;
import pcgen.core.facade.event.ListListener;
import pcgen.core.facade.event.ReferenceEvent;
import pcgen.core.facade.event.ReferenceListener;
import pcgen.core.facade.util.ListFacade;
import pcgen.gui2.tabs.InfoTabbedPane;
import pcgen.gui2.tools.Icons;
import pcgen.gui2.util.SharedTabPane;
import pcgen.system.CharacterManager;

/**
 *
 * @author Connor Petty <cpmeister@users.sourceforge.net>
 */
public final class CharacterTabs extends SharedTabPane
		implements ChangeListener, ReferenceListener<CharacterFacade>, ListListener<CharacterFacade>
{

	private final PCGenFrame frame;
	//This holds the list of characters as well as the order in which they are displayed
	private final List<CharacterFacade> characters;
	private final Map<CharacterFacade, TabLabel> listenerMap;
	private final InfoTabbedPane infoTabbedPane;
	private final JPopupMenu popupMenu;

	public CharacterTabs(PCGenFrame frame)
	{
		this.frame = frame;
		this.characters = new ArrayList<CharacterFacade>();
		this.infoTabbedPane = new InfoTabbedPane();
		this.listenerMap = new HashMap<CharacterFacade, TabLabel>();
		this.popupMenu = new JPopupMenu();
		initComponents();
	}

	private void initComponents()
	{
		CharacterManager.getCharacters().addListListener(this);
		frame.getSelectedCharacterRef().addReferenceListener(this);

		setTabPlacement(JTabbedPane.BOTTOM);
		addChangeListener(this);
		setSharedComponent(infoTabbedPane);
		//Initialize popup menu
		PCGenActionMap actions = frame.getActionMap();
		popupMenu.add(actions.get(PCGenActionMap.NEW_COMMAND));
		popupMenu.add(actions.get(PCGenActionMap.CLOSE_COMMAND));
		popupMenu.add(actions.get(PCGenActionMap.SAVE_COMMAND));
		popupMenu.add(actions.get(PCGenActionMap.SAVEAS_COMMAND));
		addMouseListener(new PopupListener());
	}

	private void addCharacter(CharacterFacade character)
	{
		characters.add(character);
		TabLabel tabLabel = new TabLabel(character);
		addTab(tabLabel);
		listenerMap.put(character, tabLabel);
	}

	private void removeCharacter(CharacterFacade character)
	{
		int index = characters.indexOf(character);
		characters.remove(index);
		removeTabAt(index);
		listenerMap.remove(character).removeListeners();
	}

	public void referenceChanged(ReferenceEvent<CharacterFacade> e)
	{
		setSelectedIndex(characters.indexOf(e.getNewReference()));
	}

	public void stateChanged(ChangeEvent e)
	{
		int index = getSelectedIndex();
		CharacterFacade character = index != -1 ? characters.get(index) : null;
		frame.setSelectedCharacter(character);
		if (character != null)
		{
			infoTabbedPane.setCharacter(character);
		}
		else
		{
			infoTabbedPane.clearStateMap();
		}
	}

	public void elementAdded(ListEvent<CharacterFacade> e)
	{
		addCharacter(e.getElement());
	}

	public void elementRemoved(ListEvent<CharacterFacade> e)
	{
		removeCharacter(e.getElement());
	}

	public void elementsChanged(ListEvent<CharacterFacade> e)
	{
		removeAll();
		for (CharacterFacade character : characters)
		{
			listenerMap.remove(character).removeListeners();
		}
		characters.clear();
		for (CharacterFacade character : (ListFacade<CharacterFacade>) e.getSource())
		{
			addCharacter(character);
		}
		infoTabbedPane.clearStateMap();
	}

	private class TabLabel extends JPanel implements ActionListener, ReferenceListener<String>
	{

		private JLabel titleLabel;
		private JButton closeButton;
		private CharacterFacade character;
		private ReferenceFacade<String> tabNameRef;
		private ReferenceFacade<String> nameRef;

		public TabLabel(CharacterFacade character)
		{
			super(new FlowLayout(FlowLayout.CENTER, 5, 2));
			this.character = character;
			this.tabNameRef = character.getTabNameRef();
			this.nameRef = character.getNameRef();

			initComponents();

			closeButton.addActionListener(this);
			tabNameRef.addReferenceListener(this);
			nameRef.addReferenceListener(this);
		}

		private void initComponents()
		{
			if (StringUtils.isNotEmpty(tabNameRef.getReference()))
			{
				titleLabel = new JLabel(tabNameRef.getReference());
			}
			else
			{
				titleLabel = new JLabel(nameRef.getReference());
			}
			add(titleLabel);

			closeButton = new JButton();
			closeButton.setMargin(new Insets(0, 0, 0, 0));
			closeButton.setBorder(BorderFactory.createEmptyBorder());
			closeButton.setFocusable(false);
			closeButton.setBorderPainted(false);
			closeButton.setContentAreaFilled(false);
			closeButton.setRolloverEnabled(true);
			
			ImageIcon icon = Icons.XButton_Stat.getImageIcon();
			Dimension size = new Dimension(icon.getIconWidth(), icon.getIconHeight());
			Insets insets = closeButton.getInsets();
			size.width += insets.left + insets.right;
			size.height += insets.top + insets.bottom;
			closeButton.setMinimumSize(size);
			closeButton.setPreferredSize(size);
			closeButton.setMaximumSize(size);

			closeButton.setIcon(icon);
			closeButton.setRolloverIcon(Icons.XButton_Roll.getImageIcon());
			closeButton.setPressedIcon(Icons.XButton_Click.getImageIcon());
			add(closeButton);

			setOpaque(false);
		}

		public void removeListeners()
		{
			tabNameRef.removeReferenceListener(this);
			nameRef.removeReferenceListener(this);
		}

		public void referenceChanged(ReferenceEvent<String> e)
		{
			if (e.getSource() == nameRef)
			{
				if (StringUtils.isEmpty(tabNameRef.getReference()))
				{
					titleLabel.setText(e.getNewReference());
				}
			}
			else
			{
				if (StringUtils.isNotEmpty(e.getNewReference()))
				{
					titleLabel.setText(e.getNewReference());
				}
				else
				{
					titleLabel.setText(nameRef.getReference());
				}
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			frame.closeCharacter(character);
		}

	}

	private class PopupListener extends MouseAdapter
	{

		public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e)
		{
			if (e.isPopupTrigger())
			{
				popupMenu.setVisible(true);
				popupMenu.show(e.getComponent(),
							   e.getX(), e.getY() - popupMenu.getHeight());
			}
		}

	}

}