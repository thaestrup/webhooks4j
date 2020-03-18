# Webhooks4j

Small, simple and extendable Java library for messaging using webhooks.

## Status

[![Build Status](https://travis-ci.org/jensborch/webhooks4j.svg?branch=master)](https://travis-ci.org/jensborch/webhooks4j) [![codecov](https://codecov.io/gh/jensborch/webhooks4j/branch/master/graph/badge.svg)](https://codecov.io/gh/jensborch/webhooks4j)

[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=dk.jensborch.webhooks4j%3Awebhooks4j&metric=alert_status)](https://sonarcloud.io/dashboard?id=dk.jensborch.webhooks4j%3Awebhooks4j)

Webhooks4j is currently under development.

## Building

The Webhooks4j is build using Maven.

To build the application run the following command:

```
./mvnw package
```

Start the test application using:

```
./mvnw compile -pl test quarkus:dev
```

Release to Maven central:
```
./mvnw release:clean release:prepare -Prelease
./mvnw -pl core release:perform -Prelease
````