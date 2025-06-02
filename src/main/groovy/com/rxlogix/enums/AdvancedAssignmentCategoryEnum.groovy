package com.rxlogix.enums

enum AdvancedAssignmentCategoryEnum {
    PVC,
    PVQ


    public getI18nKey() {
        return "app.advanced.assignment.report.category.${this.name()}"
    }

    static getItemsToSelect() {
        [PVC,
         PVQ]
    }
}
