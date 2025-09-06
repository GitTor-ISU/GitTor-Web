## Run API

```bash
run-api.sh
```
Compiles and starts the Spring Boot application for development.
This skips many stages, like checks and tests, which can be run with the maven targets `checkstyle:check` and `test`.

## Run UI

```bash
run-ui.sh
```
1. Starts a development API if one can not be found at `localhost:8080`
2. Autogenerates API connection code with Open API specification
3. Serves the Angular application with automatically recompilation and hot-reloading when you make code changes

## Run Docker Development

```bash
run-docker-dev.sh
```
1. Starts the API container with port 8080 exposed
2. Builds the UI container by getting the Open API specification from the API container
3. Starts all the containers with all ports exposed

## Run Docker Production

```bash
run-docker-prod.sh
```
1. Starts the API container with port 8080 exposed
2. Builds the UI container by getting the Open API specification from the API container
3. Starts all production containers with only ports 80 and 443 exposed

## Test End to End

```bash
test-e2e.sh
```
1. Starts a development API
2. Runs Cypress end to end tests
3. Stops the development API

## Update Open API

```bash
update-openapi.sh
```
1. Starts a development API
2. Autogenerates API connection code with Open API specification

## Update Pom

```bash
update-pom.sh
```
Updates all dependencies in [pom.xml](/api/pom.xml) to the most recent version.
