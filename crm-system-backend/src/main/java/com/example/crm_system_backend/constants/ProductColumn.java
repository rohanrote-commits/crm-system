package com.example.crm_system_backend.constants;

public enum ProductColumn {

    GSTR(6, "GSTR"),
    ITC_RECONSILATION(7, "ITC Reconsilation"),
    E_INVOICE(8, "E Invoice"),
    E_WAY_BILL(9, "EWay Bill"),
    LMS(10, "LMS"),
    THIRD_EYE(11, "Third Eye"),
    SAFE_SIGN(12, "Safe Sign");

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
