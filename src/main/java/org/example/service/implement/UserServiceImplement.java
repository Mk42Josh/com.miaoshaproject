package org.example.service.implement;

import org.apache.commons.lang3.StringUtils;
import org.example.dao.UserDOMapper;
import org.example.dao.UserPasswordDOMapper;
import org.example.dataObject.UserDO;
import org.example.dataObject.UserPasswordDO;
import org.example.error.BusinessException;
import org.example.error.EmBusinessError;
import org.example.service.UserService;
import org.example.service.model.UserModel;
import org.example.validator.ValidationResult;
import org.example.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImplement implements UserService {
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;
    @Autowired
    private ValidatorImpl validatorImpl;

    @Override
    public UserModel getUserById(Integer id) {
        //调用userDOMapper获取对应用户的dataObject
        //userDO不能传给前端
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        //Mybatis自动生成的方法只能通过主键 获取用户对应密码需要自己构造一下
        //首先在UserPassWordDOMapper.xml中修改原本的selectByPrimaryKey为selectByUserId
        //然后在UserPassWordDOMapper接口中定义对应的新的抽象方法
        if(userDO == null) return null;
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        //将两个本应属于同一个对象的数据合并起来
        return dataObject2UserModel(userDO, userPasswordDO);
    }

    @Override
    @Transactional//保证这是一个事务（原子性）避免UserDO插入数据库了 但UserPasswordDO没有的情况
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
//        if(StringUtils.isEmpty(userModel.getName())
//                || StringUtils.isEmpty(userModel.getTelephone())
//                || userModel.getGender() == null
//                || userModel.getAge() == null){
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//        }
        ValidationResult validationResult = validatorImpl.validate(userModel);
        if(validationResult.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, validationResult.getErrMsg());
        }
        //这里将传入的userModel转化为原本就在两张表的DO
        UserDO userDO = userModel2UserDO(userModel);
        //1
        //将新注册用户的信息插入数据库
        //insertSelective会在插入数据前判null 如果为null就使用数据库的默认值
        //而insert会使用null值
        //2
        //此处userDO是没有id的 知道插入数据库才有自增id
        //需要在UserDOMapper.xml中为insertSelective方法指定自增主键 keyProperty="id" useGeneratedKeys="true"
        //然后userDO回传给userModel（id） 再由userModel传数据给userPasswordDOMapper设置
        userDOMapper.insertSelective(userDO);
        userModel.setId(userDO.getId());
        UserPasswordDO userPasswordDO = userModel2UserPasswordDO(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);
    }

    @Override
    public UserModel validateLogin(String telephone, String encryptPassword) throws BusinessException {
        UserDO userDO = userDOMapper.selectByTelephone(telephone);
        if(userDO == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_ERROR);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = dataObject2UserModel(userDO, userPasswordDO);
        if(!StringUtils.equals(userModel.getEncrptPassword(), encryptPassword)){
            throw new BusinessException(EmBusinessError.USER_LOGIN_ERROR);
        }
        return userModel;
    }

    private UserPasswordDO userModel2UserPasswordDO(UserModel userModel){
        if(userModel == null) return null;
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setUserId(userModel.getId());
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        return userPasswordDO;
    }

    private UserDO userModel2UserDO(UserModel userModel){
        if(userModel == null) return null;
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        return userDO;
    }

    //UserDO UserPasswordDO只是因为数据库的表设计时分开成为两张表 此处需要合并在一起 成为一个用户的信息
    private UserModel dataObject2UserModel(UserDO userDO, UserPasswordDO userPasswordDO){
        UserModel userModel = new UserModel();
        if(userDO == null) return null;
        //user_info表的数据对应userDO 此处将userDO全部拷贝到userModel中
        BeanUtils.copyProperties(userDO, userModel);

        if(userPasswordDO == null) return null;
        //user_password对应userPasswordDO 但是id是重复的 故不能简单拷贝
        userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        return userModel;
    }
}
