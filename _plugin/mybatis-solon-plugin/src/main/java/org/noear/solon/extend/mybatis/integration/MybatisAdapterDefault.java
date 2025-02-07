package org.noear.solon.extend.mybatis.integration;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.core.Aop;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.core.event.EventBus;
import org.noear.solon.extend.mybatis.MybatisAdapter;
import org.noear.solon.extend.mybatis.tran.SolonManagedTransactionFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Mybatis 适配器默认实现
 *
 * @author noear
 * @since 1.1
 */
public class MybatisAdapterDefault implements MybatisAdapter {
    protected final BeanWrap dsWrap;

    protected Configuration config;
    protected SqlSessionFactory factory;
    protected List<String> mappers = new ArrayList<>();
    protected SqlSessionFactoryBuilder factoryBuilder;

    /**
     * 构建Sql工厂适配器，使用默认的 typeAliases 和 mappers 配置
     */
    protected MybatisAdapterDefault(BeanWrap dsWrap) {
        this(dsWrap, Solon.cfg().getProp("mybatis"));
    }

    /**
     * 构建Sql工厂适配器，使用属性配置
     */
    protected MybatisAdapterDefault(BeanWrap dsWrap, Properties props) {
        this.dsWrap = dsWrap;
        this.factoryBuilder = new SqlSessionFactoryBuilder();

        DataSource dataSource = dsWrap.raw();
        String dataSourceId = "ds-" + (dsWrap.name() == null ? "" : dsWrap.name());

        TransactionFactory tf = new SolonManagedTransactionFactory();
        Environment environment = new Environment(dataSourceId, tf, dataSource);

        initConfiguration(environment);

        //加载插件
        for (Interceptor i : MybatisPlugins.getInterceptors()) {
            config.addInterceptor(i);
        }

        //1.分发事件，推给扩展处理
        EventBus.push(config);

        //2.初始化（顺序不能乱）
        init0(props);

        Aop.getAsyn(SqlSessionFactoryBuilder.class, bw -> {
            factoryBuilder = bw.raw();
        });
    }

    protected void initConfiguration(Environment environment) {
        config = new Configuration(environment);
    }

    private void init0(Properties props) {
        if (props != null) {
            props.forEach((k, v) -> {
                if (k instanceof String && v instanceof String) {
                    String key = (String) k;
                    String valStr = (String) v;

                    if (key.startsWith("typeAliases[") || key.equals("typeAliases")) {
                        for (String val : valStr.split(",")) {
                            val = val.trim();
                            if (val.length() == 0) {
                                continue;
                            }

                            if (val.endsWith(".class")) {
                                //type class
                                Class<?> clz = Utils.loadClass(val.substring(0, val.length() - 6));
                                if (clz != null) {
                                    getConfiguration().getTypeAliasRegistry().registerAlias(clz);
                                }
                            } else {
                                //package
                                getConfiguration().getTypeAliasRegistry().registerAliases(val);
                            }
                        }
                    }
                }
            });

            //支持包名和xml
            props.forEach((k, v) -> {
                if (k instanceof String && v instanceof String) {
                    String key = (String) k;
                    String valStr = (String) v;

                    if (key.startsWith("mappers[") || key.equals("mappers")) {
                        for (String val : valStr.split(",")) {
                            val = val.trim();
                            if (val.length() == 0) {
                                continue;
                            }

                            if (val.endsWith(".xml")) {
                                //mapper xml
                                addMappersByXml(val);
                                mappers.add(val);
                            } else if (val.endsWith(".class")) {
                                //mapper class
                                Class<?> clz = Utils.loadClass(val.substring(0, val.length() - 6));
                                if (clz != null) {
                                    getConfiguration().addMapper(clz);
                                    mappers.add(val);
                                }
                            } else {
                                //package
                                getConfiguration().addMappers(val);
                                mappers.add(val);
                            }
                        }
                    }
                }
            });

            if (mappers.size() == 0) {
                throw new RuntimeException("Please add the mappers configuration!");
            }
        }
    }

    private void addMappersByXml(String val) {
        try {
            // resource 配置方式
            ErrorContext.instance().resource(val);
            /**
             * 读取mapper文件
             */
            InputStream stream = Resources.getResourceAsStream(val);
            /**
             * mapper映射文件都是通过XMLMapperBuilder解析
             */
            XMLMapperBuilder mapperParser = new XMLMapperBuilder(stream, getConfiguration(), val, getConfiguration().getSqlFragments());
            mapperParser.parse();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取配置器
     */
    @Override
    public Configuration getConfiguration() {
        return config;
    }

    /**
     * 获取会话工厂
     */
    @Override
    public SqlSessionFactory getFactory() {
        if (factory == null) {
            factory = factoryBuilder.build(getConfiguration());//new SqlSessionFactoryProxy(factoryBuilder.build(config));
        }

        return factory;
    }

    private SqlSession session;

    @Override
    public SqlSession getSession() {
        if (session == null) {
            session = getFactory().openSession();
        }

        return session;
    }

    @Override
    public List<String> getMappers() {
        return mappers;
    }
}
