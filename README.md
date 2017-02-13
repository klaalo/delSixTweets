[![build status](https://gitlab.com/kari.laalo/delSixTweets/badges/master/build.svg)](https://gitlab.com/kari.laalo/delSixTweets/commits/master)
[![coverage report](https://gitlab.com/kari.laalo/delSixTweets/badges/master/coverage.svg)](https://gitlab.com/kari.laalo/delSixTweets/commits/master)

# delSixTweets
This is a simple [Spring Boot](https://projects.spring.io/spring-boot/) project to remove six tweets at a time. Twitter's own user interface makes it quite difficult to remove tweets in bulk. DelSixTweets makes it easier to reduce your social footprint. This project was initiated as an example to utilise:

* [Twitter API](https://dev.twitter.com)
* Spring Boot

[Twitter4j](http://twitter4j.org) library is used to connect to Twitter API.

## Continuous Integration
Gitlab makes possible to use [Pipelines](https://docs.gitlab.com/ee/ci/pipelines.html) with a local [runner](https://docs.gitlab.com/runner/). The project has been set up so that each successful build job on the pipeline will be followed by another job to deploy a new revision onto the server.

## Live demo
You can reach a live demo at: [https://delsixtweets.kari.iki.fi/](https://delsixtweets.kari.iki.fi/). <span style='color: darkRed'>**Danger!**</span> Please, understand that tweets are actually removed from your timeline and there is no way of restoring them after removal. 