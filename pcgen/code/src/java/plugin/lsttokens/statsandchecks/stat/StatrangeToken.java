/*
 * Copyright (c) 2009 Tom Parker <thpr@users.sourceforge.net>
 *
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
package plugin.lsttokens.statsandchecks.stat;

import java.util.StringTokenizer;

import pcgen.cdom.enumeration.IntegerKey;
import pcgen.core.PCStat;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.token.CDOMPrimaryToken;
import pcgen.rules.persistence.token.ParseResult;

/**
 * Class deals with STATRANGE Token
 */
public class StatrangeToken implements CDOMPrimaryToken<PCStat>
{

	@Override
	public String getTokenName()
	{
		return "STATRANGE";
	}

	@Override
	public ParseResult parseToken(LoadContext context, PCStat stat, String value)
	{
		final StringTokenizer aTok = new StringTokenizer(value, "|", false);

		if (aTok.countTokens() == 2)
		{
			try
			{
				context.obj.put(stat, IntegerKey.MIN_VALUE, Integer
						.valueOf(aTok.nextToken()));
				context.obj.put(stat, IntegerKey.MAX_VALUE, Integer
						.valueOf(aTok.nextToken()));
				return ParseResult.SUCCESS;
			}
			catch (NumberFormatException ignore)
			{
				return new ParseResult.Fail("Error in specified Stat range, "
						+ "expected two comma separated integers, found: "
						+ value, context);
			}
		}
		else
		{
			return new ParseResult.Fail("Error in specified Stat range, "
					+ "expected two comma separated integers, found "
					+ aTok.countTokens() + " values in: " + value, context);
		}
	}

	@Override
	public String[] unparse(LoadContext context, PCStat stat)
	{
		Integer min = context.getObjectContext().getInteger(stat,
				IntegerKey.MIN_VALUE);
		Integer max = context.getObjectContext().getInteger(stat,
				IntegerKey.MAX_VALUE);
		if (min == null && max == null)
		{
			return null;
		}
		if (min == null || max == null)
		{
			context.addWriteMessage("Must have both min and max in "
					+ getTokenName() + ": " + min + " " + max);
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(min).append(',').append(max);
		return new String[] { sb.toString() };
	}

	@Override
	public Class<PCStat> getTokenClass()
	{
		return PCStat.class;
	}
}
