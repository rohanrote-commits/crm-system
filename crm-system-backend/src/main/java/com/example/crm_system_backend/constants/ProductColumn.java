package com.example.crm_system_backend.constants;

public enum ProductColumn {

    GSTR(7, "GSTR"),
    ITC_RECONCILIATION(3, "ITC Reconciliation"),
    E_INVOICE(6, "E Invoice"),
    E_WAY_BILL(2, "EWay Bill"),
    LMS(1, "LMS"),
    THIRD_EYE(4, "Third Eye"),
    SAFE_SIGN(5, "Safe Sign");

    private final int columnIndex;
    private final String moduleName;

    ProductColumn(int columnIndex, String moduleName) {
        this.columnIndex = columnIndex;
        this.moduleName = moduleName;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public String getModuleName() {
        return moduleName;
    }

    public static ProductColumn fromColumn(int columnIndex) {
        for (ProductColumn pc : values()) {
            if (pc.columnIndex == columnIndex) {
                return pc;
            }
        }
        return null;
    }
}
