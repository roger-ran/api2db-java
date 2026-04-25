package com.example.api2db.config;

import com.example.api2db.model.DatabaseConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置加载器
 * 负责加载和解析 config.yaml 配置文件
 */
@Component
public class ConfigLoader {

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    @Value("${config.file:/config/config.yaml}")
    private String configFilePath;

    /**
     * 认证配置：api_key -> connection_ids
     */
    private Map<String, List<String>> authConfig = new HashMap<>();

    /**
     * 数据库连接配置：connection_id -> DatabaseConnection
     */
    private Map<String, DatabaseConnection> databaseConfig = new HashMap<>();

    @PostConstruct
    public void loadConfig() {
        try {
            String yamlContent = readConfigFile();
            parseYamlConfig(yamlContent);
            log.info("Configuration loaded successfully");
            log.info("Auth config keys: {}", authConfig.keySet());
            log.info("Database connections: {}", databaseConfig.keySet());
        } catch (IOException e) {
            log.error("Failed to load configuration from {}: {}", configFilePath, e.getMessage());
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    /**
     * 读取配置文件内容
     */
    private String readConfigFile() throws IOException {
        File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            throw new IOException("Config file not found: " + configFilePath);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    /**
     * 解析 YAML 配置
     */
    private void parseYamlConfig(String yamlContent) {
        // 简单的 YAML 解析（实际项目中建议使用 SnakeYAML）
        String[] lines = yamlContent.split("\n");

        String currentApiKey = null;
        String currentConnectionId = null;
        DatabaseConnection currentDb = null;

        int indent = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("#") || trimmed.isEmpty()) {
                continue;
            }

            int currentIndent = line.indexOf(trimmed);

            if (trimmed.startsWith("auth_config:")) {
                // 开始解析认证配置
                continue;
            } else if (trimmed.startsWith("- api_key:")) {
                // 开始解析 API key
                currentApiKey = trimmed.split(":", 2)[1].trim().replaceAll("^\"|\"$", "");
                authConfig.put(currentApiKey, new ArrayList<>());
            } else if (trimmed.startsWith("- id:")) {
                // 开始解析数据库连接 ID
                currentConnectionId = trimmed.split(":", 2)[1].trim().replaceAll("^\"|\"$", "");
                if (currentApiKey != null) {
                    authConfig.get(currentApiKey).add(currentConnectionId);
                }
                currentDb = new DatabaseConnection();
                currentDb.setId(currentConnectionId);
                databaseConfig.put(currentConnectionId, currentDb);
            } else if (trimmed.startsWith("type:")) {
                if (currentDb != null) {
                    currentDb.setType(trimmed.split(":", 2)[1].trim().replaceAll("^\"|\"$", ""));
                }
            } else if (trimmed.startsWith("host:")) {
                if (currentDb != null) {
                    currentDb.setHost(trimmed.split(":", 2)[1].trim().replaceAll("^\"|\"$", ""));
                }
            } else if (trimmed.startsWith("port:")) {
                if (currentDb != null) {
                    currentDb.setPort(Integer.parseInt(trimmed.split(":", 2)[1].trim()));
                }
            } else if (trimmed.startsWith("username:")) {
                if (currentDb != null) {
                    currentDb.setUsername(trimmed.split(":", 2)[1].trim().replaceAll("^\"|\"$", ""));
                }
            } else if (trimmed.startsWith("password:")) {
                if (currentDb != null) {
                    currentDb.setPassword(trimmed.split(":", 2)[1].trim().replaceAll("^\"|\"$", ""));
                }
            } else if (trimmed.startsWith("database:")) {
                if (currentDb != null) {
                    currentDb.setDatabase(trimmed.split(":", 2)[1].trim().replaceAll("^\"|\"$", ""));
                }
            }
        }
    }

    /**
     * 验证 API key 和连接 ID 是否匹配
     * @param apiKey API key
     * @param connectionId 连接 ID
     * @return true 如果匹配，否则返回 false
     */
    public boolean validateAuth(String apiKey, String connectionId) {
        List<String> connectionIds = authConfig.get(apiKey);
        if (connectionIds == null) {
            log.warn("API key not found: {}", apiKey);
            return false;
        }

        if (!connectionIds.contains(connectionId)) {
            log.warn("Connection ID {} not authorized for API key {}", connectionId, apiKey);
            return false;
        }

        return true;
    }

    /**
     * 获取数据库连接配置
     * @param connectionId 连接 ID
     * @return DatabaseConnection，如果不存在则返回 null
     */
    public DatabaseConnection getDatabaseConfig(String connectionId) {
        return databaseConfig.get(connectionId);
    }

    /**
     * 获取所有 API keys
     */
    public Map<String, List<String>> getAuthConfig() {
        return new HashMap<>(authConfig);
    }

    /**
     * 获取所有数据库连接配置
     */
    public Map<String, DatabaseConnection> getDatabaseConfig() {
        return new HashMap<>(databaseConfig);
    }
}