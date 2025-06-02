/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License")
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.rxlogix.reportTemplate.xml.generator

import org.apache.xmlbeans.*

import javax.xml.namespace.QName

class SampleXmlUtil {
    private static final int MAX_ELEMENTS = 1000
    private int _nElements
    private SchemaTypeSampleGenerator sampleGenerator

    private SampleXmlUtil() {
        this.sampleGenerator = new SchemaTypeSampleGenerator()
    }

    /**
     * Cursor position
     * Before:
     * <theElement>^</theElement>
     * After:
     * <theElement><lots of stuff/>^</theElement>
     */
    private void createSampleForType(SchemaType stype, XmlCursor xmlc) {
        if (_typeStack.contains(stype))
            return
        _typeStack.add(stype)
        try {
            if (stype.isSimpleType() || stype.isURType()) {
                processSimpleType(stype, xmlc)
                return
            }

            // complex Type
            // <theElement>^</theElement>
            processAttributes(stype, xmlc)

            // <theElement attri1="string">^</theElement>
            switch (stype.getContentType()) {
                case SchemaType.NOT_COMPLEX_TYPE:
                case SchemaType.EMPTY_CONTENT:
                    // noop
                    break
                case SchemaType.SIMPLE_CONTENT:
                    processSimpleType(stype, xmlc)
                    break
                case SchemaType.MIXED_CONTENT:
                    xmlc.insertChars(sampleGenerator.nextWord() + " ")
                    if (stype.getContentModel() != null) {
                        processParticle(stype.getContentModel(), xmlc, true)
                    }
                    xmlc.insertChars(sampleGenerator.nextWord())
                    break
                case SchemaType.ELEMENT_CONTENT:
                    if (stype.getContentModel() != null) {
                        processParticle(stype.getContentModel(), xmlc, false)
                    }
                    break
            }
        }
        finally {
            _typeStack.remove(_typeStack.size() - 1)
        }
    }

    private void processSimpleType(SchemaType stype, XmlCursor xmlc) {
        String sample = sampleGenerator.sampleDataForSimpleType(stype)
        xmlc.insertChars(sample)
    }

    /**
     * Cursor position:
     * Before this call:
     * <outer><foo/>^</outer>  (cursor at the ^)
     * After this call:
     * <<outer><foo/><bar/>som text<etc/>^</outer>
     */
    private void processParticle(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        int loop = determineMinMaxForSample(sp, xmlc)

        while (loop-- > 0) {
            switch (sp.getParticleType()) {
                case (SchemaParticle.ELEMENT):
                    processElement(sp, xmlc, mixed)
                    break
                case (SchemaParticle.SEQUENCE):
                    processSequence(sp, xmlc, mixed)
                    break
                case (SchemaParticle.CHOICE):
                    processChoice(sp, xmlc, mixed)
                    break
                case (SchemaParticle.ALL):
                    processAll(sp, xmlc, mixed)
                    break
                case (SchemaParticle.WILDCARD):
                    processWildCard(sp, xmlc, mixed)
                    break
                default:
                    break
            }
        }
    }

    private int determineMinMaxForSample(SchemaParticle sp, XmlCursor xmlc) {
        int minOccurs = sp.getIntMinOccurs()
        int maxOccurs = sp.getIntMaxOccurs()

        if (minOccurs == maxOccurs)
            return minOccurs

        int result = minOccurs
        if (result == 0 && _nElements < MAX_ELEMENTS)
            result = 1

        if (sp.getParticleType() != SchemaParticle.ELEMENT)
            return result

        // it probably only makes sense to put comments in front of individual elements that repeat

        if (sp.getMaxOccurs() == null) {
            // xmlc.insertComment("The next " + getItemNameOrType(sp, xmlc) + " may be repeated " + minOccurs + " or more times")
            if (minOccurs == 0)
                xmlc.insertComment("Zero or more repetitions:")
            else
                xmlc.insertComment(minOccurs + " or more repetitions:")
        } else if (sp.getIntMaxOccurs() > 1) {
            xmlc.insertComment(minOccurs + " to " + String.valueOf(sp.getMaxOccurs()) + " repetitions:")
        } else {
            xmlc.insertComment("Optional:")
        }
        return result
    }

    /*
     Return a name for the element or the particle type to use in the comment for minoccurs, max occurs
    */

