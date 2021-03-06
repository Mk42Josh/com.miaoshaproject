package org.example.service.model;

import java.math.BigDecimal;

public class OrderModel {
    //订单号一般有明确的信息 就像身份证一样 例如会含有时间20210815 所以用string
    private String id;

    private Integer userId;

    private Integer itemId;

    //若非空 则表示是以秒杀方式下单
    private Integer promoId;

    //购买时的商品单价 秒杀时为秒杀商品价格
    private BigDecimal itemPrice;

    //购买的数量
    private Integer amount;

    //购买的金额 秒杀时为秒杀订单总价
    private BigDecimal orderPrice;


    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }
}
