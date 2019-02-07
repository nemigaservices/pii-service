package net.nemiga.samples.piiservice.processors;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ReturnedDataProcessorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private ReturnedDataProcessor processor = new ReturnedDataProcessor();
    private JsonObject pii;

    @Before
    public void setUp(){
        pii = new JsonObject();
        pii.addProperty("name", "Test");
        pii.addProperty("phone", "555-555-5555");
        pii.addProperty("email", "test@test.com");
    }


    @Test
    public void testProcessor() throws DataProcessorException {
        JsonObject ret  = processor.removedUnneededFields(pii, "name,phone");
        assertEquals("{\"name\":\"Test\",\"phone\":\"555-555-5555\"}", ret.toString());
    }

    @Test(expected = DataProcessorException.class)
    public void testProcessorException() throws DataProcessorException {
        processor.removedUnneededFields(pii, "name,phone1");
    }

}
