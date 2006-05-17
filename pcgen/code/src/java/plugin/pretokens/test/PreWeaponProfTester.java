/*
 * PreWeaponProficiency.java
 * Copyright 2001 (C) Bryan McRoberts <merton_monk@yahoo.com>
 * Copyright 2003 (C) Chris Ward <frugal@purplewombat.co.uk>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	   See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on November 28, 2003
 *
 * Current Ver: $Revision$
 * Last Editor: $Author$
 * Last Edited: $Date$
 *
 */
package plugin.pretokens.test;

import pcgen.core.*;
import pcgen.core.prereq.AbstractPrerequisiteTest;
import pcgen.core.prereq.Prerequisite;
import pcgen.core.prereq.PrerequisiteException;
import pcgen.core.prereq.PrerequisiteTest;
import pcgen.core.utils.CoreUtility;
import pcgen.util.PropertyFactory;

import java.util.Iterator;


/**
 * @author wardc
 *
 */
public class PreWeaponProfTester extends AbstractPrerequisiteTest implements PrerequisiteTest {

	/* (non-Javadoc)
	 * @see pcgen.core.prereq.PrerequisiteTest#passes(pcgen.core.PlayerCharacter)
	 */
	public int passes(final Prerequisite prereq, final PlayerCharacter character) throws PrerequisiteException {
		int runningTotal=0;

		final int number;
		try {
			number = Integer.parseInt(prereq.getOperand());
		}
		catch (NumberFormatException exceptn) {
			throw new PrerequisiteException(PropertyFactory.getFormattedString("PreFeat.error", prereq.toString())); //$NON-NLS-1$
		}

		final String aString = prereq.getKey();
		if ("DEITYWEAPON".equals(aString) && character.getDeity() != null) //$NON-NLS-1$
		{
			for (Iterator weapIter = CoreUtility.split(character.getDeity().getFavoredWeapon(), '|').iterator(); weapIter.hasNext();)
			{
				final String weaponKey = (String) weapIter.next();
				if (character.hasWeaponProfKeyed(weaponKey))
					runningTotal++;
			}
		}
		else if (aString.startsWith("TYPE.") || aString.startsWith("TYPE=")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			final String requiredType = aString.substring(5);
			for (Iterator e = character.getWeaponProfList().iterator(); e.hasNext();)
			{
				final String profKey = (String) e.next();
				final WeaponProf wp = Globals.getWeaponProfKeyed(profKey);
				if (wp == null)
				{
					continue;
				}
				if (wp.isType(requiredType))
				{
					runningTotal++;
				}
				else
				{
					final Equipment eq = EquipmentList.getEquipmentNamed(profKey);
					if (eq != null)
					{
						if (eq.isType(requiredType))
						{
							runningTotal++;
						}
					}
				}
			}
		}
		else
		{
			if (character.hasWeaponProfKeyed(aString))
				runningTotal++;
		}

		runningTotal = prereq.getOperator().compare(runningTotal, number);
		return countedTotal(prereq, runningTotal);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.prereq.PrerequisiteTest#kindsHandled()
	 */
	public String kindHandled() {
		return "WEAPONPROF"; //$NON-NLS-1$
	}

}
