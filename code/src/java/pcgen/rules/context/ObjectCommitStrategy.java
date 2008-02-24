/*
 * Copyright 2007 (C) Tom Parker <thpr@users.sourceforge.net>
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package pcgen.rules.context;

import java.net.URI;
import java.util.Set;

import pcgen.base.formula.Formula;
import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.base.ConcretePrereqObject;
import pcgen.cdom.enumeration.FormulaKey;
import pcgen.cdom.enumeration.IntegerKey;
import pcgen.cdom.enumeration.ListKey;
import pcgen.cdom.enumeration.ObjectKey;
import pcgen.cdom.enumeration.StringKey;
import pcgen.cdom.enumeration.VariableKey;
import pcgen.core.prereq.Prerequisite;

public interface ObjectCommitStrategy
{

	public void put(CDOMObject cdo, StringKey sk, String s);

	public <T> void put(CDOMObject cdo, ObjectKey<T> sk, T s);

	public void put(CDOMObject cdo, IntegerKey ik, Integer i);

	public void put(CDOMObject cdo, FormulaKey fk, Formula f);

	public void put(CDOMObject obj, VariableKey vk, Formula f);

	public <T> void addToList(CDOMObject cdo, ListKey<T> key, T value);

	public void removeList(CDOMObject cdo, ListKey<?> lk);

	public <T> void removeFromList(CDOMObject cdo, ListKey<T> lk, T val);

	public String getString(CDOMObject cdo, StringKey sk);

	public Integer getInteger(CDOMObject cdo, IntegerKey ik);

	public Formula getFormula(CDOMObject cdo, FormulaKey fk);

	public Formula getVariable(CDOMObject obj, VariableKey key);

	public Set<VariableKey> getVariableKeys(CDOMObject obj);

	public <T> T getObject(CDOMObject cdo, ObjectKey<T> ik);

	public <T> Changes<T> getListChanges(CDOMObject cdo, ListKey<T> lk);

	public void setExtractURI(URI extractURI);

	public void setSourceURI(URI sourceURI);

	public void put(ConcretePrereqObject cpo, Prerequisite p);

	public Changes<Prerequisite> getPrerequisiteChanges(ConcretePrereqObject obj);
}
