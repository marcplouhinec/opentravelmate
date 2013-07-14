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
providers (Transport organisation authority, Taxi company, Car-share
solutions providers, ...) can put their information at the same place,
in order to improve the visibility of their services.


Installation
------------
The first step is to clone the project on your harddisk:

        git clone https://github.com/marcplouhinec/opentravelmate.git

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
