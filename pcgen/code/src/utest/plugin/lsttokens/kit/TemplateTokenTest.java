/*
 * Copyright (c) 2007 Tom Parker <thpr@users.sourceforge.net>
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
package plugin.lsttokens.kit;

import org.junit.Test;

import pcgen.core.PCTemplate;
import pcgen.core.kit.KitTemplate;
import pcgen.persistence.PersistenceLayerException;
import pcgen.rules.persistence.CDOMSubLineLoader;
import pcgen.rules.persistence.token.CDOMPrimaryToken;
import plugin.lsttokens.testsupport.AbstractKitTokenTestCase;

public class TemplateTokenTest extends AbstractKitTokenTestCase<KitTemplate>
{

	static TemplateToken token = new TemplateToken();
	static CDOMSubLineLoader<KitTemplate> loader = new CDOMSubLineLoader<KitTemplate>(
			"PCTemplateS", KitTemplate.class);

	@Override
	public Class<KitTemplate> getCDOMClass()
	{
		return KitTemplate.class;
	}

	@Override
	public CDOMSubLineLoader<KitTemplate> getLoader()
	{
		return loader;
	}

	@Override
	public CDOMPrimaryToken<KitTemplate> getToken()
	{
		return token;
	}

	@Test
	public void testRoundRobinSimple() throws PersistenceLayerException
	{
		primaryContext.ref.constructCDOMObject(PCTemplate.class, "Fireball");
		secondaryContext.ref.constructCDOMObject(PCTemplate.class, "Fireball");
		runRoundRobin("Fireball");
	}

	@Test
	public void testRoundRobinSub() throws PersistenceLayerException
	{
		primaryContext.ref.constructCDOMObject(PCTemplate.class, "Fireball");
		secondaryContext.ref.constructCDOMObject(PCTemplate.class, "Fireball");
		primaryContext.ref
				.constructCDOMObject(PCTemplate.class, "EnhancedFeat");
		secondaryContext.ref.constructCDOMObject(PCTemplate.class,
				"EnhancedFeat");
		runRoundRobin("Fireball[TEMPLATE:EnhancedFeat]");
	}
}
