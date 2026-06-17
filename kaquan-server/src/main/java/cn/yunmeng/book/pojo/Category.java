package cn.yunmeng.book.pojo;
import lombok.Data;
import java.util.List;

/** 商品分类实体（支持三级层级） */
@Data
public class Category {
    private Integer id;
    /** 分类名称 */
    private String cateName;
    /** 分类图标URL */
    private String icon;
    /** 父分类ID，0为一级 */
    private Integer parentId;
    /** 层级: 1一级 2二级 3三级 */
    private Integer level;
    /** 排序 */
    private Integer sortOrder;
    /** 子分类列表（非数据库字段） */
    private List<Category> children;
}
