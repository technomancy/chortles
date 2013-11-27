# chortles

Calculate the magnitude of a given laugh over HTTP and JSON.

Really just used as a demonstration of an HTTP-based remote REPL.

See [Debugging Clojure apps with Drawbridge](https://devcenter.heroku.com/articles/debugging-clojure).

## Usage

This requires Leiningen 2 on the client side.

To set env vars (**pre-requisite**):

    EXPORT AUTH_USER=flynn
    EXPORT AUTH_PASS=reindeerflotilla
    EXPORT PORT=5000

To start a web server:

    $ lein run

To calculate a laugh:

    $ curl http://localhost:5000 -d hahahaha
    {"hahahaha": 4, "percentile": 100.0}

To connect a REPL to the running app:

    $ lein repl :connect http://flynn:reindeerflotilla@localhost:5000/repl

## License

Copyright © 2012 Phil Hagelberg

Distributed under the Eclipse Public License, the same as Clojure.
