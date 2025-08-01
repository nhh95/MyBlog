package com.blog.myblog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
public class DbTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void pingDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            var rs = conn.createStatement().executeQuery("SELECT 1");
            rs.next();
            int result = rs.getInt(1);
            assert result == 1;   // 성공 시 green
        }
    }
}
