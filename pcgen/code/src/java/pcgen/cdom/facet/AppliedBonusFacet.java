/*
 * Copyright (c) 2010 Tom Parker <thpr@users.sourceforge.net>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package pcgen.cdom.facet;

import java.util.List;

import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.enumeration.CharID;
import pcgen.cdom.enumeration.ListKey;
import pcgen.cdom.facet.base.AbstractListFacet;
import pcgen.cdom.facet.event.DataFacetChangeEvent;
import pcgen.cdom.facet.event.DataFacetChangeListener;
import pcgen.cdom.facet.model.RaceFacet;
import pcgen.core.bonus.BonusObj;

/**
 * AppliedBonusFacet tracks the active BonusObj objects on a Player Character.
 * 
 * @author Tom Parker (thpr [at] yahoo.com)
 */
public class AppliedBonusFacet extends AbstractListFacet<BonusObj> implements
		DataFacetChangeListener<CDOMObject>
{
	private AddedBonusFacet addedBonusFacet;

	private PrerequisiteFacet prerequisiteFacet;

	private RaceFacet raceFacet;

	/**
	 * Adds to the Player Character the appropriate BonusObj objects based on
	 * the BONUS: token in the CDOMObject or applied due to a bonus side effect.
	 * 
	 * Triggered when one of the Facets to which AppliedBonusFacet listens fires
	 * a DataFacetChangeEvent to indicate a CDOMObject was added to a Player
	 * Character.
	 * 
	 * @param dfce
	 *            The DataFacetChangeEvent containing the information about the
	 *            change
	 * 
	 * @see pcgen.cdom.facet.event.DataFacetChangeListener#dataAdded(pcgen.cdom.facet.event.DataFacetChangeEvent)
	 */
	@Override
	public void dataAdded(DataFacetChangeEvent<CDOMObject> dfce)
	{
		CharID id = dfce.getCharID();
		CDOMObject cdo = dfce.getCDOMObject();
		processAdd(id, cdo, cdo.getSafeListFor(ListKey.BONUS));
		processAdd(id, cdo, addedBonusFacet.getSet(id, cdo));
	}

	private void processAdd(CharID id, CDOMObject cdo,
			List<? extends BonusObj> bonusList)
	{
		for (BonusObj bonus : bonusList)
		{
			if (prerequisiteFacet.qualifies(id, bonus, cdo))
			{
				add(id, bonus);
			}
			else
			{
				// TODO Is this necessary? Shouldn't be present anyway...
				remove(id, bonus);
			}
		}
	}

	@Override
	public void dataRemoved(DataFacetChangeEvent<CDOMObject> dfce)
	{
		CharID id = dfce.getCharID();
		CDOMObject cdo = dfce.getCDOMObject();
		processRemove(id, cdo, cdo.getSafeListFor(ListKey.BONUS));
		processRemove(id, cdo, addedBonusFacet.getSet(id, cdo));
	}

	private void processRemove(CharID id, CDOMObject cdo,
			List<? extends BonusObj> bonusList)
	{
		for (BonusObj bonus : bonusList)
		{
			remove(id, bonus);
		}
	}

	public void setAddedBonusFacet(AddedBonusFacet addedBonusFacet)
	{
		this.addedBonusFacet = addedBonusFacet;
	}

	public void setPrerequisiteFacet(PrerequisiteFacet prerequisiteFacet)
	{
		this.prerequisiteFacet = prerequisiteFacet;
	}

	public void setRaceFacet(RaceFacet raceFacet)
	{
		this.raceFacet = raceFacet;
	}

	public void init()
	{
		raceFacet.addDataFacetChangeListener(this);
	}
}
