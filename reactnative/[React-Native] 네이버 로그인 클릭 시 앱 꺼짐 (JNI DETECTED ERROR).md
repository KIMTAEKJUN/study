안드로이드 에뮬레이터 앱에서 네이버 로그인 기능에 대해 테스트 중 네이버 로그인 버튼을 클릭하면 앱이 강제 종료(crash) 되는 문제가 발생했습니다.

## JNI 오류

안드로이드 에뮬레이터에서 네이버 로그인 버튼 클릭 시 앱이 강제 종료됩니다. adb logcat을 통해 로그를 확인해보면 JNI 오류가 발생하고 있었습니다.

```bash
adb logcat | grep -i nave
```

### 로그 내용

네이버 로그인 모듈에서 JNI 호출 시 반환 타입이 맞지 않아 오류가 발생했습니다. Java 메서드는 boolean을 반환하지만 JNI에서는 void로 호출해 앱이 종료된 것입니다.

```jsx
JNI DETECTED ERROR IN APPLICATION: the return type of CallVoidMethodA does not match boolean com.dooboolab.naverlogin.RNNaverLoginModule.login(com.facebook.react.bridge.Promise)
```

<br>

## 에러 해결

### **1. @react-native-seoul/naver-login 라이브러리 최신 버전으로 업데이트**

```jsx
npm install @react-native-seoul/naver-login@latest
```

### **2. android/app/build.gradle 설정 수정**

릴리즈 빌드 시 코드 난독화나 리소스 축소(shrinkResources) 설정이 네이버 로그인 SDK와 충돌할 수 있기에 buildTypes 설정에서 Proguard 관련 설정합니다.

```bash
# Proguard 설정
buildTypes {
    debug {
        signingConfig signingConfigs.debug
    }
    release {
        signingConfig signingConfigs.debug
        shrinkResources (findProperty('android.enableShrinkResourcesInReleaseBuilds')?.toBoolean() ?: false)
        minifyEnabled enableProguardInReleaseBuilds
        proguardFiles getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro"
        crunchPngs (findProperty('android.enablePngCrunchInReleaseBuilds')?.toBoolean() ?: true)
    }
}
```

다양한 ABI(Android Binary Interface)에서 사용하는 libc++\_shared.so 파일 충돌을 방지하기 위해 pickFirst 옵션을 추가합니다.

```bash
# Packaging Options 설정
packagingOptions {
    jniLibs {
        useLegacyPackaging (findProperty('expo.useLegacyPackaging')?.toBoolean() ?: false)
    }
    pickFirst 'lib/x86/libc++_shared.so'
    pickFirst 'lib/x86_64/libc++_shared.so'
    pickFirst 'lib/armeabi-v7a/libc++_shared.so'
    pickFirst 'lib/arm64-v8a/libc++_shared.so'
}
```
