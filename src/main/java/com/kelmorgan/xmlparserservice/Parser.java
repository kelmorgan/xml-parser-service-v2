package com.kelmorgan.xmlparserservice;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Parser {
    private String parseString;
    private String copyString;
    private int indexOfPrevSrc;

    public Parser() {
    }

    public Parser(String parseThisString) {
        setInputXML(parseThisString);
    }

    public void setInputXML(String parseThisString) {
        if (parseThisString != null) {
            this.copyString = parseThisString;
            this.parseString = parseThisString.toUpperCase();
        } else {
            this.parseString = null;
            this.copyString = null;
        }
        this.indexOfPrevSrc = 0;
    }

    public Document convertStringToDocument(String xmlStr) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlStr)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getServiceName() {
        return getValueBetweenTags("Option");
    }

    public String getServiceName(char chr) {
        if (chr == 'A') {
            return getValueBetweenTags("AdminOption");
        } else {
            return "NoServiceFound";
        }
    }

    public boolean validateXML() {
        return parseString != null && parseString.contains("<?XML VERSION=\"1.0\"?>");
    }

    public String getValueOf(String tag) {
        return getValueBetweenTags(tag);
    }

    public String getValueOf(String tag, String type) {
        if ("Binary".equalsIgnoreCase(type)) {
            return getValueBetweenBinaryTags(tag);
        } else {
            return "";
        }
    }

    public String getValueOf(String tag, boolean fromLast) {
        return getValueBetweenTags(tag, fromLast);
    }

    public String getValueOf(String tag, int start, int end) {
        return getValueBetweenTags(tag, start, end);
    }

    public int getStartIndex(String tag, int start, int end) {
        return getIndexOfTag(tag, start, end, true);
    }

    public int getEndIndex(String tag, int start, int end) {
        return getIndexOfTag(tag, start, end, false);
    }

    public int getTagStartIndex(String tag, int start, int end) {
        return getIndexOfTag(tag, start, end, true);
    }

    public int getTagEndIndex(String tag, int start, int end) {
        return getIndexOfTag(tag, start, end, false);
    }

    public String getFirstValueOf(String tag) {
        indexOfPrevSrc = parseString.indexOf("<" + tag.toUpperCase() + ">");
        return copyString.substring(indexOfPrevSrc + tag.length() + 2,
                parseString.indexOf("</" + tag.toUpperCase() + ">"));
    }

    public String getFirstValueOf(String tag, int start) {
        indexOfPrevSrc = parseString.indexOf("<" + tag.toUpperCase() + ">", start);
        return copyString.substring(indexOfPrevSrc + tag.length() + 2,
                parseString.indexOf("</" + tag.toUpperCase() + ">", start));
    }

    public String getNextValueOf(String tag) {
        indexOfPrevSrc = parseString.indexOf("<" + tag.toUpperCase() + ">", indexOfPrevSrc + tag.length() + 2);
        return copyString.substring(indexOfPrevSrc + tag.length() + 2,
                parseString.indexOf("</" + tag.toUpperCase() + ">", indexOfPrevSrc));
    }

    public int getNoOfFields(String tag) {
        return getNoOfFields(tag, 0, 0);
    }

    public int getNoOfFields(String tag, int startPos, int endPos) {
        int noOfFields = 0;
        int beginPos = startPos;
        String upperTag = tag.toUpperCase() + ">";
        try {
            while (beginPos >= 0 && (beginPos < endPos || endPos == 0)) {
                beginPos = parseString.indexOf("<" + upperTag, beginPos);
                if (beginPos == -1) break;
                beginPos = parseString.indexOf("</" + upperTag, beginPos) + upperTag.length();
                noOfFields++;
            }
        } catch (StringIndexOutOfBoundsException ignored) {
        }
        return noOfFields;
    }

    public String convertToSQLString(String strName) {
        if (strName == null) return "";
        return strName.replace("[", "[[]")
                .replace("_", "[_]")
                .replace("%", "[%]")
                .replace('?', '_');
    }

    public String getValueOf(String tag, String type, int from, int end) {
        if ("Binary".equalsIgnoreCase(type)) {
            int startPos = copyString.indexOf("<" + tag + ">", from);
            if (startPos == -1) return "";
            int endPos = copyString.indexOf("</" + tag + ">", from);
            if (endPos > end) return "";
            return copyString.substring(startPos + tag.length() + 2, endPos);
        } else {
            return "";
        }
    }

    public String toUpperCase(String value) {
        return value != null ? value.toUpperCase() : "";
    }

    public String changeValue(String xmlString, String tagName, String newValue) {
        if (xmlString == null || tagName == null || newValue == null) return "";
        String upperTag = tagName.toUpperCase();
        String upperXmlString = xmlString.toUpperCase();
        int startIndex = upperXmlString.indexOf("<" + upperTag + ">");
        if (startIndex == -1) return xmlString;
        startIndex += upperTag.length() + 2;
        int endIndex = upperXmlString.indexOf("</" + upperTag + ">", startIndex);
        if (endIndex == -1) return xmlString;
        return xmlString.substring(0, startIndex) + newValue + xmlString.substring(endIndex);
    }

    public void changeValue(String tagName, String newValue) {
        if (tagName == null || newValue == null) return;
        String upperTag = tagName.toUpperCase();
        int startIndex = parseString.indexOf("<" + upperTag + ">");
        if (startIndex != -1) {
            startIndex += upperTag.length() + 2;
            int endIndex = parseString.indexOf("</" + upperTag + ">", startIndex);
            copyString = copyString.substring(0, startIndex) + newValue + copyString.substring(endIndex);
        } else {
            int endIndex = parseString.lastIndexOf("</");
            copyString = copyString.substring(0, endIndex) + "<" + tagName + ">" + newValue + "</" + tagName + ">" + copyString.substring(endIndex);
        }
        parseString = copyString.toUpperCase();
    }

    public Set<Map<String, String>> getXMLData(String xmlInput, String tag) {
        Set<Map<String, String>> tagSet = new HashSet<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlInput)));
            NodeList nodeList = doc.getElementsByTagName(tag);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList childNodes = element.getElementsByTagName("*");
                    Map<String, String> tagMap = new LinkedHashMap<>();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        String nodeName = childNodes.item(j).getNodeName();
                        tagMap.put(nodeName, getTagValues(nodeName, element));
                    }
                    tagSet.add(tagMap);
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return tagSet;
    }

    public String getTagValues(String tagName, Element element) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            NodeList subList = nodeList.item(0).getChildNodes();
            if (subList.getLength() > 0) {
                return subList.item(0).getNodeValue();
            }
        }
        return "";
    }

    public String convertStringToUpperCase(String value) {
        return toUpperCase(value);
    }

    private String getValueBetweenTags(String tag) {
        return getValueBetweenTags(tag, false);
    }

    private String getValueBetweenTags(String tag, boolean fromLast) {
        String upperTag = tag.toUpperCase();
        int start = parseString.indexOf("<" + upperTag + ">");
        if (start == -1) return "";
        start += upperTag.length() + 2;
        int end = parseString.indexOf("</" + upperTag + ">", start);
        return (end != -1) ? copyString.substring(start, end) : "";
    }

    private String getValueBetweenTags(String tag, int start, int end) {
        String upperTag = tag.toUpperCase();
        int tagStart = parseString.indexOf("<" + upperTag + ">", start);
        if (tagStart == -1 || tagStart >= end) return "";
        tagStart += upperTag.length() + 2;
        int tagEnd = parseString.indexOf("</" + upperTag + ">", tagStart);
        return (tagEnd != -1 && tagEnd <= end) ? copyString.substring(tagStart, tagEnd) : "";
    }

    private String getValueBetweenBinaryTags(String tag) {
        return ""; // Assuming binary handling logic goes here.
    }

    private int getIndexOfTag(String tag, int start, int end, boolean isStart) {
        String upperTag = tag.toUpperCase();
        int index = isStart ? parseString.indexOf("<" + upperTag + ">", start) : parseString.indexOf("</" + upperTag + ">", start);
        return (index != -1 && index <= end) ? index : -1;
    }
}
