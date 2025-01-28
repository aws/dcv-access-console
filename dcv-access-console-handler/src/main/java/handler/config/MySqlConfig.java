// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@ConditionalOnProperty(name = "persistence-db", havingValue = "mysql")
@Configuration
public class MySqlConfig {
    @Value("${table-name-prefix:dcv_sm_ui_}")
    private String prefix;

    @Bean
    public DataSource provideDataSource(@Value("${jdbc-connection-url}") String url, @Value("${jdbc-user}") String username, @Value("${jdbc-password}") String password) {
        return DataSourceBuilder.create().url(url)
                .username(username)
                .password(password)
                .build();
    }

    @Bean
    public ImplicitNamingStrategy implicit() {
        return new ImplicitNamingStrategyLegacyJpaImpl();
    }

    @Bean
    public PhysicalNamingStrategyStandardImpl physicalNamingStrategyStandard(){
        return new PhysicalNamingImpl();
    }

    class PhysicalNamingImpl extends PhysicalNamingStrategyStandardImpl {
        @Override
        public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
            return switch (name.getText()) {
                case "SessionTemplate" -> new Identifier(prefix + "SessionTemplate", name.isQuoted());
                case "SessionTemplatePublishedToUser" -> new Identifier(prefix + "SessionTemplatePublishedToUser", name.isQuoted());
                case "UserEntity" -> new Identifier(prefix + "User", name.isQuoted());
                case "UserGroupEntity" -> new Identifier(prefix + "UserGroup", name.isQuoted());
                case "SessionTemplatePublishedToUserGroup" -> new Identifier(prefix + "SessionTemplatePublishedToUserGroup", name.isQuoted());
                case "UserGroupUserMembership" -> new Identifier(prefix + "UserGroupUserMembership", name.isQuoted());
                default -> super.toPhysicalTableName(name, context);
            };
        }
    }
}
