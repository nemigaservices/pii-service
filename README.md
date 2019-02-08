# PII Service

This repository contains code and supporting files for PII service.

## Purpose of the service
Service allows storing, managing and retrieveing PII data via REST

## Architecture and Implementaion of the service
The following diagram illustrates the organization of the service.
![image](https://user-images.githubusercontent.com/7335416/52500553-21df9b00-2b93-11e9-88fd-63b1bc8b1655.png)

Service is running in a Google Cloud and uses Google services for the management and scaling. 
Service is implemented as a Java servlet running on a Jetty in a docker container that is managed by App Engine Flex. Service by itself is not accessible from the outside; all communication with the service from the external parties are managed via  Cloud Endpoints that provide authentication and protection services (e.g. protection from DDoS).
Authentication to the service is accomplished via the API Keys that are managed by the Cloud Endpoints. Authorization is managed via the table in CloudSQL MySQL database that contains the entries that define which operations are allowed for each API key.

![image](https://user-images.githubusercontent.com/7335416/52501443-3886f180-2b95-11e9-88e2-c27be24e310b.png)

PII Data is store in a Firestore NoSQL database that allows storing easy expansion of the data fieds without any modificaitons of changes to the service. For each entry Cloud Store automatically assigns unique ID for the user that is returned to the caller upon the record creation.

Service also maintains an audit log that is stored in a table in CloudSQL MySQL database; each log entry contains the API key that was used for the operation, unique ID of the user, name of the field that was change and the new values. Delete operation is indicated as name of the field "all" and value set to "delete".


