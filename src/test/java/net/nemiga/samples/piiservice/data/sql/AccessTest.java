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

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class AccessTest {

    private PIISqlDataAccess accessDb;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws DataException {
        String url = "jdbc:mysql://dbuser:dbuser@127.0.0.1:3306/pii";
        accessDb = new PIISqlDataAccess(url);
    }

    @Test
    public void testApiAccess() throws DataException {
        assertTrue(this.accessDb.isMethodAllowed("AIzaSyCBCFX7_YoiaVMaS0kkaYGhykcF-xdPYTs", PIISqlDataAccess.METHOD.GET));
        assertFalse(this.accessDb.isMethodAllowed("AIzaSyCBCFX7_YoiaVMaS0kkaYGhykcF-xdPYTs", PIISqlDataAccess.METHOD.PUT));
    }
}
