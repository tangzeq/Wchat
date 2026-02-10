package tangzeqi.com.tools.chat.enums;

public enum UiConstantEnum {
    SYS("系统提示"),
    ;
    private String value;

    UiConstantEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }


}
