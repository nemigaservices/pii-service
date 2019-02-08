package net.nemiga.samples.piiservice.data.sql;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.nemiga.samples.piiservice.data.DataException;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.Map;
import java.util.Set;

/**
 * Contains methods to access SQL database for audit and access control
 */
public class PIISqlDataAccess {

    private Connection conn;

    private static final String INSERT_AUDIT_RECORD_SQL = "insert into Audit_Log (api_key, user_id, field_name, field_value_as_string) values (?, ?, ?, ?);";
    private static final String OBTAIN_AUDIT_RECORD_SQL = "select api_key, user_id, field_name, field_value_as_string from Audit_Log where api_key=? and user_id=?";
    private static final String DELETE_AUDIT_RECORD_SQL = "delete from Audit_Log where api_key=? and user_id=?";

    private static final String GET_METHOD_ACCESS_SQL = "select count(*) from Api_Access where api_key=? and method =?";

    /**
     * Constructs the object
     * @param url JDBC url to be used for connection
     * @throws DataException
     */
    public PIISqlDataAccess(String url) throws DataException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url);
        } catch (ClassNotFoundException e) {
            throw new DataException("Error loading JDBC Driver: "+ e.getMessage());
        } catch (SQLException e) {
            throw new DataException("Unable to connect to the database" + e.getMessage());
        }
    }

    /**
     * Records the changes in the PII data
     * @param apiKey API key used for access that made the change
     * @param id User id the data was changed for
     * @param piiData JSON object containing the fields and their values for the change
     * @throws DataException indicates an issue with accessing the database
     */
    public void recordChange(String apiKey, long id, JsonObject piiData) throws DataException{
        Set<Map.Entry<String, JsonElement>> entrySet = piiData.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String dataKey = entry.getKey();
            JsonElement data = piiData.get(entry.getKey());

            if (data.isJsonNull())
                throw new DataException("Element " + dataKey + " has null value!");
            String dataValue;
            if (data.isJsonPrimitive()) {
                if (data.getAsJsonPrimitive().isBoolean())
                    dataValue = Boolean.toString(data.getAsBoolean());
                else if (data.getAsJsonPrimitive().isString())
                    dataValue = data.getAsString();
                else if (data.getAsJsonPrimitive().isNumber()) {
                    dataValue = Integer.toString(data.getAsInt());
                }
                else{
                    throw new DataException("Element " + dataKey + " primitive type is not supported!");
                }
            } else {
                throw new DataException("Element " + dataKey + " type is not supported!");
            }

            try (PreparedStatement statementInsertAuditRecord = conn.prepareStatement(INSERT_AUDIT_RECORD_SQL)) {
                statementInsertAuditRecord.setString(1, apiKey);
                statementInsertAuditRecord.setLong(2, id);
                statementInsertAuditRecord.setString(3, dataKey);
                statementInsertAuditRecord.setString(4, dataValue);

                statementInsertAuditRecord.executeUpdate();
            } catch (SQLException e) {
                throw new DataException("SQL error while inserting the sql log record: "+e.getMessage());
            }
        }
    }

    public enum METHOD{GET, PUT, DELETE, POST};

    /**
     * Checks whether the given API key is allowed to execute a particular method
     * @param apiKey API key used for access
     * @param method method the access is checked for
     * @return true if the key is allowed for an access
     * @throws DataException indicates an issue with accessing the database
     */
    public boolean isMethodAllowed(String apiKey, METHOD method) throws DataException{
        try (PreparedStatement obtainRecordsStatement = conn.prepareStatement(GET_METHOD_ACCESS_SQL)){
            obtainRecordsStatement.setString(1, apiKey);
            obtainRecordsStatement.setString(2, method.toString());
            try (ResultSet rs = obtainRecordsStatement.executeQuery()) {
                int count=0;
                while (rs.next()) {
                    count = rs.getInt(1);
                }
                return count==1;
            }
        }
        catch (SQLException e) {
            throw new DataException("SQL error while inserting the sql log record: "+e.getMessage());
        }
    }

    /*
        Method is used for testing
     */
    public String getAuditDataAsStringForTesting(String apiKey, long id) throws SQLException{
        try (PreparedStatement obtainRecordsStatement = conn.prepareStatement(OBTAIN_AUDIT_RECORD_SQL)){
            obtainRecordsStatement.setString(1, apiKey);
            obtainRecordsStatement.setLong(2, id);
            try (ResultSet rs = obtainRecordsStatement.executeQuery()) {
                String result="";
                while (rs.next()) {
                    result+=rs.getString("api_key")+", ";
                    result+=rs.getLong("user_id")+", ";
                    result+=rs.getString("field_name")+", ";
                    result+=rs.getString("field_value_as_string")+"\n";
                }
                return result;
            }
        }
    }

    /*
        Method is used for testing
     */
    public void deleteTestData(String apiKey, long id) throws SQLException{
        try (PreparedStatement statementInsertAuditRecord = conn.prepareStatement(DELETE_AUDIT_RECORD_SQL)) {
            statementInsertAuditRecord.setString(1, apiKey);
            statementInsertAuditRecord.setLong(2, id);

            statementInsertAuditRecord.executeUpdate();
        }
    }
}
