## /etc/hosts

이 파일은 호스트명과 IP 주소를 매핑하여, DNS를 거치지 않고도 로컬 시스템에서 호스트명을 IP 주소로 변환할 수 있게 한다. 주로 작은 네트워크 환경이나 고정된 IP로 운영되는 서버를 대신하여 사용된다.

예를 들어, **/etc/hosts** 파일에 다음과 같이 설정하면

```
127.0.0.1      localhost
192.168.1.10   server1.example.com
```

이제 server1.example.com으로 접근하면 해당 IP 주소인 192.168.1.10으로 연결된다.

<br>

## /etc/resolv.conf

이 파일은 DNS 서버를 통해 도메인 이름을 IP 주소로 변환할 때 사용하는 설정을 지정하며, 도메인 이름을 확인하려고 할 때 **/etc/hosts** 파일에 없으면, 이 파일에 정의된 **nameserver**로 설정된 DNS 서버를 참조하여 IP 주소를 찾는다.

**/etc/resolv.conf** 파일의 내용은 다음과 같다.

```
search example.com
nameserver 8.8.8.8
nameserver 8.8.4.4
```

- **search**
  - 짧은 호스트명만 입력했을 때 자동으로 추가될 기본 도메인을 지정한다.
    - 예) `ping server1 입력 시 server1.example.com를 찾는다.`
- **nameserver**
  - 사용할 DNS 서버의 IP 주소를 지정한다.
    - 예) `8.8.8.8과 8.8.4.4를 사용하며, 첫 번째 서버가 응답하지 않을 때 두 번째 서버로 요청한다.`

<br>

## 주의사항

일부 리눅스 배포판에서는 **네트워크 매니저(NetworkManager)**나 **systemd-resolved**와 같은 서비스가 **/etc/resolv.conf** 파일을 자동으로 관리하고 수동으로 파일을 수정해도 재부팅 시 설정이 덮어씌워질 수 있으며, 해당 서비스의 설정을 통해 DNS를 구성하는 것이 좋다.
