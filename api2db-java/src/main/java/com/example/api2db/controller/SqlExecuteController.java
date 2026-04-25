package com.example.api2db.controller;

import com.example.api2db.config.ConfigLoader;
import com.example.api2db.exception.AuthenticationException;
import com.example.api2db.exception.DatabaseException;
import com.example.api2db.exception.SqlSecurityException;
import com.example.api2db.model.DatabaseConnection;
import com.example.api2db.model.ExecuteRequest;
import com.example.api2db.model.ExecuteResponse;
import com.example.api2db.model.SqlResult;
import com.example.api2db.service.DynamicDataSourceManager;
import com.example.api2db.service.SqlSecurityFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL 执行控制器
 * 提供 SQL 执行的 REST API 接口
 */
@RestController
@RequestMapping("/api/v1")
public class SqlExecuteController {

    private static final Logger log = LoggerFactory.getLogger(SqlExecuteController.class);

    @Autowired
    private ConfigLoader configLoader;

    @Autowired
    private DynamicDataSourceManager dataSourceManager;

    @Autowired
    private SqlSecurityFilter sqlSecurityFilter;

    /**
     * 执行 SQL 语句
     * @param request 执行请求
     * @return 执行响应
     */
    @PostMapping("/execute")
    public ResponseEntity<ExecuteResponse> executeSql(@RequestBody ExecuteRequest request) {
        log.info("Received SQL execution request from API key: {}, connection: {}",
                 request.getApiKey(), request.getConnectionId());

        // 1. 参数验证
        if (request.getApiKey() == null || request.getApiKey().isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }
        if (request.getConnectionId() == null || request.getConnectionId().isEmpty()) {
            throw new IllegalArgumentException("Connection ID is required");
        }
        if (request.getSqls() == null || request.getSqls().isEmpty()) {
            throw new IllegalArgumentException("SQL statements are required");
        }

        // 2. 鉴权验证
        if (!configLoader.validateAuth(request.getApiKey(), request.getConnectionId())) {
            throw new AuthenticationException("Invalid API key or unauthorized connection ID");
        }

        // 3. 获取数据库配置
        DatabaseConnection dbConfig = configLoader.getDatabaseConfig(request.getConnectionId());
        if (dbConfig == null) {
            throw new DatabaseException("Database connection config not found: " + request.getConnectionId());
        }

        // 4. 获取数据源
        DataSource dataSource = dataSourceManager.getOrCreateDataSource(request.getConnectionId(), dbConfig);

        // 5. 创建 JDBC Template
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // 6. 执行 SQL 语句
        List<SqlResult> results = new ArrayList<>();
        try {
            for (String sql : request.getSqls()) {
                log.debug("Executing SQL: {}", sql);

                // 6.1 SQL 安全检查
                try {
                    sqlSecurityFilter.validate(sql);
                } catch (Exception e) {
                    throw new SqlSecurityException(e.getMessage());
                }

                // 6.2 执行 SQL
                SqlResult result = executeSingleSql(jdbcTemplate, sql);
                results.add(result);
                log.info("SQL executed successfully, affected rows: {}", result.getAffectedRows());
            }

            // 7. 返回成功结果
            ExecuteResponse response = ExecuteResponse.success(results);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("SQL execution failed: {}", e.getMessage(), e);
            // 返回部分执行结果
            ExecuteResponse response = ExecuteResponse.error(e.getMessage(), results);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 执行单条 SQL 语句
     */
    private SqlResult executeSingleSql(JdbcTemplate jdbcTemplate, String sql) {
        boolean isQuery = sqlSecurityFilter.isQuery(sql);

        if (isQuery) {
            // 执行查询语句
            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
            return SqlResult.queryResult(sql, data);
        } else {
            // 执行更新语句
            int affectedRows = jdbcTemplate.update(sql);
            return SqlResult.executeResult(sql, affectedRows);
        }
    }

    /**
     * 健康检查接口
     */
    @PostMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}