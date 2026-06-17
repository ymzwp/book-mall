package cn.yunmeng.book.pojo;
import lombok.Data;
import java.math.BigDecimal;
/**卡券商品实体，携带商品图片字段*/
@Data
public class Goods {
    private Integer id;
    private Integer cateId;      //分类id
    private String goodsName;    //商品名称
    private BigDecimal price;    //商品价格
    private Integer stock;      //库存
    private String goodsImg;     //商品图片url
    private String info;         //商品简介
    private String detailImgs;   //详情图片，逗号分隔多个URL
    private Integer status;      //1上架 0下架
}