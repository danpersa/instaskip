# Instaskip


[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.danpersa/instaskip.svg)](https://clojars.org/org.clojars.danpersa/instaskip)

Transforms from eskip to json to eskip.

## Installation

Download from https://clojars.org/org.clojars.danpersa/instaskip.

For lein users

    [org.clojars.danpersa/instaskip 0.3.1]

For maven users

    <dependency>
      <groupId>org.clojars.danpersa</groupId>
      <artifactId>instaskip</artifactId>
      <version>0.3.1</version>
    </dependency>

## Publishing

     lein deploy clojars

## Usage

Example:

    $ ./instaskip.sh \
        --token="token-user~1-employees-route.admin" \
        --url="https://innkeeper.pathfinder-staging.zalan.do" \
        migrate-routes --dir="~/mosaic-staging/routes/"

Running the tests: `lein midje`

Running it from lein:

    $ lein run -- --token="token-user~1-employees-route.admin" \
        --url="https://innkeeper.pathfinder-staging.zalan.do" \
        migrate-routes \
        --dir="~/mosaic-staging/routes/"

## Options

    $ ./instaskip.sh -h

## Examples

    # Migrate the routes for a specific team
    $ ./instaskip.sh \
        --token="token-user~1-employees-route.admin" \
        --url="https://innkeeper.pathfinder-staging.zalan.do" \
        migrate-routes --dir="~/mosaic-staging/routes/" \
        --team="graviton"

    # List hosts
    $ ./instaskip.sh -t token-user~1-employees-route.admin list-hosts

    # List paths
    $ ./instaskip.sh -t token-user~1-employees-route.admin list-paths -T graviton

    # List path details
    $ ./instaskip.sh -t token-user~1-employees-route.admin list-path -i 20

    # List routes
    $ ./instaskip.sh -t token-user~1-employees-route.admin list-routes -T graviton

    # List route details
    $ ./instaskip.sh -t token-user~1-employees-route.admin list-route -i 28

    # Delete route
    $ ./instaskip.sh -t token-user~1-employees-route.admin delete-route -i 28

## License

Copyright © 2016 Dan Persa

Distributed under the MIT License.
