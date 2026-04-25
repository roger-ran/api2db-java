package com.example.api2db.service;

import com.example.api2db.model.DatabaseConnection;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源管理器
 * 负责管理和缓存多个数据库连接池
 */
@Service
public class DynamicDataSourceManager {

    private static final Logger log = LoggerFactory.getLogger(DynamicDataSourceManager.class);

    /**
     * 连接池缓存：connection_id -> DataSource
     */
    private final Map<String, DataSource> dataSourceCache = new ConcurrentHashMap<>();

    /**
     * 数据库连接配置缓存：connection_id -> DatabaseConnection
     */
    private final Map<String, DatabaseConnection> connectionConfigCache = new ConcurrentHashMap<>();

    /**
     * 获取或创建数据源
     * @param connectionId 连接 ID
     * @param dbConfig 数据库配置
     * @return DataSource
     */
    public DataSource getOrCreateDataSource(String connectionId, DatabaseConnection dbConfig) {
        return dataSourceCache.computeIfAbsent(connectionId, id -> {
            log.info("Creating new data source for connection: {}", connectionId);
            connectionConfigCache.put(id, dbConfig);
            return createDataSource(dbConfig);
        });
    }

    /**
     * 创建 HikariCP 数据源
     */
    private DataSource createDataSource(DatabaseConnection dbConfig) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(dbConfig.getDriverClassName());
        config.setJdbcUrl(dbConfig.getJdbcUrl());
        config.setUsername(dbConfig.getUsername());
        config.setPassword(dbConfig.getPassword());

        // 连接池配置
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30 秒
        config.setIdleTimeout(600000); // 10 分钟
        config.setMaxLifetime(1800000); // 30 分钟

        // 连接池名称
        config.setPoolName("HikariPool-" + dbConfig.getId());

        log.debug("HikariCP config created for database: {} ({})", dbConfig.getDatabase(), dbConfig.getType());

        return new HikariDataSource(config);
    }

    /**
     * 获取已缓存的数据源
     * @param connectionId 连接 ID
     * @return DataSource，如果不存在则返回 null
     */
    public DataSource getDataSource(String connectionId) {
        return dataSourceCache.get(connectionId);
    }

    /**
     * 测试数据库连接是否可用
     * @param connectionId 连接 ID
     * @return true 如果连接可用，否则返回 false
     */
    public boolean testConnection(String connectionId) {
        DataSource dataSource = getDataSource(connectionId);
        if (dataSource == null) {
            return false;
        }

        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5 秒超时
        } catch (SQLException e) {
            log.error("Failed to test connection for {}: {}", connectionId, e.getMessage());
            return false;
        }
    }

    /**
     * 移除并关闭指定的数据源
     * @param connectionId 连接 ID
     */
    public void removeDataSource(String connectionId) {
        DataSource dataSource = dataSourceCache.remove(connectionId);
        connectionConfigCache.remove(connectionId);

        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
            log.info("Closed data source for connection: {}", connectionId);
        }
    }

    /**
     * 关闭所有数据源
     */
    public void shutdown() {
        log.info("Shutting down all data sources...");
        dataSourceCache.forEach((id, dataSource) -> {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        });
        dataSourceCache.clear();
        connectionConfigCache.clear();
        log.info("All data sources closed");
    }

    /**
     * 获取缓存的连接数量
     */
    public int getCachedDataSourceCount() {
        return dataSourceCache.size();
    }

    /**
     * 检查连接是否已缓存
     */
    public boolean hasDataSource(String connectionId) {
        return dataSourceCache.containsKey(connectionId);
    }
}