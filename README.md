# provider.android

Provider helpers for Android

## Setup

In your main build.gradle

```
allprojects {
    repositories {
        jcenter()
        google()
        <b>maven { url "https://jitpack.io" }</b>
    }
}
```

In your project

```
dependencies {
    <b>compile 'com.github.tekartik:provider.android:0.4.1'</b>
}
```

Bleeding edge

```
dependencies {
    <b>compile 'com.github.tekartik:provider.android-SNAPSHOT'</b>
}
```

## Dev

* [Development](doc/dev.md) information