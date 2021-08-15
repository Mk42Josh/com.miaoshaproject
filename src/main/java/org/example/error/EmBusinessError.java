package org.example.error;

public enum EmBusinessError implements CommonError{
    //通用错误类型10001
    PARAMETER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKNOWN_ERROR(10002, "未知错误"),

    //20000开头为用户信息相关错误
    USER_NOT_EXIST(20001, "用户不存在"),
    USER_LOGIN_ERROR(20002, "手机号或密码错误")
    ;

    private int errCode;
    private String errMsg;

    private EmBusinessError(int errCode, String errMsg){
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    @Override
    public int getErrorCode() {
        return this.errCode;
    }

    @Override
    public String getErrorMessage() {
        return errMsg;
    }

    @Override
    public CommonError setErrorMessage(String errorMessage) {
        this.errMsg = errorMessage;
        return this;
    }
}