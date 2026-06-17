package cn.yunmeng.book.service;
/** 签到业务接口 */
public interface SignService {
    /** 查询今日是否已签到 */
    boolean isSign(Integer uid);
    /** 执行签到 */
    boolean sign(Integer uid);
}