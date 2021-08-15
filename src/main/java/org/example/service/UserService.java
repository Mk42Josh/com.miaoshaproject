package org.example.service;

import org.example.error.BusinessException;
import org.example.service.model.UserModel;

public interface UserService {
    //通过ID获取用户对象
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;

    /**
     * encryptPassword是加密后的密码
     * @param telephone
     * @param encryptPassword
     * @throws BusinessException
     */
    UserModel validateLogin(String telephone, String encryptPassword) throws BusinessException;
}
