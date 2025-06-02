package com.rxlogix.reportTemplate.xml.generator

import com.rxlogix.config.XMLTemplateNode

import com.rxlogix.enums.XMLNodeElementType
import com.rxlogix.enums.XMLNodeType
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.Locator
import org.xml.sax.SAXException
import org.xml.sax.ext.LexicalHandler

class XMLTemplateNodeBuilder implements LexicalHandler, ContentHandler {
    XMLTemplateNode rootNode
    private Stack<XMLTemplateNode> nodeStack = new Stack<>()

    @Override
    void startDTD(String name, String publicId, String systemId) throws SAXException {
        println("[startDTD] name=${name}, publicId=${publicId}, systemId=${systemId}")
    }

    @Override
    void endDTD() throws SAXException {
        println("[endDTD]")
    }

    @Override
    void startEntity(String name) throws SAXException {
        println("[startEntity] name=${name}")
    }

    @Override
    void endEntity(String name) throws SAXException {
        println("[endEntity] name=${name}")
    }

    @Override
    void startCDATA() throws SAXException {
        println("[startCDATA]")
    }

    @Override
    void endCDATA() throws SAXException {
        println("[endCDATA]")
    }

    @Override
    void comment(char[] ch, int start, int length) throws SAXException {
        println("[comment]")
    }

    @Override
    void setDocumentLocator(Locator locator) {
        println("[setDocumentLocator] locator=${locator}")
    }

    @Override
    void startDocument() throws SAXException {
        println("[startDocument]")
    }

    @Override
    void endDocument() throws SAXException {
        println("[endDocument]")
    }

    @Override
    void startPrefixMapping(String prefix, String uri) throws SAXException {
        println("[startPrefixMapping] prefix=${prefix}, uri=${uri}")
    }

    @Override
    void endPrefixMapping(String prefix) throws SAXException {
        println("[endPrefixMapping] prefix=${prefix}")
    }

    @Override
    void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        println("[startElement] uri=${uri}, localName=${localName}, qName=${qName}")

        XMLTemplateNode parent
        if (!nodeStack.empty()) {
            parent = nodeStack.peek()
        }
        XMLTemplateNode node = createTag(parent, localName)
        if (!parent) {
            rootNode = node
            addAttribute(rootNode, "xmlns", uri)
        }
        for (int i = 0; i < atts.getLength(); i++) {
            addAttribute(node, atts.getQName(i), atts.getValue(i))
        }
        nodeStack.push(node)
    }

    @Override
    void endElement(String uri, String localName, String qName) throws SAXException {
        nodeStack.pop()
    }

    @Override
    void characters(char[] ch, int start, int length) throws SAXException {
        if (!nodeStack.empty()) {
            XMLTemplateNode node = nodeStack.peek()
            StringBuilder stringBuilder = new StringBuilder()
            for (int i = start; i< length; i++) {
                stringBuilder.append(ch[i])
            }
            node.value = stringBuilder.toString()
        }
    }

    @Override
    void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {

    }

    @Override
    void processingInstruction(String target, String data) throws SAXException {

    }

    @Override
    void skippedEntity(String name) throws SAXException {

    }

    private static void addAttribute(XMLTemplateNode node, String key, String value) {
        XMLTemplateNode attributeNode = new XMLTemplateNode(
                elementType: XMLNodeElementType.ATTRIBUTE,
                type: XMLNodeType.SOURCE_FIELD,
                tagName: key,
                value: value,
                orderingNumber: node?.children ? node.children.size() + 1 : 1,
                parent: node
        )
        node.addToChildren(attributeNode)
    }

    private static XMLTemplateNode createTag(XMLTemplateNode parent, String tagName) {
        XMLTemplateNode node = new XMLTemplateNode(
                elementType: XMLNodeElementType.TAG,
                type: parent ? XMLNodeType.SOURCE_FIELD : XMLNodeType.TAG_PROPERTIES,
                tagName: tagName,
                orderingNumber: parent?.children ? parent.children.size() + 1 : 1,
                parent: parent,
        )
        parent?.addToChildren(node)
        parent?.type = XMLNodeType.TAG_PROPERTIES
        return node
    }
}
