# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Next Release

- Add spend-deduct protocol and include PRF to credit-earn [#28](https://github.com/cryptimeleon/incentive-system/pull/28)
- Upgrade to Java 16 and gradle 7 for compatibility with Spring Boot [#31](https://github.com/cryptimeleon/incentive-system/pull/31)
- Add credit-earn protocol [#27](https://github.com/upbcuk/incentive-services/pull/27)
- Rename organization to cryptimeleon  [#18](https://github.com/upbcuk/incentive-services/pull/18)
- Update gradle setup, basketserver authentication (shared secrets) and test structure, automatically generated swagger
  files, client project with integration tests, move protocoldefinition to services, replace kubernetes with
  docker-compose, add alive endpoints to all services, move basketserver client to client
  project [#14](https://github.com/upbcuk/incentive-services/pull/14)
- Add basic crypto-entities (public parameters, user/provider keys, token,
  ...) [#11](https://github.com/upbcuk/incentive-services/pull/11)
- Add data classes for token, keys and public parameters and change structure of crypto package [#16](https://github.com/upbcuk/incentive-services/pull/18)

## [0.4.0] - 2021-02-03

- Add options for local deployment and deployment using the container images from dockerhub and fix ingress
  configuration in `deploy.sh` [#8](https://github.com/upbcuk/incentive-services/pull/8)
- Fix wrong issue service configuration bug [#9](https://github.com/upbcuk/incentive-services/pull/9)
- Add basket server [#10](https://github.com/upbcuk/incentive-services/pull/10)
- Migrate from travis to github workflows and replace latest version by version
  file [#12](https://github.com/upbcuk/incentive-services/pull/12)

## [0.3.0] - 2021-01-12

- Rename protocols to protocoldefinition and add cryptoprotocols module, include math and craco, add interfaces for
  cryptoprocools use [#6](https://github.com/upbcuk/incentive-services/pull/6)
- Add local kubernetes support using KinD [#5](https://github.com/upbcuk/incentive-services/pull/5)

## [0.2.0] - 2020-12-24

- Add credit service [#4](https://github.com/upbcuk/incentive-services/pull/4)
- Improve mocks and add credit and deduct; add lombok [#3](https://github.com/upbcuk/incentive-services/pull/3)
- Add protocol mock and REST api for issueing service (including swagger api and
  tests) [#2](https://github.com/upbcuk/incentive-services/pull/2)

## [0.1.0] - 2020-12-11

- Bootstrap issue service and configure travis and gradle [#1](https://github.com/upbcuk/incentive-services/pull/1)
