

const ERROR_MESSAGE_CONSTANTS = {
    INVALID_CREDENTIALS: "Invalid credentials",
    INVALID_NAME :"Only alphabets and spaces allowed under 100 characters",
    INVALID_EMAIL: "Invalid email",
    INVALID_MOBILE_NUMBER: "Mobile must start with 7/8/9 and be 10 digits",
    INVALID_GSTIN: "Invalid GSTIN",
    INVALID_ADDRESS: "Address can include letters, numbers & special chars ",
    INVALID_DESCRIPTION: "Description can include letters, numbers & special chars",

}

const REGX_CONSTANT = {
    NAME : "^[A-Za-z ]{1,50}$",
    EMAIL: "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
    MOBILE : "^[789]\\d{9}$",
    GSTIN : "^[A-Z0-9]{15}$",
    ADDRESS_DESC: "^[A-Za-z0-9\\s,.\\-/#]{1,100}$",
}