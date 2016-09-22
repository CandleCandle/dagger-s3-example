Example using Dagger (v2) to list S3 Buckets
===========================================

Building
--------

```shell
mvn clean install assembly:single
```

Running
-------

Have AWS credentials available as per the [DefaultAWSCredentialsProviderChain](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html)
```shell
java -jar target/*with-dep*.jar
```

Organisation
------------

`@Module` annotated classes provide collections of dependencies. They are for classes are not under your control and do not have an `@Inject`-annotated constructor [or @Inject annotated fiends, ewwww!]

There is a dependency tree between each `@Module` annotated class; using the `includes` parameter to the annotation.

A `@Component` annotated interface or abstract class takes a list of modules on which it depends.

A Library requires just the javax.inject dependency, as long as the injectable classes are annotated with `@Inject`. If they require classes from a 3rd party library that does not have `@Inject` annotations, then consider making a sibling module that contains the `@Module` and `@Provides` code:
```
pom.xml [ ${groupId}:${artifactId}-parent ]
  |- pom.xml [ ${groupId}:${artifactId} ] depends on javax.inject:javax.inject:1
  \- pom.xml [ ${groupId}:${artifactId}-modules ] depends on com.google.dagger:dagger:2.x and ${groupId}:${artifactId}
```

