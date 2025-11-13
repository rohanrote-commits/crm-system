package com.example.crm_system_backend.constants;

public final class RegxConstant {
    public static  final String NAME_REGEX = "^[A-Za-z ]{1,50}$";

    public static  final String ADDRESS_REGEX = "^[A-Za-z0-9 ,./#\\-]{1,100}$";

    public static  final String MOBILE_REGEX = "^[789]\\d{9}$";

    public static  final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    public static  final String DESCRIPTION_REGEX = "^[A-Za-z0-9 ,./#@!$%^&*()_\\-]{1,100}$";

    public static  final String GSTIN_REGEX = "^[A-Z0-9]{15}$";
}
