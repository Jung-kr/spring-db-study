package spring.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static spring.jdbc.connection.ConnectionConst.*;

/**
 * JDBC를 사용해서 실제 데이터베이스에 연결
 */
@Slf4j
public class DBConnectionUtil {

    public static Connection getConnection() throws SQLException {

        Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("get connection={}, class={}", connection, connection.getClass());
        return connection;
    }
}
