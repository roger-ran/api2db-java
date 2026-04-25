package com.example.api2db.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 安全过滤器
 * 负责检查 SQL 语句是否符合白名单和黑名单规则
 */
@Service
public class SqlSecurityFilter {

    private static final Logger log = LoggerFactory.getLogger(SqlSecurityFilter.class);

    @Value("${security.whitelist.file:/config/whitelist.txt}")
    private String whitelistFile;

    @Value("${security.blacklist.file:/config/blacklist.txt}")
    private String blacklistFile;

    private Set<String> whitelist = new HashSet<>();
    private Set<String> blacklist = new HashSet<>();

    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
        "\\b(DROP|TRUNCATE|ALTER|CREATE|DELETE|INSERT|UPDATE|SELECT|EXEC|EXECUTE|GRANT|REVOKE)\\b",
        Pattern.CASE_INSENSITIVE
    );

    @PostConstruct
    public void init() {
        loadWhitelist();
        loadBlacklist();
        log.info("SQL Security Filter initialized with {} whitelist keywords and {} blacklist keywords",
                 whitelist.size(), blacklist.size());
    }

    /**
     * 加载白名单
     */
    private void loadWhitelist() {
        File whitelistFileObj = new File(whitelistFile);
        if (!whitelistFileObj.exists()) {
            log.warn("Whitelist file not found: {}, using default whitelist", whitelistFile);
            // 设置默认白名单
            whitelist.add("SELECT");
            whitelist.add("INSERT");
            whitelist.add("UPDATE");
            whitelist.add("DELETE");
            log.info("Using default whitelist: {}", whitelist);
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(whitelistFileObj), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    whitelist.add(line.toUpperCase());
                }
            }
            log.info("Loaded whitelist: {}", whitelist);
        } catch (IOException e) {
            log.warn("Failed to load whitelist from {}: {}", whitelistFile, e.getMessage());
            // 设置默认白名单
            whitelist.add("SELECT");
            whitelist.add("INSERT");
            whitelist.add("UPDATE");
            whitelist.add("DELETE");
            log.info("Using default whitelist: {}", whitelist);
        }
    }

    /**
     * 加载黑名单
     */
    private void loadBlacklist() {
        File blacklistFileObj = new File(blacklistFile);
        if (!blacklistFileObj.exists()) {
            log.warn("Blacklist file not found: {}, using default blacklist", blacklistFile);
            // 设置默认黑名单
            blacklist.add("DROP");
            blacklist.add("TRUNCATE");
            blacklist.add("ALTER");
            blacklist.add("CREATE");
            log.info("Using default blacklist: {}", blacklist);
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(blacklistFileObj), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    blacklist.add(line.toUpperCase());
                }
            }
            log.info("Loaded blacklist: {}", blacklist);
        } catch (IOException e) {
            log.warn("Failed to load blacklist from {}: {}", blacklistFile, e.getMessage());
            // 设置默认黑名单
            blacklist.add("DROP");
            blacklist.add("TRUNCATE");
            blacklist.add("ALTER");
            blacklist.add("CREATE");
            log.info("Using default blacklist: {}", blacklist);
        }
    }

    /**
     * 检查 SQL 语句是否安全
     * @param SQL 语句
     * @throws SecurityException 如果 SQL 不安全
     */
    public void validate(String sql) throws SecurityException {
        if (sql == null || sql.trim().isEmpty()) {
            throw new SecurityException("SQL statement cannot be empty");
        }

        // 检查黑名单
        for (String blackKeyword : blacklist) {
            if (containsKeyword(sql, blackKeyword)) {
                throw new SecurityException("SQL contains forbidden keyword: " + blackKeyword);
            }
        }

        // 检查白名单（至少要包含一个白名单关键字）
        boolean hasWhitelistedKeyword = false;
        for (String whiteKeyword : whitelist) {
            if (containsKeyword(sql, whiteKeyword)) {
                hasWhitelistedKeyword = true;
                break;
            }
        }

        if (!hasWhitelistedKeyword && !whitelist.isEmpty()) {
            throw new SecurityException("SQL does not contain any whitelisted keyword");
        }

        log.debug("SQL validation passed: {}", sql.substring(0, Math.min(50, sql.length())));
    }

    /**
     * 检查 SQL 语句是否包含指定关键字（不区分大小写）
     */
    private boolean containsKeyword(String sql, String keyword) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(sql).find();
    }

    /**
     * 提取 SQL 语句中的关键字
     */
    public String extractFirstKeyword(String sql) {
        Matcher matcher = KEYWORD_PATTERN.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        return "UNKNOWN";
    }

    /**
     * 判断 SQL 是否为查询语句
     */
    public boolean isQuery(String sql) {
        return extractFirstKeyword(sql).equals("SELECT");
    }
}