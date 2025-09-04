# BYTE App Webview

This project is of very little relevance to anyone other than myself at the moment since it is just a webview for a personal web app. You may be interested in it for the same reason I was interested in the [project it is forked from](https://github.com/woheller69/gptAssist), that is: it provides a very simple implementation of a dedicated WebView app for a single website.

## BYTE App

The destination for the webview in this repository is an endpoint on my personal website which hosts a personal implementanion of a YouTube client that I created many years ago. I have dubbed this the 'Better YouTube Experience App' (BYTE App). This is a YouTube subscription manager which does not require a Google account and which provides access to videos via less distracting methods such as the embedded player.

The existing implementation is very rough, since I built it many years ago (2015 or earlier) when I was just learning PHP, so I have never made it open source. I have had plans to do a full re-write and make that public, but since then [better](https://flathub.org/apps/de.schmidhuberj.tubefeeder) [alternatives](https://github.com/TeamNewPipe/NewPipe) have come around.

## Webview

A 'webview' in mobile operating systems is an application which simply opens the content of an existing webpage as if it were a complete application. This provides some facilities to enhance the experience with more traditional application functions, such as local settings and state. However, this implementation is mean to be as bare-bones as possible, only using the isolation features.

This is forked from a project which is otherwise entirely unrelated and any non-essential features have been removed. All that is left is a webview which automatically load [one webpage](https://github.com/JohnMertz/BYTE-App-WebView/blob/edbefbe2da91c95d092bfd1f796a4d3023a14010/app/src/main/java/tz/me/john/youtube/MainActivity.java#L55) and then allows access to just a few others ([see allowDomains.add](https://github.com/JohnMertz/BYTE-App-WebView/blob/edbefbe2da91c95d092bfd1f796a4d3023a14010/app/src/main/java/tz/me/john/youtube/MainActivity.java#L220))

## Reusing this for your own projects

If you would like a WebView app for your own webpage, you can fork this project and simply replace the handful of application specific fields. These include:

* The `loadURL` and `allowDomains.add` functions mentioned in the previous section which define the destination page and any other allowed sources (this includes domains that the user is allowed to open links to as well as domains which may include content in any of the allowed pages, ie. for third-party JS or CSS).
* All references to the package name `tz.me.john.youtube`.
* Review additional settings in [the manifest file](https://github.com/JohnMertz/BYTE-App-WebView/blob/edbefbe2da91c95d092bfd1f796a4d3023a14010/app/src/main/AndroidManifest.xml).
* Review additional settings in [the Gradle build file](https://github.com/JohnMertz/BYTE-App-WebView/blob/edbefbe2da91c95d092bfd1f796a4d3023a14010/app/build.gradle).

If you would like to get ideas for additional features that can be added to your WebView, you can start by looking at the project that this is forked from.

## Building

If you open this repository in Android Studio, you should simply be able to click the build button. If successful the APK file will be stored at 'app/build/intermediates/apk/debug/app-debug.apk' when complete. You will need to sideload that APK onto your device.

## Disclaimer

As noted in the project that this was forked from, Google have announced that future releases of Android 'certified' devices (ie. those which come pre-installed with the Play Store) will be incapable of side-loading apps from unknown sources unless the builder has submitted detailed personal information to them. This means that it will likely not be possible to use packages build from this repository into the future, except on de-Googled devices.
