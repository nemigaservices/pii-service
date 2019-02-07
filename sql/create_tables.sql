use pii;

drop table if exists Audit_Log;

create table Audit_Log (
	id integer NOT NULL auto_increment,
    api_key VARCHAR(255) NOT NULL,
    user_id integer not null,
    field_name varchar(255) NOT NULL,
    field_value_as_string varchar(255) NOT NULL,
    change_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    primary key(id)
);


# Use this statement to add the values
# insert into Audit_Log (api_key, user_id, field_name, field_value_as_string) values ("test", -1, "field1", "value1");
# Use this statement to check what was added for testing
# select api_key, user_id, field_name, field_value_as_stringAudit_Log from Audit_Log where api_key="test" and user_id=-1
# select api_key, user_id, field_name, field_value_as_stringAudit_Log from Audit_Log where api_key="test" and user_id=-1
# Use this statement to delete the test data
# delete from Audit_Log where api_key="test" and user_id=-1

drop table if exists Api_Access;
create table Api_Access (
	api_key VARCHAR(255) NOT NULL,
    method ENUM('GET', 'PUT', 'POST', 'DELETE'),
    unique index (api_key, method)
)


