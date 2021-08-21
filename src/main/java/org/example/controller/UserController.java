package org.example.controller;

import org.apache.commons.lang3.StringUtils;
import org.example.controller.viewobject.UserVO;
import org.example.error.BusinessException;
import org.example.error.EmBusinessError;
import org.example.response.CommonReturnType;
import org.example.save.HttpServletRequest;
import org.example.service.UserService;
import org.example.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Controller("user")//controller告诉springmvc 该类是在容器启动时需要被加载到spring的bean工厂里去的
@RequestMapping("/user")//RequestMapping则是映射路径
//@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
@CrossOrigin(originPatterns = "*",allowCredentials = "true",allowedHeaders = "*")
public class UserController extends BaseController{

    @Autowired
    private UserService userService;

    //spring通过bean的方式注入对象是单例 但是如何做到多个用户并发请求otp验证码呢？
    //threadlocal...
    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户登录接口
    @RequestMapping(value = "/login",method ={RequestMethod.POST},consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telephone")String telephone,
                                     @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if(org.apache.commons.lang3.StringUtils.isEmpty(telephone) ||
                StringUtils.isEmpty(password)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        UserModel userModel = userService.validateLogin(telephone, this.EncodeByMd5(password));

        //将登陆凭证加入到用户登陆成功的session内
        this.httpServletRequest.setAttribute("IS_LOGIN",true);
        this.httpServletRequest.setAttribute("LOGIN_USER", userModel);
        return CommonReturnType.create(null);
    }


        @RequestMapping("/get")
//      @ResponseBody注解的作用
//      是将controller的方法返回的对象转换为指定的格式之后
//      写入到response对象的body区（响应体中），通常用来返回JSON数据或者是XML数据
    @ResponseBody
    //测试语句localhost:8090/user/get?id=1
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        //调用service服务获取对应id的用户对象 并返回给前端
        UserModel userModel = userService.getUserById(id);
        /**
         * 直接return userModel会将该用户的所有信息都暴露出去
         * 正确的做法是新建一个专门用于视图的类提供数据UserVO
         */
        //return userModel;

        if(userModel == null){//若要获取的用户信息不存在 则抛出异常
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
            /*
            为了验证 EmBusinessError中未知错误UNKNOWN_ERROR能启用
            此处在用户为空的前提下 设置用户密码 导致抛出异常不是BusinessException类
            为了达到这一目的 将上一行throw new BusinessException(EmBusinessError.USER_NOT_EXIST);注释掉了
             */
//            userModel.setEncrptPassword("123");
        }

        /*
        * 为了能够将程序出错时的 错误信息 显示到网页上（前端）
        * 此处不再直接返回视图类 采用CommonReturnType
        * 前端只要连接是成功的 status就是200 所以只要连接成功 前端只需要处理这个通用返回对象即可
        */
        //return userModel2UserVO(userModel);
        UserVO userVO = userModel2UserVO(userModel);
        return CommonReturnType.create(userVO);
    }

    //获取opt验证码
    //method ={RequestMethod.POST}指明getOtp方法需要映射到http的POST请求才能生效
    @RequestMapping(value = "/getotp",method ={RequestMethod.POST},consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telephone") String telephone){
        //1 生成验证码
        Random random = new Random();
        int randInt = random.nextInt(9999)+10000;
        String optCode = String.valueOf(randInt);

        //2 需要将验证码与手机号做一个映射 一般分布式中会使用redis 因为它是键值对的存储形式 且可以覆盖value 且可以设置过期时间等
        // 这里采用httpsession的方式做关联
        httpServletRequest.setAttribute(telephone, optCode);

        //3 通过短信将otp验证码发送给用户
        System.out.println("Telephone=" + telephone + " & OtpCode=" + optCode);
        return CommonReturnType.create(null);
    }

    /**
     *
     * 该方法是在获取短信验证码以后进入的 但是由于 跨域 的一些问题
     * 每次从httpServletRequest中获取的对应的对应验证码 都是null
     * 暂时没有解决这个问题
     *
     * @param telephone
     * @param otpCode
     * @param name
     * @param gender
     * @param age
     * @param password
     * @return
     * @throws BusinessException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    //用户注册接口
    @RequestMapping(value = "/register",method ={RequestMethod.POST},consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telephone")String telephone,
                                     @RequestParam(name = "otpCode")String otpCode,
                                     @RequestParam(name = "name")String name,
                                     @RequestParam(name = "gender")Integer gender,
                                     @RequestParam(name = "age")Integer age,
                                     @RequestParam(name = "password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证otp和手机号相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getAttribute(telephone);
        //如果输入的验证码与http中保存的不一致 抛出异常
        /**
         * 为了这个注册能继续下去 不校验验证码
         *
         * 8.21 自己实现了HttpServletRequest 现在可以校验验证码了
         */
        if(!com.alibaba.druid.util.StringUtils.equals(otpCode, inSessionOtpCode)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码错误");
        }
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));//new Byte(String.valueOf(gender.intValue()))
        userModel.setTelephone(telephone);
        userModel.setRegisterMode("byphone");
        //原加密方式有问题 返回为空
//        userModel.setEncrptPassword(MD5Encoder.encode(password.getBytes()));
        userModel.setEncrptPassword(this.EncodeByMd5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        return base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
    }

    private UserVO userModel2UserVO(UserModel userModel){
        if(userModel == null) return null;
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }
}
