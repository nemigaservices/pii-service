package net.nemiga.samples.piiservice.data.audit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.nemiga.samples.piiservice.data.DataException;

import java.sql.*;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class PIIDataChangeAudit {
    private Connection conn;

    private static final String INSERT_AUDIT_RECORD_SQL = "insert into Audit_Log (api_key, user_id, field_name, field_value_as_string) values (?, ?, ?, ?);";
    private static final String OBTAIN_AUDIT_RECORD_SQL = "select api_key, user_id, field_name, field_value_as_string from Audit_Log where api_key=? and user_id=?";
    private static final String DELETE_AUDIT_RECORD_SQL = "delete from Audit_Log where api_key=? and user_id=?";

    public PIIDataChangeAudit(String url) throws DataException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url);
        } catch (ClassNotFoundException e) {
            throw new DataException("Error loading JDBC Driver: "+ e.getMessage());
        } catch (SQLException e) {
            throw new DataException("Unable to connect to the database" + e.getMessage());
        }
    }

    public void reacordChange(String apiKey, int id, JsonObject piiData) throws DataException{
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
                statementInsertAuditRecord.setInt(2, id);
                statementInsertAuditRecord.setString(3, dataKey);
                statementInsertAuditRecord.setString(4, dataValue);

                statementInsertAuditRecord.executeUpdate();
            } catch (SQLException e) {
                throw new DataException("SQL error while inserting the audit log record: "+e.getMessage());
            }
        }
    }

    String getAuditDataAsStringForTesting(String apiKey, int id) throws SQLException{
        try (PreparedStatement obtainRecordsStatement = conn.prepareStatement(OBTAIN_AUDIT_RECORD_SQL)){
            obtainRecordsStatement.setString(1, apiKey);
            obtainRecordsStatement.setInt(2, id);
            try (ResultSet rs = obtainRecordsStatement.executeQuery()) {
                String result="";
                while (rs.next()) {
                    result+=rs.getString("api_key")+", ";
                    result+=rs.getInt("user_id")+", ";
                    result+=rs.getString("field_name")+", ";
                    result+=rs.getString("field_value_as_string")+"\n";
                }
                return result;
            }
        }
    }

    void deleteTestData(String apiKey, int id) throws SQLException{
        try (PreparedStatement statementInsertAuditRecord = conn.prepareStatement(DELETE_AUDIT_RECORD_SQL)) {
            statementInsertAuditRecord.setString(1, apiKey);
            statementInsertAuditRecord.setInt(2, id);

            statementInsertAuditRecord.executeUpdate();
        }
    }
}
