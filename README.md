Open Travel Mate
================

Open Travel Mate is an open multi-platform transport information system.
Its goal is to simplify the life of transport users. It helps them to
find itineraries among all available transports (bus, metro, train,
taxi, car-share, ...).

The architecture is designed to be as extensible as possible. The
application is made of 2 layers:

 * Extensions written in JavaScript + HTML + CSS.
 * Native components offering functionalities to extensions via
   JavaScript interfaces.

The project target is to be a platform where transport information
providers (transport organisation authority, taxi company, car-share
solutions provider, ...) can put their information at the same place,
in order to improve the visibility of their services.


Installation
------------
The first step is to clone the project on your harddisk:

        git clone https://github.com/marcplouhinec/opentravelmate.git
        git submodule init
        git submodule update

The next steps depends on your target platform.

### Web platform
Simply double-click on `index.html`.

### Android platform
To access Google Maps v2, it is necessary to create a binding for the
Google Play services client library:

1. Use the Android SDK Manager to install Google Play Services.
2. Copy the directory located at `extras/google/google_play_services/libproject/google-play-services_lib`
   into `<opentravelmate-path>/android`.
3. In your IDE (for example Eclipse), create two project from sources
   located at `<opentravelmate-path>/android/google-play-services_lib`
   and `<opentravelmate-path>/android/app`
4. [Obtain a new API Key](https://developers.google.com/maps/documentation/android/start#the_google_maps_api_key)
   for Google Maps v2.
5. In the file `<opentravelmate-path>/android/AndroidManifest.xml`,
   put your key in the following XML element:
   
        <!-- Google Maps debug key -->
        <meta-data
            android:name="com.google.android.maps.v2.API_KEY"
            android:value="AIzaSyAUaKuJMg_WgYbgxnMK8-8PvJwgP84AEkA" />


Licence
-------
Copyright (C) 2013, Marc Plouhinec (marc.plouhinec@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

![LGPL-v3 logo](http://www.gnu.org/graphics/lgplv3-147x51.png)


Credits
-------

 * The application is developped under [Gentoo](http://www.gentoo.org/) and
   [Arch Linux](https://www.archlinux.org/) with [Eclipse](http://www.eclipse.org/)
   and [WebStorm](http://www.jetbrains.com/webstorm/) IDEs.
 * The logo has been created with [Gimp](http://www.gimp.org/) by using
   the [Galindo font](http://www.google.com/fonts/specimen/Galindo).
 * The common styles use the ["Roboto regular" font](http://www.google.com/fonts/specimen/Roboto).
