# Clojure MAuth Client

[![Build Status](https://travis-ci.com/mdsol/clojure-mauth-client.svg?token=16x4FpKmXowpz3fnJFpj&branch=master)](https://travis-ci.com/mdsol/clojure-mauth-client)

Leiningen/Boot Coords:

```[clojure-mauth-client "2.0.1"]```

Here it is folks, a nice, clean MAuth client done in the simplest and most minimal way possible, for your Clojure application.

There are a minimal set of dependencies (Bouncy Castle isn't one of them!), it is fast, and can even be used as middleware for securing your applications.

You do **NOT** need to jump through hoops to use this library. Simple is as simple does.

## Building

Either use the built jar, or `lein jar` to build one based on source.

## Usage

First, you need to give the library your `App UUID`, `MAuth Private Key` and `MAuth Service URL`.

You can do this by defining the environment variables `APP_UUID`, `MAUTH_KEY` and `MAUTH_SERVICE_URL` respectively.

Or, you may do this programmatically by calling `credentials/define-credentials` with the respective parameters.

You can make an MAuth call by requiring the `request` namespace and either doing `get!`, `post!`, `delete!` or `put!` with the necessary parameters. 
These are the most common verbs in use out there, but its easy to extend support to other verbs as needed (PRs welcome!).
If the verb you want isn't in the namespace, you can also make a call to `request/make-request` and specify your action directly.
It is possible to do a macro for this stuff, but I am not a big fan of that approach for this case, because you lose IDE support, and it will appear as undefined there, which is annoying.


Full example (standard usage):

```
(ns your-app.core
  (:require [clojure-mauth-client.request :as mauth]
            [clojure.data.json :as json]))

;either your app has to have the APP_UUID, MAUTH_KEY, and MAUTH_SERVICE_URL predefined in env...
;or... you can call define-credentials...

;do the call!
(-> (mauth/get! "https://www.mdsol.com" "/api/v2/testing.json")
    :body
    json/read-str)
```

Middleware Usage:

```
(ns your-app.handler
  (:require [clojure-mauth-client.middleware :refer [wrap-mauth-verification]]))
  ...
  ...
  (-> #'app-routes
            (wrap-routes middleware/wrap-formats)
            (wrap-mauth-verification))
  ...
  ...
```

Yep, it really is that simple!

Recently, it has come to my attention that cloudflare or cloudfront URLs (ie. api-gateway) were not supported due to SNI configuration in the load balancer. Welp, not anymore!
To do your calls with SNI, pass `:with-sni? true` to your calls.

For example,
`(mauth/get! "https://www.mdsol.com" "/api/v2/testing.json" :with-sni? true)`

Version 2.0.1 update -
We have added support for mauth version 2 headers as well now. To get the v2 headers you need to add additional header in your request as
"mauth-version" with value as "v2" and algorithm also uses query param if any to sign  or to generate these headers, so you will need to add additional header in your request as
"query-param-string"  and value should be string of sorted and encoded query params e.g abc=abctest&test=testing%20value
you will get the response version 2 headers as below-
"mcc-authentication" authentication value
"mcc-time"           mcc-time

## Contributing/ Tests
Tests can be run using `lein test`.

Contributions are welcome by Pull Request, and we welcome and encourage that!

## License

Copyright © 2018 Medidata Solutions

This Library is licensed under the MIT licensing terms. See [LICENSE.md](https://github.com/mdsol/clojure-mauth-client/blob/master/LICENSE.md) for details
