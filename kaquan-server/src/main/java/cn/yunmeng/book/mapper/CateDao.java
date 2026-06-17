package cn.yunmeng.book.mapper;

import cn.yunmeng.book.pojo.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

/** 分类数据访问（支持三级层级） */
@Mapper
public interface CateDao {

    /** 查所有分类按层级排序 */
    @Select("SELECT * FROM category ORDER BY level, parent_id, sort_order")
    List<Category> findAll();

    /** 按父ID查子分类 */
    @Select("SELECT * FROM category WHERE parent_id = #{parentId} ORDER BY sort_order")
    List<Category> findByParent(Integer parentId);

    /** 新增分类 */
    @Insert("INSERT INTO category(cate_name, icon, parent_id, level, sort_order) VALUES(#{cateName}, #{icon}, #{parentId}, #{level}, #{sortOrder})")
    int insert(Category c);

    /** 更新分类 */
    @Update("UPDATE category SET cate_name=#{cateName}, icon=#{icon}, parent_id=#{parentId}, level=#{level}, sort_order=#{sortOrder} WHERE id=#{id}")
    int update(Category c);

    /** 删除分类 */
    @Delete("DELETE FROM category WHERE id=#{id}")
    int delete(Integer id);
}
