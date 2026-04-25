package com.example.api2db.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * SQL 执行响应 DTO
 */
public class ExecuteResponse {
    /**
     * 执行是否成功
     */
    private boolean success;

    /**
     * 执行结果列表
     */
    private List<SqlResult> results;

    /**
     * 错误信息
     */
    private String error;

    public static ExecuteResponse success(List<SqlResult> results) {
        ExecuteResponse response = new ExecuteResponse();
        response.setSuccess(true);
        response.setResults(results);
        response.setError(null);
        return response;
    }

    public static ExecuteResponse error(String error, List<SqlResult> partialResults) {
        ExecuteResponse response = new ExecuteResponse();
        response.setSuccess(false);
        response.setResults(partialResults);
        response.setError(error);
        return response;
    }

    // Getters and Setters
    @JsonProperty("success")
    public boolean isSuccess() {
        return success;
    }

    @JsonProperty("success")
    public void setSuccess(boolean success) {
        this.success = success;
    }

    @JsonProperty("results")
    public List<SqlResult> getResults() {
        return results;
    }

    @JsonProperty("results")
    public void setResults(List<SqlResult> results) {
        this.results = results;
    }

    @JsonProperty("error")
    public String getError() {
        return error;
    }

    @JsonProperty("error")
    public void setError(String error) {
        this.error = error;
    }
}