package org.noear.solon.extend.mybatis;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

/**
 * 适配器
 *
 * 1.提供 mapperScan 能力
 * 2.生成 factory 的能力
 *
 * @author noear
 * @since 1.5
 * */
public interface MybatisAdapter {

    /**
     * 获取 Mapper 配置获取
     * */
    List<String> getMappers();

    /**
     * 获取配置器
     */
    Configuration getConfiguration();

    /**
     * 获取会话工厂
     */
    SqlSessionFactory getFactory();

    /**
     * 获取缓存的会话
     */
    SqlSession getSession();

}
