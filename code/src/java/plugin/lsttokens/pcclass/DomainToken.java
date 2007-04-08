package plugin.lsttokens.pcclass;

import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import pcgen.base.lang.StringUtil;
import pcgen.cdom.base.CDOMReference;
import pcgen.cdom.base.CDOMSimpleSingleRef;
import pcgen.cdom.base.Constants;
import pcgen.cdom.base.PrereqObject;
import pcgen.cdom.graph.PCGraphEdge;
import pcgen.cdom.graph.PCGraphGrantsEdge;
import pcgen.core.Domain;
import pcgen.core.Globals;
import pcgen.core.PCClass;
import pcgen.core.PObject;
import pcgen.core.prereq.Prerequisite;
import pcgen.persistence.LoadContext;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.lst.AbstractToken;
import pcgen.persistence.lst.PCClassLstToken;
import pcgen.persistence.lst.PCClassUniversalLstToken;
import pcgen.persistence.lst.output.prereq.PrerequisiteWriter;
import pcgen.persistence.lst.prereq.PreParserFactory;
import pcgen.util.Logging;

/**
 * Class deals with DOMAIN Token
 */
public class DomainToken extends AbstractToken implements PCClassLstToken,
		PCClassUniversalLstToken
{

	@Override
	public String getTokenName()
	{
		return "DOMAIN";
	}

	public boolean parse(PCClass pcclass, String value, int level)
	{
		final StringTokenizer aTok = new StringTokenizer(value, Constants.PIPE);

		while (aTok.hasMoreTokens())
		{
			final String aString = aTok.nextToken();
			String domainKey;
			String prereq = null; // Do not initialize, null is significant!

			// Note: May contain PRExxx
			if (aString.indexOf("[") == -1)
			{
				domainKey = aString;
			}
			else
			{
				int openBracketLoc = aString.indexOf("[");
				domainKey = aString.substring(0, openBracketLoc);
				if (!aString.endsWith("]"))
				{
					Logging.errorPrint("Unresolved Prerequisite on Domain "
						+ aString + " in " + getTokenName());
				}
				prereq =
						aString.substring(openBracketLoc + 1, aString.length()
							- openBracketLoc - 2);
			}

			Domain thisDomain = Globals.getDomainKeyed(domainKey);

			if (thisDomain == null)
			{
				Logging.errorPrint("Unresolved Domain " + domainKey + " in "
					+ getTokenName());
			}
			else
			{
				Domain clonedDomain = thisDomain.clone();
				if (prereq != null)
				{
					try
					{
						clonedDomain.addPreReq(PreParserFactory.getInstance()
							.parse(prereq));
					}
					catch (PersistenceLayerException e)
					{
						Logging.errorPrint("Error generating Prerequisite "
							+ prereq + " in " + getTokenName());
					}
				}
				pcclass.addDomain(level, clonedDomain);
			}
		}

		return true;
	}

	public boolean parse(LoadContext context, PObject po, String value)
		throws PersistenceLayerException
	{
		if (value.length() == 0)
		{
			Logging.errorPrint(getTokenName() + " may not have empty argument");
			return false;
		}
		if (Constants.LST_DOT_CLEAR.equals(value))
		{
			context.graph.unlinkParentNodes(getTokenName(), po);
			return true;
		}

		if (value.charAt(0) == '|')
		{
			Logging.errorPrint(getTokenName()
				+ " arguments may not start with | : " + value);
			return false;
		}
		if (value.charAt(value.length() - 1) == '|')
		{
			Logging.errorPrint(getTokenName()
				+ " arguments may not end with | : " + value);
			return false;
		}
		if (value.indexOf("||") != -1)
		{
			Logging.errorPrint(getTokenName()
				+ " arguments uses double separator || : " + value);
			return false;
		}
		if (value.indexOf(",,") != -1)
		{
			Logging.errorPrint(getTokenName()
				+ " arguments uses double separator ,, : " + value);
			return false;
		}

		StringTokenizer pipeTok = new StringTokenizer(value, Constants.PIPE);

		while (pipeTok.hasMoreTokens())
		{
			// Note: May contain PRExxx
			String domainKey;
			Prerequisite prereq = null;

			int openBracketLoc = value.indexOf('[');
			if (openBracketLoc == -1)
			{
				domainKey = value;
			}
			else
			{
				if (value.indexOf(']') != value.length() - 1)
				{
					Logging.errorPrint("Invalid " + getTokenName()
						+ " must end with ']' if it contains a PREREQ tag");
					return false;
				}
				domainKey = value.substring(0, openBracketLoc);
				String prereqString =
						value.substring(openBracketLoc + 1, value.length() - 1);
				if (prereqString.length() == 0)
				{
					Logging.errorPrint(getTokenName()
						+ " cannot have empty prerequisite : " + value);
					return false;
				}
				prereq = getPrerequisite(prereqString);
			}
			CDOMSimpleSingleRef<Domain> domain =
					context.ref.getCDOMReference(Domain.class, domainKey);

			PCGraphGrantsEdge edge =
					context.graph.linkObjectIntoGraph(getTokenName(), po,
						domain);
			if (prereq != null)
			{
				edge.addPrerequisite(prereq);
			}
		}
		return true;
	}

	public String[] unparse(LoadContext context, PObject po)
	{
		Set<PCGraphEdge> domainEdges =
				context.graph.getParentLinksFromToken(getTokenName(), po,
					Domain.class);
		if (domainEdges.isEmpty())
		{
			return null;
		}
		Set<String> set = new TreeSet<String>();
		PrerequisiteWriter prereqWriter = new PrerequisiteWriter();
		for (PCGraphEdge edge : domainEdges)
		{
			List<PrereqObject> sourceNodes = edge.getSinkNodes();
			if (sourceNodes.size() != 1)
			{
				context.addWriteMessage("Outgoing Edge from " + po.getKey()
					+ " had more than one sink: " + sourceNodes);
				return null;
			}
			StringBuilder sb = new StringBuilder();
			sb.append(((CDOMReference<Domain>) sourceNodes.get(0))
				.getLSTformat());

			if (edge.hasPrerequisites())
			{
				List<Prerequisite> list = edge.getPrerequisiteList();
				if (list.size() > 1)
				{
					context.addWriteMessage("Incoming Edge to " + po.getKey()
						+ " had more than one " + "Prerequisite: "
						+ list.size());
					return null;
				}
				sb.append('[');
				StringWriter swriter = new StringWriter();
				try
				{
					prereqWriter.write(swriter, list.get(0));
				}
				catch (PersistenceLayerException e)
				{
					context.addWriteMessage("Error writing Prerequisite: " + e);
					return null;
				}
				sb.append(swriter.toString());
				sb.append(']');
			}
		}
		return new String[]{StringUtil.join(set, Constants.PIPE)};
	}
}