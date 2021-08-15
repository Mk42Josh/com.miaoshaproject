package org.example.controller;

import org.example.error.BusinessException;
import org.example.error.EmBusinessError;
import org.example.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class BaseController {
    //定义一个方法来解决没有被controller处理的exception
    @ExceptionHandler(Exception.class)//注解ExceptionHandler指明当捕获到形参对应的异常时 进入下面的处理方法
    @ResponseStatus(HttpStatus.OK)//指明该处理方法是http状态200 即正常的
    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception exception){
        Map<String, Object> responseData = new HashMap<>();
        if(exception instanceof BusinessException) {
            BusinessException businessException = (BusinessException) exception;
            responseData.put("errCode", businessException.getErrorCode());
            responseData.put("errMsg", businessException.getErrorMessage());
        }else{
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrorCode());
            responseData.put("errMsg", EmBusinessError.UNKNOWN_ERROR.getErrorMessage());
        }
        return CommonReturnType.create(responseData,"fail");
    }
}
