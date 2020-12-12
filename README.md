# T2 Incentive Services

[![Build Status](https://travis-ci.com/upbcuk/incentive-services.svg?branch=develop)](https://travis-ci.com/upbcuk/incentive-services) [![Docker](https://img.shields.io/docker/cloud/build/eaudeweb/scratch?label=docker&style=flat)](https://hub.docker.com/repository/docker/upbcuk/incentive-service-issue/tags)

----

## Getting Started

1. Make sure you are using Java 15
2. To build all modules, use `./gradlew build`.
3. To run the boot services, use `./gradlew bootRun`.
4. To create a docker image for the issue service, use `./gradlew :issue:bootBuildImage --imageName=upbcuk/incentive-service-issue`

---

## Swagger API

We use [springfox](http://springfox.github.io/springfox/docs/current/) to automatically generate a swagger api. After you started the application using`./gradlew bootRun`, you can access the swagger ui at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html). 

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

---

## License

Apache License 2.0, see [LICENSE](LICENSE) file.