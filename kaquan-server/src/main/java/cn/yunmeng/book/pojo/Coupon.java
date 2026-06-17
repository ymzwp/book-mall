package cn.yunmeng.book.pojo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/** 优惠券实体 */
@Data
public class Coupon {
    private Integer id;
    /** 券名称 */
    private String title;
    /** 优惠金额 */
    private BigDecimal amount;
    /** 最低消费金额 */
    private BigDecimal minAmount;
    /** 库存数量 */
    private Integer stock;
    /** 状态: 1上架 0下架 */
    private Integer status;
    /** 创建时间 */
    private Date createTime;
}
