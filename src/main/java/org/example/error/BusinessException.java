package org.example.error;

/*
    包装器 业务异常类实现
 */
public class BusinessException extends Exception implements CommonError{
    private CommonError commonError;

    //直接接收CommonError实现枚举类EmBusinessError 构造业务异常
    public BusinessException(CommonError commonError){
        super();
        this.commonError = commonError;
    }
    //接收自定义errMsg的方法构造业务异常
    public BusinessException(CommonError commonError, String errMsg){
        this(commonError);
        commonError.setErrorMessage(errMsg);
    }

    @Override
    public int getErrorCode() {
        return commonError.getErrorCode();
    }

    @Override
    public String getErrorMessage() {
        return commonError.getErrorMessage();
    }

    @Override
    public CommonError setErrorMessage(String errorMessage) {
        commonError.setErrorMessage(errorMessage);
        return this;
    }
}
