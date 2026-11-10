import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/** Tạo và dọn một CSDL MySQL riêng cho từng bộ kiểm tra. */
public class MysqlTests {
    private static final String DATABASE = "bookstore_test";

    public static void main(String[] args) throws Exception {
        String url = Bookstore.DATABASE_URL;
        try (Connection c = Bookstore.db(); Statement ddl = c.createStatement()) {
            if (DATABASE.equalsIgnoreCase(c.getCatalog())) {
                throw new IllegalStateException("Cấu hình web phải dùng CSDL khác " + DATABASE + ".");
            }
            int queryStart = url.indexOf('?');
            int pathEnd = queryStart < 0 ? url.length() : queryStart;
            String testUrl = url.substring(0, url.lastIndexOf('/', pathEnd - 1) + 1) + DATABASE + url.substring(pathEnd);
            for (Class<?> suite : List.of(ModelTest.class, CatalogActionsTest.class, EmployeeDeleteTest.class, QuickChangesTest.class)) {
                // Không dùng IF NOT EXISTS: chỉ xóa CSDL do lần chạy này tạo.
                ddl.executeUpdate("CREATE DATABASE " + DATABASE + " CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_bin");
                try {
                    String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
                    ProcessBuilder process = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), suite.getName());
                    process.environment().put("BOOKSTORE_DB_URL", testUrl);
                    if (process.inheritIO().start().waitFor() != 0) {
                        throw new AssertionError(suite.getSimpleName() + " failed");
                    }
                } finally {
                    ddl.executeUpdate("DROP DATABASE " + DATABASE);
                }
            }
        }
    }

    static void initialize() throws Exception {
        try (Connection c = Bookstore.db()) {
            if (!DATABASE.equalsIgnoreCase(c.getCatalog()) ||
                    !Bookstore.query(c, "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA=DATABASE() LIMIT 1").isEmpty()) {
                throw new SQLException("Bộ kiểm tra cần CSDL bookstore_test trống. Hãy chạy KIEM-TRA.bat.");
            }
        }
        Bookstore.initialize();
    }
}
