import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/** Chép một lần dữ liệu từ PostgreSQL của dịch vụ Render vào MySQL trống. */
final class RenderDataTransfer {
    static void copy(Connection target, String sourceUrl) throws SQLException {
        try (Connection source = connect(sourceUrl)) {
            source.setReadOnly(true);
            source.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            source.setAutoCommit(false);
            target.setAutoCommit(false);
            int total = 0;
            try {
                for (String table : Bookstore.TABLES) {
                    total += copyTable(source, target, table);
                }
                target.commit();
            } catch (SQLException | RuntimeException ex) {
                target.rollback();
                throw ex;
            }
            System.out.println("Render migration verified " + total + " rows across " + Bookstore.TABLES.size() + " tables.");
        }
    }

    private static Connection connect(String sourceUrl) throws SQLException {
        URI uri;
        try {
            uri = URI.create(sourceUrl == null ? "" : sourceUrl);
            if (!("postgres".equals(uri.getScheme()) || "postgresql".equals(uri.getScheme())) ||
                    uri.getHost() == null || uri.getRawUserInfo() == null || uri.getRawPath().isBlank()) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            throw new SQLException("The Render PostgreSQL connection is missing or invalid.");
        }
        String[] credentials = uri.getRawUserInfo().split(":", 2);
        Properties options = new Properties();
        options.setProperty("user", decode(credentials[0]));
        options.setProperty("password", credentials.length == 2 ? decode(credentials[1]) : "");
        options.setProperty("connectTimeout", "10");
        options.setProperty("socketTimeout", "45");
        String jdbc = "jdbc:postgresql://" + uri.getHost() + (uri.getPort() < 0 ? "" : ":" + uri.getPort()) +
                uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        return DriverManager.getConnection(jdbc, options);
    }

    private static String decode(String component) {
        return URLDecoder.decode(component.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private static int copyTable(Connection source, Connection target, String table) throws SQLException {
        try (var read = source.createStatement();
             var rows = read.executeQuery("SELECT * FROM bookstore_v2." + table)) {
            var metadata = rows.getMetaData();
            var columns = new ArrayList<String>();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                columns.add(metadata.getColumnName(i));
            }
            String columnList = String.join(",", columns);
            String parameters = String.join(",", Collections.nCopies(columns.size(), "?"));
            var expected = new HashSet<List<Object>>();
            try (var insert = target.prepareStatement("INSERT INTO " + table + "(" + columnList + ") VALUES(" + parameters + ")")) {
                while (rows.next()) {
                    List<Object> values = values(rows);
                    for (int i = 0; i < values.size(); i++) {
                        insert.setObject(i + 1, values.get(i));
                    }
                    insert.addBatch();
                    expected.add(values);
                }
                insert.executeBatch();
            }
            int count = expected.size();
            try (var check = target.createStatement();
                 var inserted = check.executeQuery("SELECT " + columnList + " FROM " + table)) {
                while (inserted.next()) {
                    if (!expected.remove(values(inserted))) {
                        throw new SQLException("Migrated row differs in " + table);
                    }
                }
                if (!expected.isEmpty()) {
                    throw new SQLException("Migrated rows are missing in " + table);
                }
            }
            return count;
        }
    }

    private static List<Object> values(ResultSet row) throws SQLException {
        var metadata = row.getMetaData();
        var values = new ArrayList<Object>();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            Object value = switch (metadata.getColumnType(i)) {
                case Types.DATE -> row.getObject(i, LocalDate.class);
                case Types.TIMESTAMP -> row.getObject(i, LocalDateTime.class);
                default -> row.getObject(i);
            };
            values.add(value);
        }
        return values;
    }
}
