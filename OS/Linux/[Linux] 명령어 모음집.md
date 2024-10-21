> 회사에서 처음 사용해본 명령어 및 실무에 많이 사용되는 명령어에 대해 정리하게 되었고 앞으로도 계속 명령어에 대해 추가 예정입니다.

## 네트워크 관련 명령어
- **`dig [DOMAIN]`**:
  - 해당 도메인의 DNS 정보를 조회함.
    - 예) `dig naver.com`

- **`dig [@서버] [도메인] [쿼리타입]`**:
    - 특정 DNS 서버에 도메인 정보를 조회.
        - **`@서버`**: 사용할 DNS 서버 지정
        - **`도메인`**: 조회하려는 도메인 이름
        - **`쿼리타입`**: A, MX, NS 등 (생략 시 A 레코드)
            - 예) **`dig @8.8.8.8 example.com MX`**: Google DNS 서버에 example.com의 MX 레코드 조회


- **`curl -v [옵션] [URL]`**:
    - **주요 옵션**:
        - **`-v`**: 상세한 정보 출력 (요청/응답 헤더 포함)
        - **`-i`**: 응답 헤더를 포함한 전체 HTTP 응답 출력
        - **`-X [메소드]`**: HTTP 메소드 지정 (GET, POST 등)
        - **`-d "[데이터]"`**: POST 데이터 지정
    - **예시**:
        - GET 요청: **`curl https://example.com`**
        - 상세 정보 출력: **`curl -v https://example.com`**
        - POST 요청: **`curl -v -X POST https://api.example.com/data -d "name=John&age=30"`**


- **`nc -vv [호스트] [포트]`**:
  - 위 명령어는 특정 호스트의 포트에 연결을 시도함.
    - **`-vv`**: 매우 상세한**(very verbose)** 출력을 제공함.
    - 포트가 열려 있으면 연결 성공, 닫혀 있으면 연결에 실패함.
      - 예) `nc -vv naver.com 80`


- **`nc [옵션] [호스트] [포트]`**:
    - 네트워크 연결을 생성하고 데이터를 전송.
        - **파일 송신 (서버)**:
            - 예) **`nc -l -p 1234 < example.txt`**
        - **파일 수신 (클라이언트)**:
            - 예) **`nc [서버IP] 1234 > example.txt`**


- **`netstat [옵션]`**:
    - 네트워크 연결 상태, 라우팅 테이블, 인터페이스 통계 등을 보여줌.
        - **주요 옵션**:
            - **`-a`**: 모든 소켓을 표시 (리스닝 및 비리스닝 상태 모두)
            - **`-t`**: TCP 연결 표시
            - **`-u`**: UDP 연결 표시
            - **`-l`**: 리스닝 상태의 소켓만 표시
            - **`-n`**: 호스트명, 포트번호를 숫자로 표시
            - **`-p`**: 프로세스 ID와 프로그램 이름 표시 (Linux에서만 가능, root 권한 필요)


- **`ifconfig`** 또는 **`ip addr`**:
    - 네트워크 인터페이스의 구성 정보를 보여줌.
        - IP 주소, MAC 주소, 네트워크 마스크, 브로드캐스트 주소 등 표시

<br>

## 프로세스 및 포트 관리 명령어
- **`lsof -i [:포트]`**:
  - 특정 포트를 사용 중인 프로세스를 확인할 수 있음.
    - 예) `lsof -i :80 OR sudo lsof -i :80`
    - 만약의 특정 포트를 죽이고싶으면 kill -9 [PID] 를 입력하면 됨.
      - 예) `kill -9 1234 OR sudo kill -9 1234`


- **`ps aux`**:
  - 현재 실행 중인 모든 프로세스의 상세 정보를 보여줌.
    - **`a`**: 터미널과 연결된 모든 프로세스
    - **`u`**: 프로세스 소유자 정보 포함
    - **`x`**: 터미널에 연결되지 않은 프로세스도 포함
      - 예) `ps aux | grep [프로세스]`

<br>

## 원격 접속 및 파일 전송
- **`ssh [옵션] [사용자명@호스트]`**:
    - **주요 옵션**:
        - **`-p [포트번호]`**: 기본 22번 포트가 아닌 다른 포트로 접속
        - **`-i [키파일]`**: 인증에 사용할 개인키 파일 지정
        - **`-v`**: 자세한 연결 과정 출력 (디버깅용)
        - **`-L [로컬포트]:[원격호스트]:[원격포트]`**: 로컬 포트 포워딩
    - **예시**:
        - 일반 접속: **`ssh user@example.com`**
        - 다른 포트로 접속: **`ssh -p 2222 user@example.com`**
        - 키 파일 사용: **`ssh -i ~/.ssh/my_private_key user@example.com`**
        - Lightsail 인스턴스 접속: **`ssh -i /path/to/your-lightsail-key.pem username@your-lightsail-ip`**


- **`scp 명령어로 인스턴스에 파일 복사하기`**:
  - **로컬에서 원격 서버로 파일 복사:**
    - 예) **`scp -r -i /path/to/key.pem /path/to/local/file username@remote_host:/path/to/remote/directory`**
  - **원격 서버에서 로컬로 파일 복사:**
    - 예) **`scp -r -i /path/to/key.pem username@remote_host:/path/to/remote/file /path/to/local/directory`** 

<br>

