# T2 Incentive Services

![Develop](https://github.com/upbcuk/incentive-services/workflows/Default%20workflow/badge.svg?branch=develop) [![Docker](https://img.shields.io/docker/cloud/build/eaudeweb/scratch?label=docker&style=flat)](https://hub.docker.com/repository/docker/upbcuk/incentive-service-issue/tags)

----

## Getting Started

1. Make sure you are using Java 15
2. To build all modules, use `./gradlew build`.
3. To run the boot services, use `./gradlew bootRun`.
4. To create a docker image for the issue service, use `./gradlew :issue:bootBuildImage --imageName=upbcuk/incentive-service-issue`

---

## Swagger API

We use [springfox](http://springfox.github.io/springfox/docs/current/) to automatically generate a swagger api. After you started the application using`./gradlew bootRun`, you can access the swagger ui at [http://localhost:8001/swagger-ui/index.html](http://localhost:8001/swagger-ui/index.html) [http://localhost:8002/swagger-ui/index.html](http://localhost:8002/swagger-ui/index.html) when executed locally.

---

## Deploying to Kubernetes

We use KinD (Kubernetes in Docker) for the deployment:
 1. Install (KinD)[https://kind.sigs.k8s.io/docs/user/quick-start/#installation] and (kubectl)[https://kubernetes.io/docs/tasks/tools/install-kubectl/].
 2. Run `./deployment/deploy.sh` from the projects

You can now access the api at [http://$NODE-IP/crediting/swagger-ui/index.html]() [http://$NODE_IP/issuing/swagger-ui/index.html](). The node ip can be queried with the command `kubectl get nodes -o wide`.

If you want to deploy your local changes to a cluster, use `./deployment/deploy.sh -l`. This builds the container images from your local code instead of pulling the last release from dockerhub. 

---

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

---

## License

Apache License 2.0, see [LICENSE](LICENSE) file.
