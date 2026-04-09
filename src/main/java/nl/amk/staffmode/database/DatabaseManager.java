package nl.amk.staffmode.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.UUID;

public final class DatabaseManager {

    private final JavaPlugin plugin;
    private volatile HikariDataSource dataSource;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        FileConfiguration cfg = plugin.getConfig();
        String host = cfg.getString("database.host", "localhost");
        int port = cfg.getInt("database.port", 3306);
        String database = cfg.getString("database.database", "minecraft");
        String username = cfg.getString("database.username", "root");
        String password = cfg.getString("database.password", "");
        boolean useSSL = cfg.getBoolean("database.useSSL", false);
        boolean allowPublicKeyRetrieval = cfg.getBoolean("database.allowPublicKeyRetrieval", true);
        int maximumPoolSize = cfg.getInt("database.pool.maximumPoolSize", 10);
        int minimumIdle = cfg.getInt("database.pool.minimumIdle", 2);
        long connectionTimeoutMs = cfg.getLong("database.pool.connectionTimeoutMs", Duration.ofSeconds(10).toMillis());
        long idleTimeoutMs = cfg.getLong("database.pool.idleTimeoutMs", Duration.ofMinutes(1).toMillis());
        long maxLifetimeMs = cfg.getLong("database.pool.maxLifetimeMs", Duration.ofMinutes(30).toMillis());
        long leakDetectionThresholdMs = cfg.getLong("database.pool.leakDetectionThresholdMs", 0L);

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=" + useSSL +
                "&allowPublicKeyRetrieval=" + allowPublicKeyRetrieval +
                "&serverTimezone=UTC" +
                "&characterEncoding=utf8";

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(jdbcUrl);
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(Math.max(1, maximumPoolSize));
        hikari.setMinimumIdle(Math.max(0, minimumIdle));
        hikari.setConnectionTimeout(Math.max(250L, connectionTimeoutMs));
        hikari.setIdleTimeout(Math.max(0L, idleTimeoutMs));
        hikari.setMaxLifetime(Math.max(0L, maxLifetimeMs));
        if (leakDetectionThresholdMs > 0) {
            hikari.setLeakDetectionThreshold(leakDetectionThresholdMs);
        }
        hikari.setPoolName("StaffMode-Hikari");
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikari.addDataSourceProperty("useServerPrepStmts", "true");

        HikariDataSource ds = new HikariDataSource(hikari);
        dataSource = ds;

        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement("""
                     CREATE TABLE IF NOT EXISTS staffmode_players (
                       uuid CHAR(36) NOT NULL,
                       enabled TINYINT(1) NOT NULL,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       PRIMARY KEY (uuid)
                     ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                     """)) {
            ps.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            stop();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public void stop() {
        HikariDataSource ds = dataSource;
        dataSource = null;
        if (ds != null) {
            ds.close();
        }
    }

    public void setStaffModeAsync(UUID uuid, boolean enabled) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> setStaffMode(uuid, enabled));
    }

    public void setStaffMode(UUID uuid, boolean enabled) {
        HikariDataSource ds = dataSource;
        if (ds == null) {
            return;
        }
        try (Connection connection = ds.getConnection();
             PreparedStatement ps = connection.prepareStatement("""
                     INSERT INTO staffmode_players (uuid, enabled)
                     VALUES (?, ?)
                     ON DUPLICATE KEY UPDATE enabled = VALUES(enabled), updated_at = CURRENT_TIMESTAMP
                     """)) {
            ps.setString(1, uuid.toString());
            ps.setBoolean(2, enabled);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to update staffmode state: " + e.getMessage());
        }
    }
}
