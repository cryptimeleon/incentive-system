# Incentimeleon

![Develop](https://github.com/cryptimeleon/incentive-system/workflows/Default%20workflow/badge.svg?branch=develop)

## About

This project is an implementation of
the [Privacy-Preserving Incentive Systems with Highly Efficient Point-Collection](https://eprint.iacr.org/2020/382)
paper published in 2020. You can watch this [presentation on youtube](https://www.youtube.com/watch?v=Up-ECbJ4w5U&t=1s)
to learn more about the basic ideas of this project.

## Deployment

### Services

We provide docker images of the services for custom deployments.
To deploy the incentive-system follow these steps (tested on linux and macOS):
 0. Install docker (tested on version 20.10.12) and docker-compose (tested on version 1.29.2)
 1. Checkout this repository (you only need the contents of the deployment folder)
 2. Change the HOST variable in `deployment/deploy.sh` to your server's url or `localhost:8009` for a local installation
 3. Run `./deployment/deploy.sh`
 4. The deployment runs at port `8009`

If you want to build docker images locally, you can use the command `./deployment/build-docker-images.sh`.

### App

You can change the deployment's url in `app/build.gradle` by setting the `deploymentBaseUrl` variable before building.
For local deployments, use the naming scheme `http://xxx.xxx.xxx.xxx:8009` and add the line `android:usesCleartextTraffic="true"` to the `AndroidManifest.xml` to enable http.

## Building

To build the project, you need Java 11 and Android SDK 33 (can be installed with
[sdkmanager](https://developer.android.com/studio/command-line/sdkmanager) or via Android Studio).

## Benchmark of the Incentive System

To run a benchmark, install mcl following these [instructions](https://github.com/cryptimeleon/mclwrap) and
run `./gradlew :crypto:benchmark`.

## Swagger API

When you start the services (either locally or via docker), you can access the swagger api page
at `basepath/swagger-ui/index.html`.

## Creating promotions and choosing images

You can create configure own promotion-sets in the bootstrap service.
To add images, put the image in the `web/public/assets/` folder.
Use the promotion_name in lower case with spaces replaced by underscores and choose the jpg format.
For a promotion named `Christmas Promotion`, name the image `christmas_promotion.jpg`.

## License

Apache License 2.0, see [LICENSE](LICENSE) file.
