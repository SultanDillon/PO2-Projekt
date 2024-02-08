import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;
import org.testng.annotations.AfterTest;

import java.sql.*;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ApplicationTests {
    private final LoginManager logman = new LoginManager(true);
    private final Logger logger = LogManager.getLogger(ApplicationTests.class);
    private final WypozyczalniaDVDFrame frame = new WypozyczalniaDVDFrame();
    private final WypozyczalniaDVDPracownik pracownik = new WypozyczalniaDVDPracownik();

    @Test
    public void testAuth() {
        boolean wynik = logman.authenticate("admin", "admin".toCharArray());
        if(wynik) {
            logger.info("Zalogowano jak admin");
        }
        assertTrue(wynik);
    }

    @Test
    public void testDB() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:wypozyczalnia_dvd.db");
            assertNotNull(connection);
            logger.info("Połączono z bazą");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Błąd połączenia z bazą danych");
        }
    }

    @Test
    public void testDBinit() {
        boolean wynik = WypozyczalniaDVDFrame.initializeDatabase();
        assertTrue(wynik);
    }

    @Test
    public void testAddDVD() {
        boolean wynik = pracownik.addDVD("Test");
        assertTrue(wynik);
    }

    @AfterTest
    public static void cleanup() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:wypozyczalnia_dvd.db");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM dvd WHERE title = 'Test'")) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
