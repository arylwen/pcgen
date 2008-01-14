/*
 * PrePCLevelTester.java
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
 * Created on 2007-12-07
 *
 */
package plugin.pretokens.test;

import pcgen.core.PlayerCharacter;
import pcgen.core.prereq.AbstractPrerequisiteTest;
import pcgen.core.prereq.Prerequisite;
import pcgen.core.prereq.PrerequisiteException;
import pcgen.core.prereq.PrerequisiteTest;

/**
 * @author jfrazierjr
 *
 */
public class PrePCLevelTester extends AbstractPrerequisiteTest implements
		PrerequisiteTest
{

	/* (non-Javadoc)
	 * @see pcgen.core.prereq.PrerequisiteTest#passes(pcgen.core.PlayerCharacter)
	 */
	@Override
	public int passes(final Prerequisite prereq, final PlayerCharacter character)
	{

		final int requiredLevel = Integer.parseInt(prereq.getOperand());
		final int runningTotal =
				prereq.getOperator().compare(character.getTotalPlayerLevels(),
					requiredLevel);
		return countedTotal(prereq, runningTotal);
	}

	/* (non-Javadoc)
	 * @see pcgen.core.prereq.PrerequisiteTest#kindsHandled()
	 */
	public String kindHandled()
	{
		return "PCLEVEL"; //$NON-NLS-1$
	}

	public int passesCDOM(Prerequisite prereq, PlayerCharacter character)
			throws PrerequisiteException
	{
		int requiredLevel = Integer.parseInt(prereq.getOperand());
		int levels = character.getTotalCDOMPlayerLevels();
		int runningTotal = prereq.getOperator().compare(levels, requiredLevel);
		return countedTotal(prereq, runningTotal);
	}

}