    private String getItemNameOrType(SchemaParticle sp, XmlCursor xmlc) {
        String elementOrTypeName
        if (sp.getParticleType() == SchemaParticle.ELEMENT) {
            elementOrTypeName = "Element (" + sp.getName().getLocalPart() + ")"
        } else {
            elementOrTypeName = printParticleType(sp.getParticleType())
        }
        return elementOrTypeName
    }

    private void processElement(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        // cast as schema local element
        SchemaLocalElement element = (SchemaLocalElement) sp
        if (element.getType().abstract) {
            return
        }
        /// ^  -> <elemenname></elem>^
        xmlc.insertElement(element.getName().getLocalPart(), element.getName().getNamespaceURI())
        _nElements++
        /// -> <elem>^</elem>
        xmlc.toPrevToken()
        // -> <elem>stuff^</elem>

        createSampleForType(element.getType(), xmlc)
        // -> <elem>stuff</elem>^
        xmlc.toNextToken()

    }

    private void moveToken(int numToMove, XmlCursor xmlc) {
        for (int i = 0; i < Math.abs(numToMove); i++) {
            if (numToMove < 0) {
                xmlc.toPrevToken()
            } else {
                xmlc.toNextToken()
            }
        }
    }

    private static final String formatQName(XmlCursor xmlc, QName qName) {
        XmlCursor parent = xmlc.newCursor()
        parent.toParent()
        String prefix = parent.prefixForNamespace(qName.getNamespaceURI())
        parent.dispose()
        String name
        if (prefix == null || prefix.length() == 0)
            name = qName.getLocalPart()
        else
            name = prefix + ":" + qName.getLocalPart()
        return name
    }

    private void processAttributes(SchemaType stype, XmlCursor xmlc) {
        SchemaProperty[] attrProps = stype.getAttributeProperties()
        for (int i = 0; i < attrProps.length; i++) {
            SchemaProperty attr = attrProps[i]
            String defaultValue = attr.getDefaultText()
            xmlc.insertAttributeWithValue(attr.getName(), defaultValue == null ?
                    sampleGenerator.sampleDataForSimpleType(attr.getType()) : defaultValue)
        }
    }

    private void processSequence(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        SchemaParticle[] spc = sp.getParticleChildren()
        for (int i = 0; i < spc.length; i++) {
            /// <parent>maybestuff^</parent>
            processParticle(spc[i], xmlc, mixed)
            //<parent>maybestuff...morestuff^</parent>
            if (mixed && i < spc.length - 1)
                xmlc.insertChars(sampleGenerator.nextWord())
        }
    }

    private void processChoice(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        SchemaParticle[] spc = sp.getParticleChildren()
        processParticle(spc[0], xmlc, mixed)
        /*
        xmlc.insertComment("You have a CHOICE of the next " + String.valueOf(spc.length) + " items at this level")
        for (int i = 0; i < spc.length; i++) {
            processParticle(spc[i], xmlc, mixed)
        }*/
    }

    private void processAll(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        SchemaParticle[] spc = sp.getParticleChildren()
        for (int i = 0; i < spc.length; i++) {
            processParticle(spc[i], xmlc, mixed)
            if (mixed && i < spc.length - 1)
                xmlc.insertChars(sampleGenerator.nextWord())
        }
    }

    private void processWildCard(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
        xmlc.insertComment("You may enter ANY elements at this point")
        xmlc.insertElement("AnyElement")
    }

    /**
     * This method will get the base type for the schema type
     */
    private String printParticleType(int particleType) {
        StringBuffer returnParticleType = new StringBuffer()
        returnParticleType.append("Schema Particle Type: ")

        switch (particleType) {
            case SchemaParticle.ALL:
                returnParticleType.append("ALL\n")
                break
            case SchemaParticle.CHOICE:
                returnParticleType.append("CHOICE\n")
                break
            case SchemaParticle.ELEMENT:
                returnParticleType.append("ELEMENT\n")
                break
            case SchemaParticle.SEQUENCE:
                returnParticleType.append("SEQUENCE\n")
                break
            case SchemaParticle.WILDCARD:
                returnParticleType.append("WILDCARD\n")
                break
            default:
                returnParticleType.append("Schema Particle Type Unknown")
                break
        }

        return returnParticleType.toString()
    }

    private ArrayList _typeStack = new ArrayList()
}
