package cn.yunmeng.book.pojo;
import lombok.Data;
import java.util.Date;
/**后台管理员实体，对应admin表*/
@Data
public class Admin {
    private Integer id;
    private String username; //管理员账号
    private String password; //管理员MD5密码
    private Date createTime;
}