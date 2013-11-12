[![Build Status](https://travis-ci.org/eyeem/chips-android.png)](https://travis-ci.org/eyeem/chips-android)

Chips library from EyeEm
=================

EyeEm style bubbles (a.k.a. chips).

[![](http://cdn.eyeem.com/thumb/h/400/f88f4ba735e60e5b6faa24b252d1c1b62e375f72-1384269072)] [![](http://cdn.eyeem.com/thumb/h/400/8c660660033aac40d3d099fbc220e993c57ed7eb-1384269111)]

Usage
============
There are two main widgets which you can use:

- `ChipsEditText` for editable text and bubbles with an optional `AutocompletePopover`.
- `ChipsTextView` if you plan only on displaying non-editable text with bubbles and optionally wish to provide some feedback on bubble press.

Check out included `example` app to see how to play and what can be done with this library.

Including in your project
=========================

You can either check out the repo manually or grab a snapshot `aar` which is hosted on sonatype repo. To do so, include this in your build.gradle file:

```
dependencies {

    repositories {
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
        mavenCentral()
        mavenLocal()
    }

    compile 'com.eyeem.chips:library:0.9.0-SNAPSHOT@aar'

    // ...other dependencies
}
```

Developed By
============

* Lukasz Wisniewski [@vishna](https://twitter.com/vishna)

Whorthwhile mentions
============
- [chips-edittext-library](https://github.com/kpbird/chips-edittext-library)
- [chips from Google](https://android.googlesource.com/platform/frameworks/ex/+/refs/heads/master/chips)

License
=======

    Copyright 2013 EyeEm Mobile GmbH

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.