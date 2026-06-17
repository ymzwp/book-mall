package cn.yunmeng.book.pojo;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
/**
 * 前台用户实体类，对应user数据表
 */
@Data //lombok自动生成get/set
public class User {
    private Integer id;         //用户主键
    private String username;    //登录账号
    private String password;    //MD5加密密码
    private String nickName;    //用户昵称
    private String phone;       //手机号
    private String email;       //邮箱
    private String avatar;      //头像图片地址
    private BigDecimal balance; //账户余额
    private Integer points;    //云梦豆
    private String memberLevel;//会员等级: normal/bronze/silver/gold
    private String inviteCode; //邀请码
    private Integer invitedBy; //邀请人ID
    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;    //注册时间
}