package net.yiyutao.seata.spring.boot.autoconfigure;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.github.pagehelper.PageInterceptor;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.spring.annotation.GlobalTransactionScanner;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author masteryi
 * @version 1.0
 **/
@Configuration
@ConditionalOnClass({DruidDataSource.class,DataSourceProxy.class,SqlSessionFactory.class})
@ConditionalOnBean(DataSource.class)
public class SeataAutoConfiguration {

    /**
     * init durid datasource
     *
     * @return : druidDataSource  datasource instance
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public DruidDataSource druidDataSource(){
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * init datasource proxy
     * @param druidDataSource   datasource bean instance
     * @return : DataSourceProxy  datasource proxy
     */
    @Bean
    @ConditionalOnMissingBean
    public DataSourceProxy dataSourceProxy(DruidDataSource druidDataSource){
        return new DataSourceProxy(druidDataSource);
    }

    /**
     * init mybatis sqlSessionFactory
     * @param dataSourceProxy   datasource proxy
     * @return DataSourceProxy  datasource proxy
     */
    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(DataSourceProxy dataSourceProxy) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSourceProxy);
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:/mapper/*.xml"));
        factoryBean.setTransactionFactory(new JdbcTransactionFactory());
        //mybatis配置
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        //开启变量名驼峰自动转化
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);
        //插件配置,配置分页插件
        factoryBean.setPlugins(new Interceptor[]{new PageInterceptor()});
        return factoryBean.getObject();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.application.name", matchIfMissing = true)
    public GlobalTransactionScanner globalTransactionScanner(@Value("${spring.application.name}") String applicationName) {
        return new GlobalTransactionScanner(applicationName+"-seata", "my_test_tx_group");
    }
}
