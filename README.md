# delSixTweets
This is a simple [Spring Boot](https://projects.spring.io/spring-boot/) project to remove six tweets at a time. Twitter's own user interface makes it quite difficult to remove tweets in bulk. DelSixTweets makes it easier to reduce your social footprint. This project was initiated as an example to utilise:

* [Twitter API](https://dev.twitter.com)
* Spring Boot

[Twitter4j](http://twitter4j.org) library is used to connect to Twitter API.


## App Engine
The project has been modified to run on [Google App Engine](https://cloud.google.com/appengine/). If you are looking for the plain Spring Boot version, please see branch plainSpring.

App Engine version is based on relevant [archetype](https://github.com/klaalo/spring-boot-gae-stub).

Unit tests and CI were removed in connection with migrating to app engine.

## Live demo
You can reach a live demo at: [https://g.karilaalo.fi/twitter/](https://g.karilaalo.fi/twitter/). <span style='color: darkRed'>**Danger!**</span> Please, understand that tweets are actually removed from your timeline and there is no way of restoring them after removal. 
