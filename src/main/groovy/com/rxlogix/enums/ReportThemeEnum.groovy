package com.rxlogix.enums

import java.awt.Color

/**
 * Created by gologuzov on 21.03.17.
 */
enum ReportThemeEnum {
    GRADIENT_BLUE("gradient_blue", "#0071a5", "#ffffff", "#dcebf2", "Gradient Blue"),
    SOLID_BLUE("solid_blue", "#0071a5", "#ffffff", "#dcebf2", "Solid Blue"),
    SOLID_GOLDEN_GREY("solid_golden_grey", "#eea320", "#333333", "#f2eadc", "Solid Golden Grey"),
    SOLID_ORANGE("solid_orange", "#eea320", "#333333", "#f2eadc", "Solid Orange");

    String name
    Color columnHeaderBackgroundColor
    Color columnHeaderForegroundColor
    Color subTotalBackgroundColor
    String displayName

    private ReportThemeEnum(String name, String columnHeaderBackgroundColor, String columnHeaderForegroundColor, String subTotalBackgroundColor, String displayName) {
        this.name = name
        this.columnHeaderBackgroundColor = Color.decode(columnHeaderBackgroundColor)
        this.columnHeaderForegroundColor = Color.decode(columnHeaderForegroundColor)
        this.subTotalBackgroundColor = Color.decode(subTotalBackgroundColor)
        this.displayName = displayName
    }

    public static ReportThemeEnum searchByName(String name) {
        values().find { it.name == name }
    }
}
