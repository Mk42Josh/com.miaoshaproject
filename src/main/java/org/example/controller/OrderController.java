package org.example.controller;

import org.example.error.BusinessException;
import org.example.response.CommonReturnType;
import org.example.service.OrderService;
import org.example.service.model.OrderModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping
@CrossOrigin(originPatterns = "*",allowCredentials = "true",allowedHeaders = "*")
public class OrderController extends BaseController{

    @Autowired
    private OrderService orderService;

    //封装下单请求
    public CommonReturnType createOrder(@RequestParam(name="itemId")Integer itemId,
                                        @RequestParam(name = "amount")Integer amount,
                                        @RequestParam(name = "userId")Integer userId) throws BusinessException {
        OrderModel orderModel = orderService.createOrder(userId, itemId, amount);
        return CommonReturnType.create(orderModel);
    }
}
