package org.example.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@Component
public class ValidatorImpl implements InitializingBean {

    private Validator validator;

    public ValidatorImpl() {
        System.out.println("ValidatorImpl创建了");
    }

    //在bean初始化以后 执行afterPropertiesSet方法
    @Override
    public void afterPropertiesSet() throws Exception {
        //将hibernate validator 通过工厂初始化方式 实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
    public ValidationResult validate(Object bean){
        final ValidationResult result = new ValidationResult();
        //validator就是校验器 容器启动后在ValidatorImpl被加载 然后被工厂实例化validator后
        //就可以依据传入的类 根据该类内部的注解 去校验并返回错误信息
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if(constraintViolationSet.size() > 0){
            result.setHasErrors(true);
            constraintViolationSet.forEach(constraintViolation -> {
                String message = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                result.getErrorMsgMap().put(propertyName, message);
            });
        }
        return result;
    }

}
