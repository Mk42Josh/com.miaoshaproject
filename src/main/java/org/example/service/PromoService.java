package org.example.service;

import org.example.service.model.PromoModel;

public interface PromoService {

    //根据商品id获取该商品秒杀信息
    PromoModel getPromoByItemId(Integer itemId);
}
