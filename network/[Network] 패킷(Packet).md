## 패킷(Packet)이란?

패킷(Packet)이란 **네트워크에서 데이터를 주고받을 때 사용되는 데이터 조각**이자 데이터를 작은 단위로 나누어 전송하는 기본 단위이다.

### 패킷(Packet)의 구조

일반적으로 패킷은 1,000에서 1,500 byte의 크기이며, 아래와 같이 세 가지로 이루어져 있다.
![패킷구조](https://velog.velcdn.com/images/kimtaekjun/post/187d0021-dd0f-46bf-bd5a-3c6e51976d7f/image.png)

- **헤더(Header)**: 패킷에 대한 제어 정보를 담고 있다. 여기에는 패킷의 길이, 번호, 네트워크 프로토콜, 송신자 및 수신자의 IP 주소 등이 포함된다. 이 정보는 데이터가 정확하고 효율적으로 전달되기 위해 필요하며, 사용하는 IP 버전에 따라 헤더 내용은 다를 수 있다.
- **페이로드(Payload)**: 실제로 전달하고자 하는 데이터이다. 수신자가 요청한 이메일 본문, 웹 페이지의 콘텐츠, 이미지 데이터 등이 여기에 해당하며, ‘Body(바디)’라고도 불린다.
- **트레일러(Trailer)**: 패킷의 끝을 표시하며, 오류 검출 기능을 담당한다. 트레일러에는 체크섬(Checksum)이나 순환 중복 검사(CRC, Cyclic Redundancy Check) 코드가 포함되어 있어 데이터 전송 중 손실이나 오류 여부를 확인할 수 있다.

### 와이어샤크(Wireshark)를 통해 패킷 보기

**와이어샤크(Wireshark)**는 네트워크에서 전송되는 패킷을 실시간으로 캡처하고 분석할 수 있는 오픈 소스 소프트웨어이다. 이를 통해 네트워크 트래픽을 상세히 모니터링하고, 문제를 진단하며, 보안 취약점을 파악하는 데 활용된다.

![](https://velog.velcdn.com/images/kimtaekjun/post/82b6828e-331b-4ac9-92e0-a64fcf50b07c/image.png)

1. **설치**
   - **와이어샤크(Wireshark)** [공식 웹사이트](https://www.wireshark.org/download.html)에서 프로그램을 다운로드하여 설치한다.
2. **네트워크 인터페이스 선택**
   - 프로그램 실행 후, 패킷을 캡처할 네트워크 인터페이스를 선택한다. 활성화된 네트워크 인터페이스는 트래픽 활동이 표시되므로 이를 참고하여 선택한다.
3. **패킷 캡처 시작**
   - 화면 왼쪽 위의 빨간색으로 표시된 “Start Capturing Packets” 버튼을 클릭하면 패킷 캡처가 시작된다. (이미지 참고)
4. **패킷 분석**
   - 캡처된 패킷 목록에서 특정 패킷을 클릭하면 하단에 패킷의 상세 정보가 계층별로 표시된다.
     - **프레임(Frame)**: 전체 패킷의 요약 정보.
     - **Ethernet Header**: 데이터 링크 계층의 정보.
     - **IP Header**: 네트워크 계층의 정보.
     - **TCP/UDP Header**: 전송 계층의 정보.
     - **Payload**: 실제 데이터.
5. **필터링**
   - **필터 박스**를 이용하여 원하는 조건의 패킷만을 선택적으로 볼 수 있다.
     - 예) http (HTTP 트래픽만 표시), ip.src == 192.168.1.1 (특정 IP에서 송신된 패킷만 표시)

<br>

## 참고한 글

[1. 패킷(Packet) | 토스페이먼츠 개발자센터](https://docs.tosspayments.com/resources/glossary/packet)  
[2. [Network] Packet: 패킷의 개념, 생성 원리](https://engineerinsight.tistory.com/302)  
[3. 와이어샤크(Wireshark) 개념 / 설치 방법](https://seoinsung.tistory.com/72)  
[4. [네트워크] 와이어샤크란?](https://12bme.tistory.com/512)
