# Intro

Not ready yet

[![Build Status](https://travis-ci.org/narkisr/grant.png)](https://travis-ci.org/narkisr/grant)


# Usage


# Build

Make sure to have the latest graalvm native-image tool:

```bash
$ lein uberjar
$ native-image -jar target/grant-0.1.0-standalone.jar --no-fallback --report-unsupported-elements-at-runtime --initialize-at-build-time --allow-incomplete-classpath
```

# Copyright and license

Copyright [2020] [Ronen Narkis]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
