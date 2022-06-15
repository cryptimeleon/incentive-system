# Cryptimeleon Incentive System

![Develop](https://github.com/cryptimeleon/incentive-system/workflows/Default%20workflow/badge.svg?branch=develop) [![Docker](https://img.shields.io/docker/cloud/build/eaudeweb/scratch?label=dockerhub&style=flat)](https://hub.docker.com/repository/docker/cryptimeleon/incentive-service-issue/tags)

## About

This project is an implementation of
the [Privacy-Preserving Incentive Systems with Highly Efficient Point-Collection](https://eprint.iacr.org/2020/382)
paper published in 2020. You can watch this [presentation on youtube](https://www.youtube.com/watch?v=Up-ECbJ4w5U&t=1s)
to learn more about the basic ideas of this project.

## Getting Started

The project consists of three main components: The cryptographic
protocols in the _crypto_ package, an android application in the _app_ package, and several spring boot webservices in the _services_ package.

To build the project, you need Java 11 and Android SDK 30 (can be installed with 
[sdkmanager](https://developer.android.com/studio/command-line/sdkmanager) or via Android Studio).
To deploy the services, we use docker and docker-compose.


## Benchmark of the Incentive System

To run a benchmark, install mcl following these [instructions](https://github.com/cryptimeleon/mclwrap) and
run `./gradlew :crypto:benchmark`.

## Developing

If you check out the math and craco repositories to the parent directory, the composite build will automatically use
these local versions instead of the maven releases. This comes in handy for development and avoids using snapshots.
Furthermore, we recommend enabling annotation processing in your IDE for better lombok support.

### Swagger API

When you start the services (either locally or via docker), you can access the swagger api page
at `basepath/swagger-ui/index.html`.

### Running the Services using Docker Compose

You can then use the `docker-compose.yaml` with the command `docker-compose -f deployment/docker-compose.yaml up`. You
can then reach the services at the localhost ports described above. If you want to build docker images locally from your
local code, you can use the command `./deployment/build-docker-images.sh`.

## License

Apache License 2.0, see [LICENSE](LICENSE) file.
