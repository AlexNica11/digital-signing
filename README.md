# digital-signing

# Welcome to the the digital-signing app!

The app can sign PAdES, XAdES, JAdES, CAdES up to baseline levels B-B, B-T, B-LT and B-LTA.
The build and steps will be split into 3 parts: client, server, database.


# Client:

Please check beforehand that you have npm installed.
The client is a Vite (version 4.2.1) React (version: 18.2.66) application.
Inside the folder "\digital-signing\dsclient", run the following commands:

## Build

```bash
npm run build
```

## Run Locally

```bash
npm run preview
```
The server will be available at localhost:4173.

# Server:

The server is a Spring Boot (version: 3.2.5) application, using JAVA 21.
Inside the folder "\digital-signing\dsms",
Set the JAVA_HOME env variable to the JAVA 21 location.
Please run the following commands.

## Build

```bash
mvn clean install
```

## Run Locally

```bash
java -jar dsms-0.0.1-SNAPSHOT.jar
```
The server will be available at localhost:8080.


# Database:

The database is a PostgreSQL server, deloyed inside Docker.

The database requires an installed Docker environment.
To deploy the database locally please run inside the "\digital-signing" folder the following command:

```bash
docker compose up
```


# Resources for testing

Inside the "\digital-signing\dsms\src\test\resources" folder, there are multiple files that you can use to test the application, like pdf, xml, json and text files that can be used as documents for signature creation.

There are 3 key stores provided, each containing a self signed certificate.
Here are the parameters for those key stores:

Name: good-user-crl-ocsp.p12
Password: ks-password
Alias: good-user-crl-ocsp
No private key password for the alias. 
Certificate contains CRL and OCSP sources. Can be used for all the provided types and levels of signature creation.

Name: good-user.p12
Password: ks-password
Alias: good-user
No private key password for the alias.
Certificate does not contain CRL and OCSP sources. Can be used for all the provided types and only for levels B-B and B-T of signature creation.

Name: signer-key-store.p12
Password: password
Alias: signer-cert-alias
No private key password for the alias.
Certificate does not contain CRL and OCSP sources. Can be used for all the provided types and only for levels B-B and B-T of signature creation.