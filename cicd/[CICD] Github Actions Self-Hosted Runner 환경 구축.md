## Self-Hosted Runner란?

- GitHub Actions에서 **사용자가 직접 관리하는 컴퓨터/서버**를 빌드 및 배포 환경으로 사용하는 기능입니다.
  - **기본 방식**: GitHub 클라우드 서버 사용
  - **Self-Hosted**: 내 맥북, 회사 서버, AWS EC2 등 사용
  - **목적**: GitHub Actions 워크플로우를 내 환경에서 실행

<br>

## 발생 문제

회사에서 진행 중인 프로젝트의 테스트 서버 CI/CD 파이프라인을 구축하고자 했습니다.
하지만 인프라 구조와 보안 정책 때문에 일반적인 방식으로는 GitHub Actions를 통한 배포가 불가능한 상황이었습니다.

1. **EC2 Bastion 호스트를 통한 접근 구조**
   - 회사의 테스트/프로덕션 EC2 인스턴스들은 **Private Subnet**에 위치해 있어 직접 외부 접근이 불가능
   - 모든 배포 및 SSH 접속은 반드시 **Bastion 호스트(중간 관문 서버)**를 경유해야 함
2. **보안 정책에 따른 접근 실패**
   - Bastion 호스트의 보안 그룹(Security Group) 설정상, SSH(22번 포트)는 **회사 사무실의 고정 IP**만 허용
   - 반면, **Github-Hosted Runner의 Runner는 매번 다른 IP 대역에서 실행**되므로, **SSH 연결 시 접근 거부 발생 → 배포 실패**

### Github-Hosted Runner IP 변경 문제

- Github-Hosted Runner는 **140개 이상의 IP 범위** 사용 (github.com/meta API 기준)
  - IP 범위 예시: **`4.175.96.0/22, 13.64.0.0/16, 20.*.*.*/16`** 등
- 따라서 **매 실행마다 다른 IP**에서 Runner가 동작함 → 고정 IP 기반 화이트리스트 구성 불가

### 전에 시도했던 실패한 해결책

1. **모든 Github-Hosted Runner IP 전체 허용**
   - **보안상 위험도가 너무 큼**
2. **특정 IP만 허용**
   - **지속적인 IP 변경으로 실패**
3. **VPN 서버 구성 후 Github-Hosted Runner → VPN 경유**
   - **설정 복잡함 + VPN 유지 비용**

<br>

## 해결 방안

1. **개인 맥북에 Self-Hosted Runner 구축 (임시/개발용)**
   - 현재는 혼자 개발 중인 프로젝트이므로 이 방식을 우선 적용
   - **장점:**
     - **IP 문제 완전 해결** - 회사 고정 IP로 SSH 가능
     - **즉시 구축 가능** - 추가 서버 없이 즉시 배포 가능
     - **기존 워크플로우 재활용** - `runs-on: self-hosted`로 Runner 타입만 수정
     - **비용 효율적** - 추가 인프라 비용 없음

> **CI/CD 파이프라인 배포 플로우**: Git Push → 맥북 Runner → 로컬 빌드 → 베스천 전송 → Target EC2 Instance 배포

2. **추후 내부 전용 서버에 Runner 구축 (운영용)**
   - 추후 내부 전용 서버가 마련되면 Runner를 이전해 운영 환경으로 전환 예정
   - **장점:**
     - **무중단 이전** - 워크플로우 수정 없이 Runner만 변경
     - **24/7 안정성** - 개인 기기 전원 문제 없음
     - **성능 향상** - 전용 서버 리소스 활용 가능
     - **보안 강화** - 내부망 환경에서 동작

> **CI/CD 파이프라인 배포 플로우**: Git Push → 내부 Runner → 로컬 빌드 → 베스천 전송 → Target EC2 Instance 배포

<br>

## 참고했던 블로그

- [다나와 기술블로그 | Github Actions에 Self-hosted Runner 등록하기](https://danawalab.github.io/common/2022/08/24/Self-Hosted-Runner.html)
- [DevOpsSong | GitHub Actions, Self-hosted runner 구성 가이드](https://nginxstore.com/blog/ci-cd/github-actions-self-hosted-runner-%EA%B5%AC%EC%84%B1-%EA%B0%80%EC%9D%B4%EB%93%9C/)
- [KS피터 | GitHub Actions self hosted runner 설정하기](https://ks-peter.tistory.com/entry/GitHub-Actions-self-hosted-runner-%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0)