## Screen 사용법 및 명령어
- **`Screen 세션 생성`**:
  - 예) `screen -S [session_name or id]`


- **`Screen 세션 연결`**:
  - **Single Display Mode**
    - 예)  `screen -r [session_name or id]`
  - **Multi Display Mode**
    - 예) `screen -x [session_name or id]`


- **`Screen 세션 빠져나오기, 전환하기`**:
  - `Ctrl + A` 를 누른 후, 뗀 다음 `D` 누르기
    - **Screen 세션에서 빠져나온 후 그 다음 원하는 세션으로 전환함.**
      - 예) `screen -r [session_name or id]`


- **`이전 화면 읽어보기(스크롤백)`**:
  - `Ctrl + A` 를 누른 후, 뗀 다음 `[` 누르기


- **`현재 실행 중인 모든 Screen 세션 보기`**:
  - 예) `screen -ls`


- **`Screen 세션 종료하기`**:
  - 예) `screen -S [session_name or id] -X  quit`

<br>

## 파일 및 디렉토리 조작 명령어
- **`mv [이동할 디렉터리/파일] [이동할 위치]`**:
  - 디렉터리나 파일을 이동하거나 이름을 변경하는데 사용함.
    - **파일 이동:**
      - 예) `mv mv example.txt directory`
      - 예) `mv example.txt /Users/username/directory`
    - **파일 이름 변경:**
      - 예) `mv example.txt example2.txt`


- **`grep [옵션] [패턴] [파일]`**:
  - 파일 내용에서 특정 패턴을 검색함.
    - **소스코드 내 특정 문자열 찾기**:
      - 현재 디렉토리의 모든 .js파일에서 'import' 문 찾기
        - 예) `grep "import" *.js`
      - 하위 디렉토리를 포함한 모든 .js 파일에서 'import' 찾기
        - 예) `grep -r "import" *.js`
      - 대소문자 구분 없이 'error' 문자열 찾기
        - 예) `grep -i "error" *.js`


  - **다른 명령어와 리다이렉트 되어 출력 필터링하기**:
    - 실행 중인 프로세스 중 'mysql'이 포함된 것만 표시
      - 예) `ps aux | grep mysql`
    - 디렉토리 내용 중 '.json' 파일만 표시
      - 예) `ls -l | grep '.json'`
    - 'error'가 포함된 줄만 실시간으로 표시
      - 예) `tail -f [directory] | grep error`


  - **주요 옵션**:
    - **`-i`**: 대소문자 구분 없이 검색
      - 예) `grep -i "error" *.js`
    - **`-r`** 또는 **`-R`**: 하위 디렉토리를 재귀적으로 검색
      - 예) `grep -r "import" *.js`
    - **`-n`**: 매칭된 줄의 번호도 함께 표시
      - 예) `grep -n "function" *.js`


- **`chmod [옵션] [모드] [파일]`**:
  - 파일이나 디렉토리의 권한을 변경함.
    - script.sh 파일에 실행 권한 부여
      - 예) `chmod 755 example.sh`
    - directory와 그 하위 모든 파일의 권한을 644로 변경
      - 예) `chmod -R 644 [directory]`

<br>

## 명령어 히스토리 관리 명령어
- **`history`**:
  - 사용자가 입력한 명령어 기록을 보여줌.
    - **특정 개수의 명령어 보기**: 
      - 예) `history [n]`
    - **특정 명령어 검색**:
      - 예) `history | grep [keyword]`

<br>

## vi 편집기 사용법 및 명령어
- **`vi 파일 열기`**: `vi [파일이름]`
    - 예) `vi example.txt`


- **`vi 파일 편집하기`**:
    - `키보드 I 누르기`: Insert 모드로 전환됨.


- **`vi 저장`**:
    - **저장 안하고 나가기**: `ESC`를 누른 후 `:q!` 입력
    - **저장하고 나가기**: `ESC`를 누른 후 `:wq` 입력
    - **저장만 하기**: `ESC`를 누른 후 `:w` 입력


- **`vi 특정 라인으로 이동하기`**:
    - `ESC`를 누른 후 `:[line]` 입력
    - 예) `:24` 번째 줄로 이동


- **`vi 아래 화면 가기`**:
    - `Ctrl + F` 또는 `Page Down` 키
- **`vi 위 화면 가기`**:
    - `Ctrl + B` 또는 `Page Down` 키


- **`vi 특정 문자열 검색하기`**:
    - **전방 검색**: `ESC`를 누른 후 `/[검색어]`  입력
    - **후방 검색**: `ESC`를 누른 후 `?[검색어]` 입력
    - **다음 검색 결과로 이동**: `n`
    - **이전 검색 결과로 이동**: `N`


## 관리자 권한 실행 명령어
- **`sudo [커맨드]`**:
  - 일시적으로 관리자(root) 권한으로 명령어를 실행함.
  - sudo는 시스템에 중요한 영향을 미칠 수 있으므로, 필요한 경우에만 제한적이고 신중하게 사용해야 함.

<br>

## 그 외 명령어
- `forever`
  - Node.js 스크립트를 백그라운드에서 지속적으로 실행하는데 사용함.
    - **forever 설치하기**: `npm install -g forever`
      - **실행 중인 스크립트 목록**: `forever list`
      - **스크립트 시작**: `forever start app.js`
      - **스크립트 종료**: `forever stop app.js`