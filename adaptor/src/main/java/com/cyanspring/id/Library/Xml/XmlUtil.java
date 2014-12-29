package com.cyanspring.id.Library.Xml;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.cyanspring.id.Library.Util.LogUtil;
import com.google.common.base.Strings;

/**
 * Provide helper functions for xml parsing.
 */
public class XmlUtil {
	// -----------------------------------------------------------------------------
	// Parsing related API
	// -----------------------------------------------------------------------------
	private static final Logger log = LoggerFactory.getLogger(XmlUtil.class);
	private XmlUtil() {
	}

	
	/**
	 * Convert Document to String
	 * @param doc
	 * @return String 
	 */
	public static String toXml(Document doc) {
	    try {
	        StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
	        return sw.toString();
	    } catch (Exception ex) {
	        throw new RuntimeException("Error converting to String", ex);
	    }
	}

	/**
	 * Convert Element to String
	 * @param elm
	 * @return
	 */
	public static String toXml(Element elm) {
	    try {
	        StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	        transformer.transform(new DOMSource(elm), new StreamResult(sw));
	        return sw.toString();
	    } catch (Exception ex) {
	        throw new RuntimeException("Error converting to String", ex);
	    }
	}
	
	/**
	 * Parse a file and return a XML Document object
	 * 
	 * @param strFilename
	 *            filename of the xml document
	 * @return the parsed DOM document
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static Document parse(String strFilename) throws IOException,
			SAXException, ParserConfigurationException {
		FileReader reader = null;

		try {
			reader = new FileReader(strFilename);
			DocumentBuilder db = getDocumentBuilder(false);

			return db.parse(new InputSource(reader));
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	/**
	 * Parse a file and return a XML Document object
	 * 
	 * @param file
	 * @return
	 */
	public static Document parse(File file)
	{
		Document doc = null;
		// if (!file.exists())
		// return null;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			LogUtil.logException(log, e);
		}		
		return doc;
		
	}

	/**
	 * Parse a string and return a XML document object
	 * 
	 * @param strXml
	 *            the XML string
	 * @return the parsed DOM document
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static Document parseXml(String strXml) throws SAXException,
			IOException, ParserConfigurationException {
		DocumentBuilder db = getDocumentBuilder(false);

		return db.parse(new InputSource(new StringReader(strXml)));
	}

	// -----------------------------------------------------------------------------
	// XPath related API (selectSingleNode, selectNodes)
	// -----------------------------------------------------------------------------

	/**
	 * Select the first node that matches the given XPath expression.
	 * 
	 * @param nodeContext
	 *            The DOM node where the xpath is to be evaluate.
	 * @param strXPath
	 *            The XPath expression
	 * @return the first node that matches the given XPath expression, or null
	 *         if none matches.
	 * @throws XPathExpressionException
	 */
	public static Node selectSingleNode(Node nodeContext, String strXPath)
			throws XPathExpressionException {
		return selectSingleNode(nodeContext, strXPath, nodeContext);
	}

	/**
	 * Select the first node that matches the given XPath expression, taking
	 * into account all namespace found in the given node.
	 * 
	 * @param nodeContext
	 *            The DOM node where the xpath is to be evaluate.
	 * @param strXPath
	 *            The XPath expression
	 * @param nodeNamespace
	 *            the node from which all the namespace declarations will be
	 *            taken.
	 * @return the first node that matches the given XPath expression, or null
	 *         if none matches.
	 * @throws XPathExpressionException
	 */
	public static Node selectSingleNode(Node nodeContext, String strXPath,
			Node nodeNamespace) throws XPathExpressionException {
		NamespaceContext nsContext = new NodeNamespaceContext(nodeNamespace);

		return selectSingleNode(nodeContext, strXPath, nsContext);
	}

	/**
	 * Select the first node that matches the given XPath expression, taking
	 * into account all namespaces mapping defined in namespaces.
	 * 
	 * @param nodeContext
	 *            The DOM node where the xpath is to be evaluate.
	 * @param strXPath
	 *            The XPath expression
	 * @param namespaces
	 *            a mapping between namespace prefixes and URIs
	 * @return the first node that matches the given XPath expression, or null
	 *         if none matches.
	 * @throws XPathExpressionException
	 */
	public static Node selectSingleNode(Node nodeContext, String strXPath,
			Map<String, String> namespaces) throws XPathExpressionException {
		NamespaceContext nsContext = new NodeNamespaceContext(nodeContext,
				namespaces);

		return selectSingleNode(nodeContext, strXPath, nsContext);
	}

	/**
	 * Select the first node that matches the given XPath expression. The
	 * namespace mapping is defined by nsContext.
	 * 
	 * @param nodeContext
	 *            The DOM node where the xpath is to be evaluate.
	 * @param strXPath
	 *            The XPath expression
	 * @param nsContext
	 *            Implementation of NamespaceContext.
	 * @return the first node that matches the given XPath expression, or null
	 *         if none matches.
	 * @throws XPathExpressionException
	 */
	public static Node selectSingleNode(Node nodeContext, String strXPath,
			NamespaceContext nsContext) throws XPathExpressionException {
		NodeList nodes = selectNodes(nodeContext, strXPath, nsContext);
		if (nodes == null || nodes.getLength() == 0)
			return null;

		return nodes.item(0);
	}

	/**
	 * Select all nodes that matches the given XPath expression.
	 * 
	 * @param nodeContext
	 *            The DOM node where the xpath is to be evaluate.
	 * @param strXPath
	 *            The XPath expression
	 * @return all the nodes that match the given XPath expression
	 */
	public static NodeList selectNodes(Node nodeContext, String strXPath)
			throws XPathException {
		return selectNodes(nodeContext, strXPath, nodeContext);
	}

	/**
	 * Select all nodes that matches the given XPath expression, taking into
	 * account all namespace found in the given node.
	 * 
	 * @param nodeContext
	 *            The DOM node where the xpath is to be evaluate.
	 * @param strXPath
	 *            The XPath expression
	 * @param nodeNamespace
	 *            the node from which all the namespace declarations will be
	 *            taken.
	 * @return all the nodes that match the given XPath expression
	 * @throws XPathExpressionException
	 */
	public static NodeList selectNodes(Node nodeContext, String strXPath,
			Node nodeNamespace) throws XPathExpressionException {
		NamespaceContext nsContext = new NodeNamespaceContext(nodeNamespace);

		return selectNodes(nodeContext, strXPath, nsContext);
	}

	/**
	 * Select all nodes that matches the given XPath expression, taking into
	 * account all namespaces mapping defined in namespaces.
	 * 
	 * @param nodeContext
	 *            The DOM node where the xpath is to be evaluate.
	 * @param strXPath
	 *            The XPath expression
	 * @param namespaces
	 *            a mapping between namespace prefixes and URIs
	 * @return all the nodes that match the given XPath expression
	 * @throws XPathExpressionException
	 */
	public static NodeList selectNodes(Node nodeContext, String strXPath,
			Map<String, String> namespaces) throws XPathExpressionException {
		NamespaceContext nsContext = new NodeNamespaceContext(nodeContext,
				namespaces);

		return selectNodes(nodeContext, strXPath, nsContext);
	}

	/**
	 * Select all nodes that matches the given XPath expression. The namespace
	 * mapping is defined by nsContext.
	 * 
	 * @param nodeContext
	 *            The DOM node where the xpath is to be evaluate.
	 * @param strXPath
	 *            The XPath expression
	 * @param nsContext
	 *            Implementation of NamespaceContext.
	 * @return all the nodes that match the given XPath expression
	 * @throws XPathExpressionException
	 */
	public static NodeList selectNodes(Node nodeContext, String strXPath,
			NamespaceContext nsContext) throws XPathExpressionException {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		xpath.setNamespaceContext(nsContext);

		XPathExpression xpathExpr = xpath.compile(strXPath);

		NodeList nodes = (NodeList) xpathExpr.evaluate(nodeContext,
				XPathConstants.NODESET);

		return nodes;
	}

	/**
	 * Convert a NodeList to a List<Node>
	 * 
	 * @param nodeList
	 * @return
	 */
	public static List<Node> asListOfNodes(NodeList nodeList) {
		int listLength = nodeList.getLength();
		List<Node> list = new ArrayList<Node>(listLength);

		for (int i = 0; i < listLength; i++) {
			Node node = nodeList.item(i);
			list.add(node);
		}

		return list;
	}

	// -----------------------------------------------------------------------------
	// Nodes & Attribute related API
	// -----------------------------------------------------------------------------

	/**
	 * Return the value of an attribute. If the attribute does not exist, "" is
	 * returned instead.
	 * 
	 * @param nodeParent
	 *            The parent node.
	 * @param strAttrName
	 *            Name of the attribute.
	 * @return attribute value, or "" if attribute does not exist.
	 */
	public static String getAttribute(Element nodeParent, String strAttrName) {
		return nodeParent.getAttribute(strAttrName);
	}

	/**
	 * Return the value of an attribute as integer. If the attribute does not
	 * exist, or cannot be converted to an integer, then a default value is
	 * returned.
	 * 
	 * @param nodeParent
	 *            The parent node.
	 * @param strAttrName
	 *            Name of the attribute.
	 * @param defaultValue
	 *            default value to return if attribute does not exist or cannot
	 *            be converted to integer.
	 * @return
	 */
	public static int getIntAttribute(Element nodeParent, String strAttrName,
			int defaultValue) {
		String strValue = nodeParent.getAttribute(strAttrName);
		if (strValue.isEmpty())
			return defaultValue;

		try {
			return Integer.parseInt(strValue);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Return the value of an attribute as double. If the attribute does not
	 * exist, or cannot be converted to an integer, then a default value is
	 * returned.
	 * 
	 * @param nodeParent
	 *            The parent node.
	 * @param strAttrName
	 *            Name of the attribute.
	 * @param defaultValue
	 *            default value to return if attribute does not exist or cannot
	 *            be converted to double.
	 * @return
	 */
	public static double getDoubleAttribute(Element nodeParent,
			String strAttrName, double defaultValue) {
		String strValue = nodeParent.getAttribute(strAttrName);
		if (strValue.isEmpty())
			return defaultValue;

		try {
			return Double.parseDouble(strValue);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Return the value of an attribute as Date. If the attribute does not
	 * exist, or cannot be converted to an integer, then a default value is
	 * returned. Use strDateFormat to specify Date convert format.
	 * 
	 * @param nodeParent
	 *            The parent node.
	 * @param strAttrName
	 *            Name of the attribute.
	 * @param strDateFormat
	 *            Date format for value conversion.
	 * @param defaultValue
	 *            default value to return if attribute does not exist or cannot
	 *            be converted to Date.
	 * @return
	 */
	public static Date getDateAttribute(Element nodeParent, String strAttrName,
			String strDateFormat, Date defaultValue) {
		String strValue = nodeParent.getAttribute(strAttrName);
		if (strValue.isEmpty())
			return defaultValue;

		try {
			SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
			return sdf.parse(strValue);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Return the text value of a child node.
	 * 
	 * @param nodeParent
	 *            The parent node.
	 * @param strnNodeName
	 *            Name of the child node.
	 * @return
	 */
	public static String getNodeValue(Element nodeParent, String strnNodeName) {
		Node node = getFirstChildNode(nodeParent, strnNodeName);
		if (node == null)
			return "";

		return getNodeTextValue(node);
	}

	/**
	 * Return the text value of a child node.
	 * @param nodeParent:  The parent node.
	 * @param strnNodeName: Name of the child node.
	 * @param strDefault: default value
	 * @return 
	 */
	public static String getNodeValue(Element nodeParent, String strnNodeName, String strDefault) {
		Node node = getFirstChildNode(nodeParent, strnNodeName);
		if (node == null)
			return strDefault;

		return getNodeTextValue(node);
	}
	
	/**
	 * Return the value of a child node as integer.
	 * 
	 * @param nodeParent
	 *            The parent node.
	 * @param strNodeName
	 *            Name of the child node.
	 * @param defaultValue
	 *            default value to return if attribute does not exist or cannot
	 *            be converted to integer.
	 * @return
	 */
	public static int getNodeIntValue(Element nodeParent, String strNodeName,
			int defaultValue) {
		String strValue = getNodeValue(nodeParent, strNodeName);
		if (strValue.isEmpty())
			return defaultValue;

		try {
			return Integer.parseInt(strValue);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Return the value of a child node as double.
	 * 
	 * @param nodeParent
	 *            The parent node.
	 * @param strNodeName
	 *            Name of the child node.
	 * @param defaultValue
	 *            default value to return if attribute does not exist or cannot
	 *            be converted to double.
	 * @return
	 */
	public static double getNodeDoubleValue(Element nodeParent,
			String strNodeName, double defaultValue) {
		String strValue = getNodeValue(nodeParent, strNodeName);
		if (strValue.isEmpty())
			return defaultValue;

		try {
			return Double.parseDouble(strValue);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Return the value of a child node as Date.
	 * 
	 * @param nodeParent
	 *            The parent node.
	 * @param strNodeName
	 *            Name of the child node.
	 * @param strDateFormat
	 *            Date format for value conversion.
	 * @param defaultValue
	 *            default value to return if attribute does not exist or cannot
	 *            be converted to Date.
	 * @return
	 */
	public static Date getNodeDateValue(Element nodeParent, String strNodeName,
			String strDateFormat, Date defaultValue) {
		String strValue = getNodeValue(nodeParent, strNodeName);
		if (strValue.isEmpty())
			return defaultValue;

		try {
			SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
			return sdf.parse(strValue);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Return the first child node that matches the given strTagName.
	 * 
	 * @param nodeParent
	 *            The parent node for the match.
	 * @param strTagName
	 *            The name of the xml tag.
	 * @return The first child node with the given tag name, or null if none
	 *         found.
	 */
	public static Node getFirstChildNode(Element nodeParent, String strTagName) {
		NodeList nodeList = nodeParent.getElementsByTagName(strTagName);
		if (nodeList == null || nodeList.getLength() == 0)
			return null;

		return nodeList.item(0);
	}

	/**
	 * Return the text value of an 'element' node. For example <Node>x</Node>
	 * should return "x"
	 * 
	 * @param node
	 * @return
	 */
	public static String getNodeTextValue(Node node) {
		if (node.hasChildNodes())
			return node.getFirstChild().getNodeValue();
		else
			return node.getTextContent();
	}

	/**
	 * Return the OuterXml (similar to .Net XML API) of the given node. from
	 * http://chicknet.blogspot.com/2007/05/outerxml-for-java.html
	 * 
	 * @param node
	 * @return
	 */
	public static String getOuterXml(Node node) {
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty("omit-xml-declaration", "yes");

			StringWriter writer = new StringWriter();
			transformer
					.transform(new DOMSource(node), new StreamResult(writer));
			return writer.toString();
		} catch (Exception e) {
			return "";
		}
	}

	// -----------------------------------------------------------------------------
	// Encoding related API
	// -----------------------------------------------------------------------------

	/**
	 * Perform xml encoding: replacing special character.
	 * 
	 * @param strText
	 * @return
	 */
	public static String xmlEncode(String strText) {
		if (Strings.isNullOrEmpty(strText))
			return "";

		StringBuilder buf = new StringBuilder(strText.length());
		boolean wasEscaped = false;

		for (int ii = 0; ii < strText.length(); ii++) {
			char c = strText.charAt(ii);
			switch (c) {
			case '&':
				buf.append("&amp;");
				wasEscaped = true;
				break;
			case '<':
				buf.append("&lt;");
				wasEscaped = true;
				break;
			case '>':
				buf.append("&gt;");
				wasEscaped = true;
				break;
			case '\'':
				buf.append("&apos;");
				wasEscaped = true;
				break;
			case '"':
				buf.append("&quot;");
				wasEscaped = true;
				break;
			default:
				if (!isLegal(c)) {
					// remove illegal character
					wasEscaped = true;
				} else {
					buf.append(c);
				}
				break;
			}
		}

		return wasEscaped ? buf.toString() : strText;
	}

	/**
	 * Determines whether the passed string contains any illegal characters, per
	 * section 2.2 of the XML spec. This is a rare occurrence, typically limited
	 * to strings that contain ASCII control characters. The Xerces parser will
	 * reject such input, even if escaped as an entity. However, the Xalan
	 * transformer will happily generate such entities.
	 * <p>
	 * Note: at present, marks characters from the UTF-16 "surrogate blocks" as
	 * illegal. The XML specification allows code points from the "higher
	 * planes" of Unicde, but disallows the surrogate blocks used to contruct
	 * code points in these planes. Rather than allow code points from the
	 * surrogate block, and get hurt by a bozo transformer that doesn't know
	 * that Java strings are UTF-16, I took the conservative (and wrong)
	 * approach. On the other hand, if you're using characters from outside the
	 * BMP, you probably don't have ASCII control characters in your text, and
	 * don't need this method at all.
	 *
	 * @return true if this string does <em>not</em> contain any illegal
	 *         characters.
	 */
	public static boolean isLegal(String s) {
		for (int ii = 0; ii < s.length(); ii++) {
			if (!isLegal(s.charAt(ii)))
				return false;
		}
		return true;
	}

	/**
	 * Removes all illegal characters from the passed string. If the string does
	 * not contain illegal characters, returns it unchanged.
	 */
	public static String stripIllegals(String s) {
		StringBuilder buf = null;
		for (int ii = 0; ii < s.length(); ii++) {
			char c = s.charAt(ii);
			if (!isLegal(c)) {
				if (buf == null) {
					buf = new StringBuilder(s.length());
					buf.append(s.substring(0, ii));
				}
			} else if (buf != null)
				buf.append(c);
		}
		return (buf != null) ? buf.toString() : s;
	}

	// used by isLegal(char)
	private final static boolean[] LEGAL_CONTROL_CHARS = new boolean[] { false,
			false, false, false, false, false, false, false, false, true, true,
			false, false, true, false, false, false, false, false, false,
			false, false, false, false, false, false, false, false, false,
			false, false, false };

	/**
	 * Does the actual work of {@link isLegal(String)}.
	 */
	private static boolean isLegal(char c) {
		// based on http://en.wikipedia.org/wiki/Valid_characters_in_XML
		//
		if (c < '\u0020')
			return LEGAL_CONTROL_CHARS[c];

		else if (c >= '\u007f' && c < '\u00a0')
			return false; // valid, but highly discourage

		else if (c >= '\u0020' && c <= '\ud7ff')
			return true;

		else if (c >= '\ue000' && c <= '\ufffd')
			return true;

		else
			return false;

		/*
		 * from Roger's original XmlHelper
		 * 
		 * if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c
		 * < '\u2100')) return false;
		 */

		/*
		 * from PracticalXml
		 * 
		 * if (c < '\ud800') { return (c < '\u0020') ? LEGAL_CONTROL_CHARS[c] :
		 * true; } return (c >= '\ue000');
		 */
	}

	/**
	 * Create a DocumentBuilder object
	 * 
	 * @param validate
	 *            If true, will validate by DTD.
	 * @return
	 * @throws ParserConfigurationException
	 */
	private static DocumentBuilder getDocumentBuilder(Boolean validate)
			throws ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setCoalescing(true);
		dbf.setValidating(validate);
		return dbf.newDocumentBuilder();
	}

}
