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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.nemiga.samples.piiservice.data.DataException;

import java.util.Map;
import java.util.Set;

/**
 * A simple Task List application demonstrating how to connect to Cloud Datastore, create, modify,
 * delete, and query entities.
 */
public class PIIStorage {

  private final Datastore datastore;

  private final KeyFactory keyFactory;

  // [START datastore_build_service]
  public PIIStorage() {
    // Create an authorized Datastore service using Application Default Credentials.
    this.datastore = DatastoreOptions.getDefaultInstance().getService();
    // Create a Key factory to construct keys associated with this project.
    this.keyFactory = datastore.newKeyFactory().setKind("PII");
  }
  // [END datastore_build_service]

  PIIStorage(Datastore dataStore) {
    this.datastore = dataStore;
    // Create a Key factory to construct keys associated with this project.
    this.keyFactory = datastore.newKeyFactory().setKind("PII");
  }

  // [START datastore_add_entity]
  /**
   * Adds a PII entity to the Datastore.
   *
   * @param piiData PII information
   * @return The {@link Key} of the entity
   * @throws DatastoreException if the ID allocation or put fails
   * @throws DataException if there is an issue with the data
   */
  public long createPII(JsonObject piiData) throws DataException {
    Key key = datastore.allocateId(keyFactory.newKey());
    Entity.Builder piiBuilder = Entity.newBuilder(key);

    this.setData(piiData, piiBuilder);

    Entity pii = piiBuilder.build();
    datastore.put(pii);
    return key.getId();
  }
  // [END datastore_add_entity]

  // [START datastore_update_entity]
  /**
   * Updates the PII with new data
   *
   * @param id The ID of the PII
   * @param piiData Data to be updated
   * @return true if the task was found, false if not
   * @throws DatastoreException if the transaction fails
   * @throws DataException if there is an issue with the data
   */
  boolean updatePII(long id, JsonObject piiData) throws DataException {
    Transaction transaction = datastore.newTransaction();
    try {
      Entity pii = transaction.get(keyFactory.newKey(id));
      if (pii != null) {
        Entity.Builder piiBuilder = Entity.newBuilder(pii);
        this.setData(piiData, piiBuilder);
        Entity piiEntity = piiBuilder.build();
        transaction.put(piiEntity);
      }
      transaction.commit();
      return pii != null;
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }
  // [END datastore_update_entity]

  // [START datastore_delete_entity]
  /**
   * Deletes a task entity.
   *
   * @param id The ID of the user
   * @throws DatastoreException if the delete fails
   */
  void deletePII(long id) {
    datastore.delete(keyFactory.newKey(id));
  }
  // [END datastore_delete_entity]

  // [START datastore_get_entity]
  /**
   * Retrieves PII data
   *
   * @param id The ID of the user
   * @return PII data stored in the Json object
   * @throws DatastoreException if the retrieval fails
   * @throws DataException if there is an issue with the data
   */
  public JsonObject getPII(long id) throws DataException {
    Entity pii = datastore.get(keyFactory.newKey(id));
    if (pii == null) {
      return null;
    }

    JsonObject piiData = new JsonObject();

    for (String name : pii.getNames()) {
      ValueType valueType = pii.getValue(name).getType();
      switch (valueType) {
        case STRING:
          piiData.addProperty(name, pii.getString(name));
          break;
        case BOOLEAN:
          piiData.addProperty(name, pii.getBoolean(name));
          break;
        case LONG:
          piiData.addProperty(name, pii.getLong(name));
          break;
        default:
          throw new DataException(
              "Unexpected data type in the PII data in the storage for id: " + id);
      }
    }
    return piiData;
  }
  // [END datastore_get_entity]
  private void setData(JsonObject piiData, Entity.Builder piiBuilder) throws DataException {
    Set<Map.Entry<String, JsonElement>> entrySet = piiData.entrySet();
    for (Map.Entry<String, JsonElement> entry : entrySet) {
      String key = entry.getKey();
      JsonElement data = piiData.get(entry.getKey());

      if (data.isJsonNull()) throw new DataException("Element " + key + " has null value!");

      if (data.isJsonPrimitive()) {
        if (data.getAsJsonPrimitive().isBoolean()) piiBuilder.set(key, data.getAsBoolean());
        else if (data.getAsJsonPrimitive().isString())
          piiBuilder.set(
              key, StringValue.newBuilder(data.getAsString()).setExcludeFromIndexes(true).build());
        else if (data.getAsJsonPrimitive().isNumber()) {
          piiBuilder.set(key, data.getAsInt());
        }
      } else {
        throw new DataException("Element " + key + " type is not supported!");
      }
    }
  }
}
