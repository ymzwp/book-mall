package cn.yunmeng.book.pojo;
import lombok.Data;
import java.util.Date;
/**用户签到实体*/
@Data
public class UserSign {
    private Integer id;
    private Integer userId;
    private Date signDate;//签到日期
}