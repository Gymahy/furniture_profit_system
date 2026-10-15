package utility;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import config.Config;

import java.sql.Connection;
import java.sql.SQLException;

public final class DBConnection {
    private static final HikariDataSource DATA_SOURCE;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Config.getProperty("db.url"));
        config.setUsername(Config.getProperty("db.user"));
        config.setPassword(Config.getProperty("db.pass"));
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(Config.getIntProperty("db.pool.size", 10));
        config.setPoolName("furniture-sales-pool");
        DATA_SOURCE = new HikariDataSource(config);
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static void closeDataSource() {
        if (!DATA_SOURCE.isClosed()) {
            DATA_SOURCE.close();
        }
    }
}
