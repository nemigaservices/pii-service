/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.nemiga.samples.piiservice.data.piistorage;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.google.gson.JsonObject;
import net.nemiga.samples.piiservice.data.DataException;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.threeten.bp.Duration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Contains Cloud Datastore snippets demonstrating concepts for documentation.
 */
@RunWith(JUnit4.class)
public class PIIStorageTest {

  private static final LocalDatastoreHelper HELPER = LocalDatastoreHelper.create(1.0);
  private static final FullEntity<IncompleteKey> TEST_FULL_ENTITY = FullEntity.newBuilder().build();

  private Datastore datastore;
  private PIIStorage storage;


  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Starts the local Datastore emulator.
   *
   * @throws IOException if there are errors starting the local Datastore
   * @throws InterruptedException if there are errors starting the local Datastore
   */
  @BeforeClass
  public static void beforeClass() throws IOException, InterruptedException {
    HELPER.start();
  }

  /**
   * Initializes Datastore and cleans out any residual values.  Also initializes global variables
   * used for testing.
   */
  @Before
  public void setUp() {
    datastore = HELPER.getOptions().toBuilder().setNamespace("ghijklmnop").build().getService();
    this.storage = new PIIStorage(datastore);
  }

  /**
   * Stops the local Datastore emulator.
   *
   * @throws IOException if there are errors stopping the local Datastore
   * @throws InterruptedException if there are errors stopping the local Datastore
   */
  @AfterClass
  public static void afterClass() throws IOException, InterruptedException, TimeoutException {
    HELPER.stop(Duration.ofMinutes(1));
  }


  @Test
  public void testFullCycle() throws DataException {
    JsonObject pii = new JsonObject();
    pii.addProperty("name", "Test");
    pii.addProperty("phone", "555-555-5555");
    pii.addProperty("email", "test@test.com");

    long id = this.storage.createPII(pii);
    System.out.println("Created PII with id:"+id);

    JsonObject piiData = this.storage.getPII(id);
    assertNotNull(piiData);
    assertEquals("Test", piiData.get("name").getAsString());
    assertEquals("555-555-5555", piiData.get("phone").getAsString());
    assertEquals("test@test.com",piiData.get("email").getAsString());

    JsonObject piiNew = new JsonObject();
    piiNew.addProperty("phone", "666-666-6666");
    piiNew.addProperty("email", "test@test.org");

    this.storage.updatePII(id, piiNew);

    piiData = this.storage.getPII(id);
    assertNotNull(piiData);
    assertEquals("Test", piiData.get("name").getAsString());
    assertEquals("666-666-6666", piiData.get("phone").getAsString());
    assertEquals("test@test.org", piiData.get("email").getAsString());

    this.storage.deletePII(id);
    piiData = this.storage.getPII(id);
    assertNull(piiData);
  }
}
