package com.rental.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 数据库自动初始化
 * 应用启动时检测并创建表结构，初始化数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            if (!isTableExists("sys_user")) {
                log.info("检测到数据库未初始化，开始执行初始化脚本...");
                executeInitScript();
                log.info("数据库初始化完成");
            } else {
                log.info("数据库已初始化，跳过");
            }
            // 确保管理员密码正确
            ensureAdminPassword();
        } catch (Exception e) {
            log.error("数据库初始化失败: {}", e.getMessage(), e);
        }
    }

    private boolean isTableExists(String tableName) {
        try {
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE 1=0", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void executeInitScript() throws Exception {
        ClassPathResource resource = new ClassPathResource("db/init.sql");
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, resource);
        }
    }

    private void ensureAdminPassword() {
        try {
            String encodedPassword = passwordEncoder.encode("admin123");
            jdbcTemplate.update(
                "UPDATE sys_user SET password = ? WHERE username = ?",
                encodedPassword, "admin");
            log.info("管理员密码已设置为: admin123");
        } catch (Exception e) {
            log.warn("更新管理员密码失败: {}", e.getMessage());
        }
    }
}
