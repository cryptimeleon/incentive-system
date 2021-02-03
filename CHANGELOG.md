# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.4.0] - 2021-02-03
 - Add options for local deployment and deployment using the container images from dockerhub and fix ingress configuration in `deploy.sh` [#8](https://github.com/upbcuk/incentive-services/pull/8)
 - Fix wrong issue service configuration bug [#9](https://github.com/upbcuk/incentive-services/pull/9)
 - Add basket server [#10](https://github.com/upbcuk/incentive-services/pull/10)
 - Migrate from travis to github workflows and replace latest version by version file [#12](https://github.com/upbcuk/incentive-services/pull/12)

## [0.3.0] - 2021-01-12
 - Rename protocols to protocoldefinition and add cryptoprotocols module, include math and craco, add interfaces for cryptoprocools use [#6](https://github.com/upbcuk/incentive-services/pull/6)
 - Add local kubernetes support using KinD [#5](https://github.com/upbcuk/incentive-services/pull/5)

## [0.2.0] - 2020-12-24
 - Add credit service [#4](https://github.com/upbcuk/incentive-services/pull/4)
 - Improve mocks and add credit and deduct; add lombok [#3](https://github.com/upbcuk/incentive-services/pull/3)
 - Add protocol mock and REST api for issueing service (including swagger api and tests) [#2](https://github.com/upbcuk/incentive-services/pull/2)

## [0.1.0] - 2020-12-11
 - Bootstrap issue service and configure travis and gradle [#1](https://github.com/upbcuk/incentive-services/pull/1)
