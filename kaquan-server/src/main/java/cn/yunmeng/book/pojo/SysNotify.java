package cn.yunmeng.book.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/** 站内通知实体 */
@Data
public class SysNotify {
    private Integer id;
    /** 通知标题 */
    private String title;
    /** 通知内容 */
    private String content;
    /** 状态: 1显示 0隐藏 */
    private Integer status;
    /** 创建时间（东八区） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
