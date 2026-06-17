package cn.yunmeng.book.pojo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/** 订单实体 */
@Data
public class Orders {
    private Integer id;
    /** 下单用户ID */
    private Integer userId;
    /** 商品ID */
    private Integer goodsId;
    /** 订单金额 */
    private BigDecimal orderPrice;
    /** 充值账号（QQ号/手机号） */
    private String accountNumber;
    /** 支付方式: alipay/wechat/balance */
    private String payMethod;
    /** 订单状态: pending/completed/refunded */
    private String status;
    /** 下单时间（东八区） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
