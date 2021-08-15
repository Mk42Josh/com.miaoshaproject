package org.example.controller;

import org.example.controller.viewobject.ItemVO;
import org.example.dataObject.ItemDO;
import org.example.error.BusinessException;
import org.example.response.CommonReturnType;
import org.example.service.ItemService;
import org.example.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/item")
@RequestMapping("/item")//RequestMapping则是映射路径
@CrossOrigin(originPatterns = "*",allowCredentials = "true",allowedHeaders = "*")
public class ItemController extends BaseController{

    @Autowired
    private ItemService itemService;

    @RequestMapping(value = "/get",method ={RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id){
        ItemModel itemModel = itemService.getItemById(id);
        ItemVO itemVO = itemModel2ItemVO(itemModel);
        return CommonReturnType.create(itemVO);
    }

    @RequestMapping(value = "/list",method ={RequestMethod.GET})
    @ResponseBody
    public CommonReturnType listItem(){
        List<ItemModel> itemModelList = itemService.list();
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = itemModel2ItemVO(itemModel);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }

    @RequestMapping(value = "/create",method ={RequestMethod.POST},consumes = {"application/x-www-form-urlencoded"})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price")BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        ItemModel item = itemService.createItem(itemModel);
        ItemVO itemVO =itemModel2ItemVO(item);
        return CommonReturnType.create(itemVO);
    }

    private ItemVO itemModel2ItemVO(ItemModel itemModel){
        if(itemModel == null) return null;
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        return itemVO;
    }

}
