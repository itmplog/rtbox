RTBox (RootBox)
===============

[![Build Status](https://travis-ci.org/nullog/rtbox.svg?branch=master)](https://travis-ci.org/nullog/rtbox) 
[![Circle CI](https://circleci.com/gh/nullog/rtbox.svg?style=svg)](https://circleci.com/gh/nullog/rtbox) 
[![Build Status](https://drone.io/github.com/nullog/rtbox/status.png)](https://drone.io/github.com/nullog/rtbox/latest) 

This ia a library to simplify the usage of root exec on the Android OS.It is a Java wrapper around native binaries shipped with every Android OS, but can also be used to package and execute your own native binaries.

Use library as Gradle dependency
================================

1. The Last VERSION: 
[ ![Download](https://api.bintray.com/packages/itmp/top/top.itmp.rtbox/images/download.svg) ](https://bintray.com/itmp/top/top.itmp.rtbox/_latestVersion)

2.  Gradle:
    ```
    compile 'top.itmp.rtbox:rtbox:0.2.0'
    ```

3. Maven:
    ```
    <dependency>
      <groupId>top.itmp.rtbox</groupId>
      <artifactId>rtbox</artifactId>
      <version>0.2.0</version>
      <type>pom</type>
    </dependency>
    ```

4. Snapshots:
	- Add it in your root build.gradle at the end of repositories:

		```
		allprojects {
			repositories {
				...
					maven { url "https://jitpack.io" }
			}
		}
		```

	- Add the dependency

		```
		dependencies {
			compile 'com.github.nullog:rtbox:-SNAPSHOT'
		}
		```

	[![](https://jitpack.io/v/nullog/rtbox.svg)](https://jitpack.io/#nullog/rtbox)
	[Download The Last AAR](https://jitpack.io/com/github/nullog/rtbox/-SNAPSHOT/rtbox--SNAPSHOT.aar)
	> How to use the last aar:

	* Add it in your `app` build.gradle at the end of repositories: 

		```
		repositories {
			flatDir {
				dirs 'libs'
			}
			...
		}
		```

	* add dependencies:

		```
		dependencies {
			compile(name:'rtbox--SNAPSHOT', ext:'aar')
				...
		}
		```


Contribute
==========
Fork RTBox and do a Pull Request. I will merge your changes back into the main project.

Other Root Libraries
====================
- https://github.com/Chainfire/libsuperuser
- https://github.com/Free-Software-for-Android/RootCommands
- https://github.com/Stericson/RootTools

Authors
=======

RTBox is based on serveral other open source projects:
- Dominik Sch¨¹rmann (RootCommands)
- Stephen Erickson, Chris Ravenscroft, Adam Shanks, Jeremy Lakeman (RootTools)
- Michael Elsdrfer (Android Autostarts)

License
=======

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



