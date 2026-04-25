package com.example.api2db.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * 单条 SQL 执行结果
 */
public class SqlResult {
    /**
     * 执行的 SQL 语句
     */
    private String sql;

    /**
     * SQL 类型：QUERY（查询）或 EXECUTE（更新）
     */
    private String type;

    /**
     * 查询结果数据（QUERY 类型）
     */
    private List<Map<String, Object>> data;

    /**
     * 受影响的行数（EXECUTE 类型）
     */
    private int affectedRows;

    public static SqlResult queryResult(String sql, List<Map<String, Object>> data) {
        SqlResult result = new SqlResult();
        result.setSql(sql);
        result.setType("QUERY");
        result.setData(data);
        result.setAffectedRows(0);
        return result;
    }

    public static SqlResult executeResult(String sql, int affectedRows) {
        SqlResult result = new SqlResult();
        result.setSql(sql);
        result.setType("EXECUTE");
        result.setData(null);
        result.setAffectedRows(affectedRows);
        return result;
    }

    // Getters and Setters
    @JsonProperty("sql")
    public String getSql() {
        return sql;
    }

    @JsonProperty("sql")
    public void setSql(String sql) {
        this.sql = sql;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("data")
    public List<Map<String, Object>> getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    @JsonProperty("affected_rows")
    public int getAffectedRows() {
        return affectedRows;
    }

    @JsonProperty("affected_rows")
    public void setAffectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
    }
}