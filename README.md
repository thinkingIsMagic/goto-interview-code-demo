## Gojek Android Machine Code

Requirements
- Android Studio Ladybug | 2024.2.1 Patch 1
- JDK 21 (Built in Android Studio JDK)

## Troubleshoot

```
* What went wrong:
  Execution failed for task ':app:parseDebugLocalResources'.
> Could not resolve all files for configuration ':app:androidApis'.
> Failed to transform android.jar to match attributes {artifactType=android-platform-attr, org.gradle.libraryelements=jar, org.gradle.usage=java-runtime}.
> Execution failed for PlatformAttrTransform: /Users/raditya.gumay/Library/Android/sdk/platforms/android-35/android.jar.
> /Users/raditya.gumay/Library/Android/sdk/platforms/android-35/android.jar
```
// remove /Users/raditya.gumay/Library/Android/sdk/platforms/android-35 directory and redownload it