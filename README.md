# Clojure MAuth Client

[![Build Status](https://travis-ci.com/mdsol/clojure-mauth-client.svg?token=16x4FpKmXowpz3fnJFpj&branch=master)](https://travis-ci.com/mdsol/clojure-mauth-client)

Leiningen/Boot Coords:

```[clojure-mauth-client "2.0.8"]```

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

Version 2.0.2 update -
We have added support for mauth version 2 headers as well now. To get the v2 headers you need to add additional header in your request as
"mauth-version" with value as "v2" and algorithm also uses query param if any to sign  or to generate these headers, so you will need to add additional header in your request as
"query-param-string"  and value should be string of sorted and encoded query params e.g abc=abctest&test=testing%20value
you will get the response version 2 headers as below-
"mcc-authentication" authentication value
"mcc-time"           mcc-time

Version 2.0.3 update -
Changed mauth unauthorized error status code from 403 to 401 code which corresponds to the right HTTP status code for Unauthorized.
And also changed signature algorithm to SHA512withRSA, previously it was SHA256withRSA.

Version 2.0.4 update -
With this version we have added support to validate mauth requests with v2 version and validating mcc-time and mcc-authentication headers.

Version 2.0.5 update -
With this version we have removed dependency of extra header "mauth-version" that we needed to validate mauth requests with v2 version. 
No need to send this extra header to validate v2 version requests.

Version 2.0.6 update -
With this version we have removed throwing exceptions when not valid mauth headers keys are added or no valid token passed in signature.

Version 2.0.7 update-
Updated few of the library versions to remove vulnerability

Version 2.0.8 upsdate -
For mAuth Protocol V2, includes the query-string (when present), in the authentication ticket to validate the requests.

## Contributing/ Tests
Tests can be run using `lein test`.

Contributions are welcome by Pull Request, and we welcome and encourage that!

## Release
Currently, our releases are done manually, but the deploy is done by Travis/CI when a tag is present.

### How to release
We use the built-in lein task `release`. This task will verify that there is nothing uncommitted, updates the release version, 
tags the release, deploys to clojars (Travis/CI) and then bumps the version to the next snapshot version (`lein bump!`). See `lein help release` for more details.

1. Switch to the `master` branch
2. Make sure you pull the latest code from Github
3. Create a new branch. This is because main is blocked from pushing commits directly
4. Set the upstream from this new branch `git push --set-upstream origin <new-branch>`
5. run `lein release`
6. PR the changes created during the release
7. Travis CI will trigger and deploy
8. If Travis CI deploys without issues, don't merge yet, run `lein bump!`
9. PR the changes created during the `bump!` and once approved, merge the PR.
10. Verify if the new version was deployed to Clojars.
11. Pat yourself on the back! You just released the latest version of the lib

## License

Copyright © 2018 Medidata Solutions

This Library is licensed under the MIT licensing terms. See [LICENSE.md](https://github.com/mdsol/clojure-mauth-client/blob/master/LICENSE.md) for details
