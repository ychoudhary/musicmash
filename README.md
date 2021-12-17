# General

The project is about solving a problem to get details for a given mbID<Music Brainz Id>, with
respect to an artist. The information is scattered on different websites, and we need to gather and
combine that.

There are lot of other ways, which can be used to solve this problem, but I used SprintBoot for
hosting the app and HttpClient and Rest template in making the connection to various backends.

Other solution might be using Apache Camel Or MuleSoft ESB, which both provides routing
functionality out of the box which can connect to most of the backend types.

This is an example project demonstrating Spring Boot connecting with various backed services and
also using RateLimit for some backend.

## Running locally

```bash
./gradlew bootRun
```

Right now only one profile support is added to using Live Endpoints, but we can easily add PRD/DEV
kind of profile, and then we will use the following command to run locally. We will also need to
have local application properties file to support that.

```bash
./gradlew -Dspring.profiles.active=local bootRun
```

in Eclipse and IDEA Run the Application.java as Run as Java Application

```bash
http localhost:8080/musicmash/1234
```

```bash
localhost:8080/musicmaash/{mbId}
```

## Documentation

At the moment, live documentation is not available, but can be easily done using adding swagger or
openAPI build dependency.

# Sample Requests

URLS.txt file is added which has few examples of mbIds