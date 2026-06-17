package cn.yunmeng.book.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值订单实体
 */
@Data
public class RechargeOrder {
    private Long id;
    /** 用户ID */
    private Integer userId;
    /** 订单号 */
    private String orderNo;
    /** 充值金额 */
    private BigDecimal amount;
    /** 支付方式 */
    private String payMethod;
    /** 订单状态 */
    private String status;
    /** 充值前余额 */
    private BigDecimal balanceBefore;
    /** 充值后余额 */
    private BigDecimal balanceAfter;
    /** 备注 */
    private String remark;
    /** 创建时间（东八区） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
