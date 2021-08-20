package org.example.service.implement;

import org.example.dao.OrderDOMapper;
import org.example.dao.SequenceDOMapper;
import org.example.dataObject.OrderDO;
import org.example.dataObject.SequenceDO;
import org.example.error.BusinessException;
import org.example.error.EmBusinessError;
import org.example.service.ItemService;
import org.example.service.OrderService;
import org.example.service.UserService;
import org.example.service.model.ItemModel;
import org.example.service.model.OrderModel;
import org.example.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException {
        //1 校验输入
        UserModel user = userService.getUserById(userId);
        if(Objects.isNull(user)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户不存在");
        }
        ItemModel item = itemService.getItemById(itemId);
        if(Objects.isNull(item)){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品不存在");
        }
        if(amount <= 0 || amount > 99){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "购买数量错误");
        }

        //2 落单减库存（无法应对落单后不支付的情况，有人恶意下单不支付，就会使得商品实际没有卖但库存减了）
        // 则另一种就是支付减库存了
        boolean result = itemService.decreaseStock(itemId, amount);
        if(!result){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //3 订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        orderModel.setItemPrice(item.getPrice());
        orderModel.setOrderPrice(item.getPrice().multiply(new BigDecimal(amount)));
        orderModel.setId(generateOrderNo());

        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insert(orderDO);

        //4 返回前端

        return orderModel;
    }

    /**
     * 为什么这里要用REQUIRES_NEW？
     *
     * 为了保证全局唯一性，只要生成了的订单号，就不可以再产生一样的
     * 这里为了防止调用generateOrderNo的事务回滚而导致generateOrderNo本身回滚
     * 采用了REQUIRES_NEW，使得generateOrderNo执行完就提交，是一个单独的事务
     *
     * 而默认的REQUIRED则是与调用者的事务合二为一
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private String generateOrderNo(){
        StringBuilder sb = new StringBuilder();

        //前8位 年月日
        LocalDateTime now = LocalDateTime.now();
        sb.append(now.format(DateTimeFormatter.ISO_DATE).replace("-", ""));

        //中间6位 自增序列
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        int currentValue = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(currentValue + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String middle6 = String.valueOf(sequenceDO.getCurrentValue());
        for(int i = 0; i < 6-middle6.length(); i++){
            sb.append("0");
        }
        sb.append(middle6);

        //最后2位 分库分表位
        /**
         * 假设用户id为10012 则最后2位是10012 % 100 = 12
         * 将该用户的信息全部放到第12号表
         */
        sb.append("00");
        return sb.toString();
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel == null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        return orderDO;
    }
}
