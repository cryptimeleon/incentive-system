# T2 Incentive Services

![Develop](https://github.com/cryptimeleon/incentive-system/workflows/Default%20workflow/badge.svg?branch=develop) [![Docker](https://img.shields.io/docker/cloud/build/eaudeweb/scratch?label=dockerhub&style=flat)](https://hub.docker.com/repository/docker/cryptimeleon/incentive-service-issue/tags)

----

## Getting Started

1. Make sure you are using Java 16.
2. To build all modules, use `./gradlew build`.
3. To run the boot services, use `./gradlew bootRun`.
4. To create a docker image for the issue service,
   use `./gradlew :issue:bootBuildImage --imageName=cryptimeleon/incentive-service-issue`
   
--- 

## Benchmark of the Incentive System

To run a benchmark, install mcl following these [instructions](https://github.com/cryptimeleon/mclwrap) and run `./gradlew cryptoprotocol:benchmark`.

---

## Swagger API

We use [springfox](http://springfox.github.io/springfox/docs/current/) to automatically generate a swagger api. After
you started the application (for example using the Spring Boot plugin or `./gradlew :[service-name]:bootRun`), you can
access the swagger ui at

- Issue service: [http://localhost:8001/swagger-ui/index.html](http://localhost:8001/swagger-ui/index.html)
- Credit service: [http://localhost:8002/swagger-ui/index.html](http://localhost:8002/swagger-ui/index.html)
- Basket server: [http://localhost:8003/swagger-ui/index.html](http://localhost:8003/swagger-ui/index.html)

when executed locally.


---

## Running the Services using Docker Compose

You can then use the `docker-compose.yaml` with the command `docker-compose -f deployment/docker-compose.yaml up`. You
can then reach the services at the localhost ports described above. If you want to build docker images locally from your
local code, you can use the command `./deployment/build-docker-images.sh`.


---

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

---

## License

Apache License 2.0, see [LICENSE](LICENSE) file.
