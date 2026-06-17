package cn.yunmeng.book.mapper;

import cn.yunmeng.book.pojo.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/** 商品数据访问 */
@Mapper
public interface GoodsDao {
    /** 查询所有上架商品 */
    @Select("SELECT * FROM goods WHERE status = 1")
    List<Goods> findUpAll();

    /** 按分类查上架商品 */
    @Select("SELECT * FROM goods WHERE status = 1 AND cate_id = #{cid}")
    List<Goods> findByCid(Integer cid);

    /** 按ID查商品详情 */
    @Select("SELECT * FROM goods WHERE id = #{id}")
    Goods findById(Integer id);

    /** 分页查全部上架商品 */
    @Select("SELECT * FROM goods WHERE status = 1 LIMIT #{offset}, #{size}")
    List<Goods> findUpPage(@Param("offset") int offset, @Param("size") int size);

    /** 分页按分类查上架商品 */
    @Select("SELECT * FROM goods WHERE status = 1 AND cate_id = #{cid} LIMIT #{offset}, #{size}")
    List<Goods> findByCidPage(@Param("cid") Integer cid, @Param("offset") int offset, @Param("size") int size);

    /** 统计上架商品总数 */
    @Select("SELECT COUNT(*) FROM goods WHERE status = 1")
    int countAll();

    /** 统计某分类上架商品数 */
    @Select("SELECT COUNT(*) FROM goods WHERE status = 1 AND cate_id = #{cid}")
    int countByCid(Integer cid);
}
