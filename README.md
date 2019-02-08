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

![image](https://user-images.githubusercontent.com/7335416/52502709-4ab65f00-2b98-11e9-8bcf-2dcc6f9a5e01.png)

### Data Encryption
In-transit data encrypotion is assured by SSL enforced by the Cloud Endpoints.

Data storage encryption is assured by Google - all data stored in the Firestore and Cloud SQL is always encrypted. 

Presently default encryption options are chosen for Firestore https://cloud.google.com/firestore/docs/server-side-encryption, however, advanced encryption options are available by Google should it become necessary https://cloud.google.com/security/encryption-at-rest/.

Information about Cloud SQL data encryption can be found here: https://cloud.google.com/sql/faq#encryption

### Vendor dependency
Current implementaion has the following vendor dependency.
- Firestore - high. All the code for interacting with the firestore is contained in https://github.com/nemigaservices/pii-service/blob/master/src/test/java/net/nemiga/samples/piiservice/data/piistorage/PIIStorageTest.java. The effort to migrate to a different NoSQL should not exceed 2-4 hours.
- App Engine Flex - medium to low. App Engine Flex just runs a docker container with Jetty and HttpServlet. The effort to migrate should be minimum dependently on the chosen new solution.
- Cloud Endpoints - low. Cloud Endpoints are ony acting as a proxy - code does not contain anything specific for the endpoints. As the result, any other proxy that uses OpenAPI specification can be easily introduced.
- Cloud SQL - low. The system uses MySQL; only the URL change will be required for the migration. 

## Building and testing of the service
To build and deploy a solution run the following command:
```
./buildAndDeploy.sh
```

To perform full end-to end integration test, run the following command:
```
mvn exec:java -Dexec.mainClass="net.nemiga.samples.piiservice.IntegrationTest" -Dexec.classpathScope="test" 
```

_Note_ Unit-tests require access to a database - Cloud SQL Proxy should be running. To start the proxy, use the following command:
```
./runCloudSQLProxy.sh 
```

Script _runPiiTest.sh_ contains sample commands to test the service with Curl.

## Using the PII Service API
OpenAPI specification stored in _openapi.yml_ contains full documentation for the service.
The following screenshots from Cloud Endpoints Development Portal illustrate the use of each API.
### Create PII for the user (POST)
![image](https://user-images.githubusercontent.com/7335416/52505034-185c3000-2b9f-11e9-8d1a-fc567cc98a50.png)

