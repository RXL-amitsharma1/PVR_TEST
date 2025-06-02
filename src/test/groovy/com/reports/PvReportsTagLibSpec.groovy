package com.reports


import grails.testing.web.taglib.TagLibUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class PvReportsTagLibSpec extends Specification implements TagLibUnitTest<PvReportsTagLib>  {

    void "test renderAnnotateIcon"() {
        given:
        String formedTags = "<span class=\"annotationPopover\"><i class='fa fa-comment-o test'  \n" +
                "                    data-content='html' \n" +
                "                    \n" +
                "                    data-placement=right\n" +
                "                    \n" +
                "                     ></i></span>\n" +
                "                "

        expect:
        tagLib.renderAnnotateIcon([class:'test','data-content':'html']).toString() == formedTags
    }

    void "test generateSpotFireFileName"() {
        expect:
        tagLib.generateSpotFireFileName([fileName: 'testFile']).toString() == 'testFile'
    }

    @Unroll
    void "test create menu link"() {
        expect:
        tagLib.createMenuLink(link: link).toString() == output
        where:
        link       || output
        '#'        || '#'
        ''         || ''
        null       || ''
        '/xyz/abc' || '/xyz/abc'
    }

}