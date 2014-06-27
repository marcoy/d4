# d4

An experiment of using d3 with clojure/clojurescript.

## Usage

Running the browser REPL

Build the cljurescript file and then run a REPL.
```
?> lein do cljsbuild once, repl
```

Inside the REPL, you should be dropped into the `d4.repl` namespace. Within that
namespace are some helper functions. First, there is the `(run)` function. It
creates a jetty webserver running at port 3000. Then, the `(brepl)` will start a
browser REPL server and drop you into the browser REPL.
```
d4.repl=> (run)
d4.repl=> (brepl)
cljs.user=> ; Inside the browser REPL
```

Finally, navigate to http://localhost:3000/repl using your browser. That's it.

## License

Copyright © 2014 Marco Yuen <marcoy@gmail.com>

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
