package cn.yunmeng.book.mapper;

import cn.yunmeng.book.pojo.SysNotify;
import org.apache.ibatis.annotations.*;

import java.util.List;

/** 站内通知数据访问 */
@Mapper
public interface NotifyDao {

    /** 查询可见通知（status=1）按时间倒序 */
    @Select("SELECT id, title, content, status, create_time FROM sys_notify WHERE status = 1 ORDER BY create_time DESC")
    List<SysNotify> findActive();

    /** 查询全部通知 */
    @Select("SELECT * FROM sys_notify ORDER BY create_time DESC")
    List<SysNotify> findAll();

    /** 新增通知 */
    @Insert("INSERT INTO sys_notify(title, content) VALUES(#{title}, #{content})")
    int insert(SysNotify notify);

    /** 更新通知 */
    @Update("UPDATE sys_notify SET title = #{title}, content = #{content}, status = #{status} WHERE id = #{id}")
    int update(SysNotify notify);

    /** 删除通知 */
    @Delete("DELETE FROM sys_notify WHERE id = #{id}")
    int delete(Integer id);
}
