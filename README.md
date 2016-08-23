Example using Dagger (v2) to list S3 Buckets
===========================================

Building
--------

```shell
mvn clean install assembly:single
```

Running
-------

```shell
java -jar target/*with-dep*.jar
```

Organisation
------------

`@Module` annotated classes provide collections of dependencies. In this example, they each, currently, provide just one dependency each,

There is a dependency tree between each `@Module` annotated class; using the `includes` parameter to the annotation.

The annotations have a rentention policy of RUNTIME, thus, if I were doing a library that may supply dependencies it seems sensible to do a multi-module component with two modules: `library` and `library-module`. The main library would have the expected code and the library-module would provide the @Module annotated classes for the library. That way you don't have to pull in the dagger dependency if you are not using it.

I'm not (yet) comfortable in having the extra interface `MainComponent` as well as the `S3ListingModule`, it feels like there's two things when there could only be one.

