package com.example.api2db.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * SQL 执行请求 DTO
 */
public class ExecuteRequest {
    /**
     * API 密钥，用于鉴权
     */
    private String apiKey;

    /**
     * 连接 ID，对应配置中的数据库连接
     */
    private String connectionId;

    /**
     * 要执行的 SQL 语句列表
     */
    private List<String> sqls;

    // Getters and Setters
    @JsonProperty("api-key")
    public String getApiKey() {
        return apiKey;
    }

    @JsonProperty("api-key")
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @JsonProperty("connection_id")
    public String getConnectionId() {
        return connectionId;
    }

    @JsonProperty("connection_id")
    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    @JsonProperty("sqls")
    public List<String> getSqls() {
        return sqls;
    }

    @JsonProperty("sqls")
    public void setSqls(List<String> sqls) {
        this.sqls = sqls;
    }
}