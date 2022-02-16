# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Next Release

- Extract client-side pseudorandomness and include promotionId to PRF input [#76](https://github.com/cryptimeleon/incentive-system/pull/76)
- Integrate promotions to app, ui updates, and project refactoring [#70](https://github.com/cryptimeleon/incentive-system/pull/70)
- Add v1 of service that integrates promotion and the crypto protocols [#65](https://github.com/cryptimeleon/incentive-system/pull/65)
- Adapt SpendDeductZkp to a PartialProofOfKnowledge [#62](https://github.com/cryptimeleon/incentive-system/pull/62)
- Add promotion id to tokens and change token's single value to a vector. [#63](https://github.com/cryptimeleon/incentive-system/pull/63)
- Migrate app to jetpack compose [#55](https://github.com/cryptimeleon/incentive-system/pull/55)
- Refactor app to use a repository pattern [#54](https://github.com/cryptimeleon/incentive-system/pull/54)
- Add basket implementation and issue-join + credit-earn to app, use Hilt for dependency injection [#53](https://github.com/cryptimeleon/incentive-system/pull/53)
- Add barcode scanner and room database to app and change item id in basket service from `UUID` to `String` to allow scanning barcodes [#50](https://github.com/cryptimeleon/incentive-system/pull/50)
- Add mcl support to docker images, separate the gradle builds and add first setup workflow to app [#47](https://github.com/upbcuk/incentive-services/pull/47)
- Add docker-compose files for server configuration and local deployment, add info service that provides pp and provider keys, and incorporate crypto into credit and issue service [#43](https://github.com/upbcuk/incentive-services/pull/43)
- Restructure project and build. Merge (private) app repository into this repo. Rename `cryptoprotocol` to `crypto`, `basketserver` to `basket`, and package `incentivesystem` to `incentive` [#41](https://github.com/cryptimeleon/incentive-system/pull/41)
- Add benchmark to cryptoprotocols and mcl support [#37](https://github.com/cryptimeleon/incentive-system/pull/37)
- Make cryptoprotocols compatible with android [#39](https://github.com/cryptimeleon/incentive-system/pull/39)
- Add issue-join protocol [#30](https://github.com/cryptimeleon/incentive-system/pull/30)
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
- Write integration test for all three cryptographic protocols (Issue-Join, Credit-Earn, Spend-Deduct) [#35](https://github.com/cryptimeleon/incentive-system/pull/35)

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
