package org.example.service.implement;

import org.example.dao.ItemDOMapper;
import org.example.dao.ItemStockDOMapper;
import org.example.dataObject.ItemDO;
import org.example.dataObject.ItemStockDO;
import org.example.error.BusinessException;
import org.example.error.EmBusinessError;
import org.example.service.ItemService;
import org.example.service.model.ItemModel;
import org.example.validator.ValidationResult;
import org.example.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Override
    @Transactional
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验生成的商品是否符合规范
        ValidationResult validateResult = validator.validate(itemModel);
        if(validateResult.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, validateResult.getErrMsg());
        }

        //将商品加入数据库
        ItemDO itemDO = itemModel2ItemDO(itemModel);
        itemDOMapper.insertSelective(itemDO);
        //加入数据库后 获得对应的自增的id
        itemModel.setId(itemDO.getId());
        //把商品库存信息添加到 item_stock表中
        ItemStockDO itemStockDO = itemModel2ItemStockDO(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);

        //返回商品
        return this.getItemById(itemModel.getId());
    }
    private ItemDO itemModel2ItemDO(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }
    private ItemStockDO itemModel2ItemStockDO(ItemModel itemModel){
        if(itemModel == null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    @Override
    public List<ItemModel> list() {
        List<ItemDO> itemDOList = itemDOMapper.listAllItem();
        List<ItemModel> itemModels = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = convert2ItemModel(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModels;
    }

    @Override
    public ItemModel getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if(itemDO == null) return null;
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(id);
        return convert2ItemModel(itemDO,itemStockDO);
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        int affectRow = itemStockDOMapper.decreaseStock(itemId, amount);
        //成功减少库存 返回为受影响的条目 正常情况下非0即1
        if(affectRow > 0){
            return true;
        }
        return false;
    }

    private ItemModel convert2ItemModel(ItemDO itemDO, ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();
        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;
    }
}
