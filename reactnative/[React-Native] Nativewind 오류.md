nativewind를 적용 후 앱을 실행시키고 home.tsx로 접속했을 때 아래와 같은 에러가 떴다.

```
 ERROR  app/(tabs)/home.tsx: [BABEL] /Users/kimtaekjun/Github/<프로젝트명>/app/(tabs)/home.tsx: .plugins is not a valid Plugin property
```

아래의 방법으로 nativewind를 삭제 후 다시 설치해 보기도 하고 node_modules 파일을 삭제 후 다시 깔아보기도 했지만 에러는 고쳐지지 않았다.

```shell
npm uninstall nativewind
npm install nativewind
```
```shell
rm -rf node_modules
npm install
```

하지만 더 쉬운 방법으로 에러를 고칠 수 있었다. 현재 설치된 nativewind를 삭제 후 안정화된 2.0.11 버전으로 재설치 하는 방법이었다.

```shell
npm uninstall nativewind
npm install nativewind@2.0.11
```

<br>

## 참고
https://stackoverflow.com/questions/77996575/babel-plugins-is-not-a-valid-plugin-property