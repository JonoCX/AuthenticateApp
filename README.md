# Authentication Android Application: AuthenticateMe

An application that performs topic detection and relationship analysis (from a users Twitter profile), and
locational analysis to see if they are able to authenticate to perform a particular action.

The requesting user has their Twitter profile checked against the topics that they have been talking about 
(with more time, the topics that you expect someone to be talking about can be added in) and the relationships,
Twitter specific relationships, they have with a set of static users.

The location information is inferred to see if they are situated within the Claremont Tower building on the
Newcastle University campus.

The JavaDocs are available to read here: [API Docs](https://jonocx.github.io/AuthenticateApp/)

## Future Work
* Make the application more dynamic, so the network/relationship analysis is statically checked against pre-defined users.
* Allow key users within the network to authorise a requesting user.

## Built With
* Java
* Android Studio
* [Twitter4J](http://twitter4j.org/en)
* [Monkey Learn](http://www.monkeylearn.com/)
* [SimpleJSON](https://code.google.com/archive/p/simple-json/)
* [Fabric: Twitter](https://get.fabric.io/)
* [Network Analysis](https://github.com/JonoCX/NetworkAnalysis) (Structure, design and code brought inline from this project)
* [Android Location](https://developer.android.com/reference/android/location/package-summary.html)
