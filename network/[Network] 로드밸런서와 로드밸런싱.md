## 로드밸런서(Load Balancer)란?
**로드밸런싱(Load Balancing)**을 제공해주는 하드웨어 장치나 소프트웨어 어플리케이션을 의미하고 클라이언트와 서버 그룹 사이에 위치해 서버에 가해지는 트래픽을 여러 대의 서버에 고르게 분배하여 특정 서버의 부하를 덜어주며, 이러한 문제를 해결하기 위해서 **스케일 업(Scale up)**과 **스케일 아웃(Scale out)** 방식 중 한 가지를 사용해 해결한다.

### 로드밸런서의 서버 확장 방식
![](https://velog.velcdn.com/images/kimtaekjun/post/bab00142-e4db-4a9f-b039-aa7b80064c46/image.png)

[출처: SmileShark 블로그](https://www.smileshark.kr/post/what-is-a-load-balancer-a-comprehensive-guide-to-aws-load-balancer)

#### 스케일 업(Scale up)
기존 서버의 성능을 향상시키는 방법으로, CPU, 메모리, 디스크 등의 자원을 업그레이드하여 처리 능력을 향상시킨다. 예를 들어 서버의 CPU를 4코어에서 8코어로, 메모리를 16GB에서 32GB로 업그레이드하는 것이다.
> **스케일 업(Scale up)**은 확장성에 한계가 있고 비용 증가 폭이 크지만 관리와 운영에 큰 변화가 없으며 장애 시 다운 타임이 발생한다.

#### 스케일 아웃(Scale out)
여러 대의 컴퓨터나 서버를 추가하여 트래픽이나 작업을 분산시켜 처리하는 방식으로, **로드밸런서(Load Balancer)**를 통해 트래픽을 분산시킨다. 예를 들어 기존의 2대의 서버에 2대를 더 추가하여 총 4대의 서버로 트래픽을 분산 처리하는 것이다.
> **스케일 아웃(Scale out)**은 지속적 확장이 가능하고 비용 부담이 적지만 관리 편의성과 운영 비용이 증가하며 장애 시 전면 장애 가능성이 적다.

## AWS 로드밸런서 종류
### ALB(Application Load Balancer)
OSI 7계층에서 동작하며 HTTP/HTTPS 트래픽을 라우팅하고 로드밸런싱하는 데 사용되고 다양한 라우팅 방식을 지원하며 WAF와 통합하여 보안 기능을 제공하며 컨테이너화된 애플리케이션 및 마이크로서비스 아키텍처에 적합함.

### NLB(Network Load Balancer)
OSI 4계층에서 동작하며 TCP/UDP 트래픽을 로드밸런싱하는 데 사용되고 고성능, 저지연 요구 사항이 있는 애플리케이션에 적합하며 탄력적 IP 주소를 통해 고정 IP를 제공하며 장애 조치 시나리오에서 빠른 응답 시간을 제공함.

### ELB(Elastic Load Balancer)
AWS의 기존 로드밸런서로 OSI 4계층과 7계층에서 동작하고 HTTP/HTTPS 및 TCP 트래픽을 로드밸런싱하는 데 사용되며, 기본적인 라우팅 및 상태 확인 기능을 제공하지만 새로운 애플리케이션의 경우 ALB 또는 NLB 사용이 권장됨.

<br>

## 로드밸런싱(Load Balancing)이란?
여러 대의 컴퓨터나 서버에 작업을 고르게 분산하여 처리하는 걸 의미하고 트래픽이 과도하게 몰려 서비스가 중단되는 일을 막고 지연 없이 작업을 처리하며, 이를 통해 애플리케이션의 가용성과 확장성을 높일 수 있다.

### 다양한 로드밸런싱 알고리즘
#### 정적 로드밸런싱
- **`라운드 로빈 방식(Round Robin Method)`**
  - 클라이언트의 요청을 여러 대의 서버에 순차적으로 분배하는 방식이다.
  - 서버의 성능이 동일하고, 서버와의 연결(세션)이 오래 지속되지 않은 경우에 활용


- **`가중치 기반 라운드 로빈 방식(Weighted Round Robin Method)`**
  - 각 서버마다 가중치를 부여하고, 가중치가 높은 서버에 클라이언트 요청을 우선적으로 배분하는 방식이다.
  - 여러 서버가 같은 사양이 아니고, 특정 서버의 스펙이 더 좋은 경우 해당 서버의 가중치를 높게 매겨 트래픽 처리량을 늘림
    - A서버의 가중치가 2, B서버의 가중치가 1일 때 A서버 2개, B서버 1개로 요청을 전달


- **`IP 해시 방식(IP Hash Method)`**
  - 클라이언트의 IP 주소를 해싱(Hashing)하여 특정 서버에 요청을 처리하는 방식이다.
  - 사용자의 IP를 기반으로 해싱(Hashing)하여 부하를 분산하기 때문에 사용자가 매번 동일한 서버에 연결

### 동적 로드밸런싱
- **`최소 연결 방법(Least Connection Method)`**
  - 요청이 들어온 시점에 가장 적은 연결(세션) 상태를 보이는 서버에 우선적으로 트래픽을 배분하는 방식이다. 
  - 자주 연결(세션)이 길어지거나, 서버에 분배된 트래픽들이 일정하지 않은 경우에 활용


- **`최소 응답 시간 방법(Least Response Time Method)`**
  - 서버의 현재 연결 상태와 응답 시간(Response Time)을 모두 고려하여 가장 짧은 응답 시간을 보내는 서버로 트래픽을 배분하는 방식이다.
  - 각 서버의 가용한 리소스와 성능, 처리 중인 데이터 양이 상이할 경우에 활용

### 로드밸런싱의 종류
![](https://velog.velcdn.com/images/kimtaekjun/post/86019039-7443-4900-ba7d-dcb2b0965f82/image.png)

#### L4 로드밸런싱
네트워크 계층이나 전송 계층에서 작동하고 IP 주소나 포트 번호, 프로토콜 등의 정보를 기반으로 트래픽을 분산하며, 전송 프로토콜에 따라 트래픽을 분산하는 것이 가능하다.

#### L7 로드밸런싱
애플리케이션 계층에서 작동하고 HTTP 헤더나 쿠키, URL 등의 정보를 기반으로 트래픽을 분산하며, 사용자의 요청을 기준으로 특정 서버에 트래픽을 분산하는 것이 가능하다.