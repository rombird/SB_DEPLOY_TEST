package com.example.demo.config;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class TxConfig {

    @Autowired
    private DataSource dataSource;

    // JPA Tx
    @Bean(name="jpaTransactionManager") // 이름 일치 트랙잭션을위한 빈의 이름
    public JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) { // entitymanagerfactory가 알아서 만들어지게되는데
        JpaTransactionManager transactionManager = new JpaTransactionManager();                     //JpaConfig에서 설정해줬음 <- 의존주의를 받는다고 보면 된다. 이렇게 만들어진 것에 JpaTranscationManager 생성
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }

}
