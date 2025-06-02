package com.rxlogix.jasperserver

public class ReportUnit extends Resource {
    public static final byte LAYOUT_POPUP_SCREEN = 1
    public static final byte LAYOUT_SEPARATE_PAGE = 2
    public static final byte LAYOUT_TOP_OF_PAGE = 3
    public static final byte LAYOUT_IN_PAGE = 4

    private ResourceReference dataSource = null
    private ResourceReference query = null
    private List inputControls
    private ResourceReference mainReport = null
    private List resources
    private String inputControlRenderingView
    private String reportRenderingView
    private boolean alwaysPromptControls
    private byte controlsLayout = LAYOUT_POPUP_SCREEN
    private Long dataSnapshotId

    public ReportUnit() {
        resources = new ArrayList()
        inputControls = new ArrayList()
    }

    public ResourceReference getDataSource() {
        return dataSource
    }

    public void setDataSource(ResourceReference dataSource) {
        this.dataSource = dataSource
    }

    public ResourceReference getQuery() {
        return query
    }

    public void setQuery(ResourceReference query) {
        this.query = query
    }

    public List getInputControls() {
        return inputControls
    }

    public void setInputControls(List inputControls) {
        this.inputControls = inputControls
    }

    public List getResources() {
        return resources
    }

    public void setResources(List resources) {
        this.resources = resources
    }

    public ResourceReference getMainReport() {
        return mainReport
    }

    public void setMainReport(ResourceReference mainReport) {
        this.mainReport = mainReport
    }

    public void setMainReport(FileResource report) {
        setMainReport(new ResourceReference(report))
    }

    public String getInputControlRenderingView() {
        return inputControlRenderingView
    }

    public void setInputControlRenderingView(String inputControlRenderingView) {
        this.inputControlRenderingView = inputControlRenderingView
    }

    public String getReportRenderingView() {
        return reportRenderingView
    }

    public void setReportRenderingView(String reportRenderingView) {
        this.reportRenderingView = reportRenderingView
    }


    public boolean isAlwaysPromptControls() {
        return alwaysPromptControls
    }

    public void setAlwaysPromptControls(boolean alwaysPromptControls) {
        this.alwaysPromptControls = alwaysPromptControls
    }


    public byte getControlsLayout() {
        return controlsLayout
    }

    public void setControlsLayout(byte controlsLayout) {
        this.controlsLayout = controlsLayout
    }

    public void addResource(ResourceReference resourceReference) {
        resources.add(resourceReference);
    }

    public void addResourceReference(String referenceURI) {
        addResource(new ResourceReference(referenceURI));
    }

    public void addResource(FileResource resource) {
        addResource(new ResourceReference(resource));
    }

    public ResourceReference removeResource(int index) {
        return (ResourceReference) resources.remove(index);
    }

    protected Class getClientItf() {
        return ReportUnit.class
    }

    public Long getDataSnapshotId() {
        return dataSnapshotId
    }

    public void setDataSnapshotId(Long dataSnapshotId) {
        this.dataSnapshotId = dataSnapshotId
    }

}
