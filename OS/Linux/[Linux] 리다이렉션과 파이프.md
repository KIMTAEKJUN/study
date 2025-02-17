## 표준 입출력(Standard Stream)

리눅스는 세 가지 표준 스트림(stdin, stdout, stderr)를 가지고있고 표준 입출력(Standard Stream)은 프로세스가 생성되면 기본적으로 생기는 입출력을 위한 채널이며, 터미널을 열면 쉘 프로세스의 세 가지 표준 스트림(stdin, stdout, stderr)이 모두 터미널에 연결되어 사용자로부터 입력을 받고 출력한다.

- **`표준 입력(stdin)`**: 데이터를 입력하는 채널

  - `File Descriptor`: 0
  - 예) **`키보드 입력, 파일 입력 등`**

- **`표준 출력(stdout)`**: 실행 결과가 출력되는 채널

  - `File Descriptor`: 1
  - 예) **`디스플레이 출력, 파일 출력 등`**

- **`표준 에러 출력(stderr)`**: 실행 중 발생하는 에러 메세지가 출력되는 채널

  - `File Descriptor`: 2
  - 예) **`디스플레이 출력, 파일 출력 등`**

- **`파일 디스크립터(File Descriptor)`**:
  - 운영 체제에서 프로세스가 열려있는 파일이나 입출력 리소스를 식별하는 데 사용되는 숫자.

<br>

## 리다이렉션(Redirection)

표준 스트림(Standard Stream)의 흐름을 바꿔 키보드로 표준 입력을 받거나 화면으로 표준 출력을 하는 것이 아니라 파일로 표준 입/출력 등의 기능을 제공하며, >, < 기호를 사용해서 리다이렉션을 사용할 수 있다.

- **`출력 리다이렉션(>)`**:

  - **`[명령어] > [파일]`**:
    - 명령어의 표준 출력을 파일로 보내고 파일이 존재하지 않으면 새로 생성되며, 존재하면 덮어씌움.
      - 출력을 example.txt에 저장하며, 기존 example.txt가 있다면 덮어씌움.
        - 예) **`ls > example.txt`**

- **`입력 리다이렉션(<)`**:

  - **`[명령어] < [파일]`**:
    - 명령어의 표준 입력을 파일로부터 받음.
      - example.txt 파일의 내용을 sort 명령어의 입력으로 사용
        - 예) **`sort < example.txt`**

- **`출력 추가 리다이렉션(>>)`**:
  - **`[명령어] >> [파일]`**:
    - 명령어의 표준 출력을 파일에 추가하며, 파일이 존재하지 않으면 새로 생성됨.
      - 출력을 example.txt에 추가되고 기존 example.txt의 내용은 유지하며 새로운 내용을 추가
        - 예) **`echo "Hello World!" >> example.txt`**

<br>

## 파이프(Pipe)

두 개의 프로세스를 연결하여 한 프로세스의 표준 출력을 다른 프로세스의 표준 입력으로 사용하고자 할 때 사용하며, 파이프(|)를 사용하면 여러 명령어를 연결하여 복잡한 작업을 수행할 수 있다.

특히 어떤 파일에서 어떤 문자열을 포함한 행을 찾을 때 pipe와 grep 명령어를 조합해서 많이 사용한다.

- **`[명령어] | [명령어]`**:
  - 출력 결과를 grep 명령어로 필터링하여 ".txt" 확장자를 가진 파일만 출력
    - 예) **`ls | grep ".txt"`**
  - 출력 결과에서 mysql이라는 문자열이 포함된 프로세스 정보만 필터링하여 출력
    - 예) **`ps aux | grep mysql`**

<br>

## 표준 스트림 번호 활용

표준 스트림 번호란 프로세스가 데이터를 입출력할 때 사용하는 추상적인 통로이고 각각의 스트림은 고유한 번호를 가지고 있으며, 각 스트림의 번호를 이용한 다양한 출력 제어가 가능하다.

- **`에러 출력 리다이렉션`**:

  - 오류 메시지만 파일로 저장
    - 예) **`command 2> error.log`**

- **`모든 출력 리다이렉션`**:

  - 일반 출력과 에러 출력을 모두 파일로 저장
    - 예) **`command > output.txt 2>&1`**

- **`간편한 모든 출력 리다이렉션`**:
  - 모든 출력을 한 파일로 저장하는 축약형
    - 예) **`command &> all.log`**
