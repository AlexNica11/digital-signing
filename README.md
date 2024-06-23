# digital-signing

#Welcome to the the digital-signing app!

The build and steps will be split into 3 parts: client, server, database.


#Client:

Please check beforehand that you have npm installed.
The client is a Vite (version 4.2.1) React (version: 18.2.66) application.
Inside the folder "\digital-signing\dsclient", run the following commands:

##Build

npm run build

##Run Locally

npm run preview


#Server:

The server is a Spring Boot (version: 3.2.5) application, using JAVA 21.
Inside the folder "\digital-signing\dsms",
Set the JAVA_HOME env variable to the JAVA 21 location.
Please run the following commands.

##Build

mvn clean install

##Run Locally

java -jar dsms-0.0.1-SNAPSHOT.jar


#Database:

The database is a PostgreSQL server, deloyed inside Docker.

The database requires an installed Docker environment.
To deploy the database locally please run inside the "\digital-signing" folder the following command:

docker compose up
