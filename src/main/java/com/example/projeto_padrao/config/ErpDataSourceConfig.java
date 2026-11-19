package com.example.projeto_padrao.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Datasource somente-leitura para o banco do ERP. So e criado quando "erp.datasource.url"
 * estiver preenchido em application.properties - ate la, a integracao com o ERP fica desativada
 * e a aplicacao funciona normalmente so com o banco local (H2).
 */
@Configuration
public class ErpDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "erp.datasource")
    @ConditionalOnProperty(prefix = "erp.datasource", name = "url")
    public HikariDataSource erpDataSource() {
        return new HikariDataSource();
    }

    @Bean
    @ConditionalOnProperty(prefix = "erp.datasource", name = "url")
    public JdbcTemplate erpJdbcTemplate(HikariDataSource erpDataSource) {
        return new JdbcTemplate(erpDataSource);
    }
}
