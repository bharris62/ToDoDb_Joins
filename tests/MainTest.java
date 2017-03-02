import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Created by BHarris on 3/2/17.
 */
public class MainTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }
    @Test
    public void testUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn, "Blake");
        User user = Main.selectUser(conn, "Blake");
        conn.close();

        assertTrue(user != null);
        assertTrue(user.userName.equals("Blake"));
    }

    @Test
    public void testToDo() throws SQLException {
        Connection conn = startConnection();

    }
}
