package org.example.service.model;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ItemModel {
    private Integer id;

    //String不填时为空字符串 所以是NotBlank 其他的类型不填就是null 所以是NotNull
    @NotBlank(message = "名称不能为空")
    private String title;

    @NotNull(message = "价格不能不填")
    @Min(value = 0,message = "价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "库存不能不填")
    private Integer stock;

    @NotBlank(message = "描述不能为空")
    private String description;

    private Integer sales;

    @NotBlank(message = "图片不能为空")
    private String imgUrl;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSales() {
        return sales;
    }

    public void setSales(Integer sales) {
        this.sales = sales;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
