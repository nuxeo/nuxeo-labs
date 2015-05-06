## Important Note

**These feature is not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


## Building
### Requirements
Building requires the following software:
- git
- maven

### How to build 
```
git clone https://github.com/nuxeo/nuxeo-labs
cd nuxeo-labs-more-like-ui
mvn clean install
```

## Deploying
- Copy the jar package from target to the nxserver/bundles folder of your server
- Build and deploy this [module] (https://github.com/nuxeo/nuxeo/tree/master/nuxeo-features/nuxeo-elasticsearch/nuxeo-elasticsearch-http-read-only)
- Uncomment elasticsearch.httpEnabled=true in nuxeo.conf if you use the embedded Elasticsearch server

## Use
In studio, add a Generic widget to your layout and set the type to more_like_this.
That's it !

##About Nuxeo
Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Netflix, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at [www.nuxeo.com](http://www.nuxeo.com).