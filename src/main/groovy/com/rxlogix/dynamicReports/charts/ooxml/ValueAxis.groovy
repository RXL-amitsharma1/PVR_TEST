package com.rxlogix.dynamicReports.charts.ooxml

import com.rxlogix.dynamicReports.charts.ooxml.enums.*
import org.openxmlformats.schemas.drawingml.x2006.chart.*
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph

public class ValueAxis extends ChartAxis {

    private CTValAx ctValAx

    public ValueAxis(Chart chart, long id, AxisPosition pos, String title) {
        super(chart)
        createAxis(id, pos, title)
    }

    public ValueAxis(Chart chart, CTValAx ctValAx) {
        super(chart)
        this.ctValAx = ctValAx
    }

    @Override
    public long getId() {
        return ctValAx.getAxId().getVal()
    }

    public void setCrossBetween(AxisCrossBetween crossBetween) {
        ctValAx.getCrossBetween().setVal(fromCrossBetween(crossBetween))
    }

    public AxisCrossBetween getCrossBetween() {
        return toCrossBetween(ctValAx.getCrossBetween().getVal())
    }

    @Override
    protected CTAxPos getCTAxPos() {
        return ctValAx.getAxPos()
    }

    @Override
    protected CTNumFmt getCTNumFmt() {
        if (ctValAx.isSetNumFmt()) {
            return ctValAx.getNumFmt()
        }
        return ctValAx.addNewNumFmt()
    }

    @Override
    protected CTScaling getCTScaling() {
        return ctValAx.getScaling()
    }

    @Override
    protected CTCrosses getCTCrosses() {
        return ctValAx.getCrosses()
    }

    @Override
    protected CTBoolean getDelete() {
        return ctValAx.getDelete()
    }

    @Override
    protected CTTickMark getMajorCTTickMark() {
        return ctValAx.getMajorTickMark()
    }

    @Override
    protected CTTickMark getMinorCTTickMark() {
        return ctValAx.getMinorTickMark()
    }

    @Override
    public void crossAxis(ChartAxis axis) {
        ctValAx.getCrossAx().setVal(axis.getId())
    }

    private void createAxis(long id, AxisPosition pos, String title) {
        ctValAx = chart.getCTChart().getPlotArea().addNewValAx()
        ctValAx.addNewAxId().setVal(id)
        ctValAx.addNewAxPos()
        ctValAx.addNewScaling()
        ctValAx.addNewCrossBetween()
        ctValAx.addNewCrosses()
        ctValAx.addNewCrossAx()
        ctValAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO)
        ctValAx.addNewDelete()
        ctValAx.addNewMajorTickMark()
        ctValAx.addNewMinorTickMark()
        ctValAx.addNewMajorGridlines()
        if (title) {
            CTTitle cttTitle = ctValAx.addNewTitle()
            CTTx tx = cttTitle.addNewTx()
            CTTextBody rich = tx.addNewRich()
            rich.addNewBodyPr();  // body properties must exist (but can be empty)
            rich.addNewLstStyle();
            CTTextParagraph p = rich.addNewP()
            p.addNewPPr().addNewDefRPr()
            p.addNewR().setT(title)
            cttTitle.addNewOverlay().setVal(false);
        }
        setPosition(pos)
        setOrientation(AxisOrientation.MIN_MAX)
        setCrossBetween(AxisCrossBetween.BETWEEN)
        if (pos == AxisPosition.RIGHT) {
            setCrosses(AxisCrosses.MAX)
        } else {
            setCrosses(AxisCrosses.AUTO_ZERO)
        }
        setVisible(true)
        setMajorTickMark(AxisTickMark.OUT)
        setMinorTickMark(AxisTickMark.NONE)
    }

    private static STCrossBetween.Enum fromCrossBetween(AxisCrossBetween crossBetween) {
        switch (crossBetween) {
            case AxisCrossBetween.BETWEEN: return STCrossBetween.BETWEEN
            case AxisCrossBetween.MIDPOINT_CATEGORY: return STCrossBetween.MID_CAT
            default:
                throw new IllegalArgumentException()
        }
    }

    private static AxisCrossBetween toCrossBetween(STCrossBetween.Enum ctCrossBetween) {
        switch (ctCrossBetween.intValue()) {
            case STCrossBetween.INT_BETWEEN: return AxisCrossBetween.BETWEEN
            case STCrossBetween.INT_MID_CAT: return AxisCrossBetween.MIDPOINT_CATEGORY
            default:
                throw new IllegalArgumentException()
        }
    }
}
