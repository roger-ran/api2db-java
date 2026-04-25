package com.example.api2db.model;

/**
 * 数据库连接配置
 */
public class DatabaseConnection {
    /**
     * 连接 ID
     */
    private String id;

    /**
     * 数据库类型：mysql, postgresql, oracle, sqlserver
     */
    private String type;

    /**
     * 数据库主机地址
     */
    private String host;

    /**
     * 数据库端口
     */
    private int port;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    /**
     * 数据库名称
     */
    private String database;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * 根据数据库类型获取 JDBC URL
     */
    public String getJdbcUrl() {
        switch (type.toLowerCase()) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s", host, port, database);
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            case "oracle":
                return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, database);
            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s", host, port, database);
            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }

    /**
     * 根据数据库类型获取 JDBC 驱动类名
     */
    public String getDriverClassName() {
        switch (type.toLowerCase()) {
            case "mysql":
                return "com.mysql.cj.jdbc.Driver";
            case "postgresql":
                return "org.postgresql.Driver";
            case "oracle":
                return "oracle.jdbc.OracleDriver";
            case "sqlserver":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }
}