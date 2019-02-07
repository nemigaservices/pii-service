package net.nemiga.samples.piiservice.data.sql;

import com.google.gson.JsonObject;
import net.nemiga.samples.piiservice.data.DataException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/*
    !!! Do not forget to start SQL Proxy! ./runCloudSQLProxy.sh
 */
@RunWith(JUnit4.class)
public class AuditTest {

    private PIISqlDataAccess auditDb;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws DataException {
        String url = "jdbc:mysql://dbuser:dbuser@127.0.0.1:3306/pii";
        auditDb = new PIISqlDataAccess(url);
    }

    @Test
    public void testAddAuditData() throws DataException, SQLException {
        JsonObject pii = new JsonObject();
        pii.addProperty("name", "Test");
        pii.addProperty("phone", "555-555-5555");
        pii.addProperty("email", "test@test.com");

        this.auditDb.recordChange("test", -1, pii);

        String testStr = this.auditDb.getAuditDataAsStringForTesting("test", -1);

        assertEquals(
            "test, -1, name, Test\n"
            + "test, -1, phone, 555-555-5555\n"
            + "test, -1, email, test@test.com\n",
            testStr);
    }

    @After
    public void cleanUp() throws SQLException {
        auditDb.deleteTestData("test", -1);
    }
}
