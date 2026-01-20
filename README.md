# Stock Radar
목적: 컴퓨터 부품 실시간 재고 · 최저가 비교 및 알림 서비스

기간: 2025.02.20 ~ 2025.03.25

인원: 백엔드 & 프론트 1인 (본인), 백엔드 2인 / 총 3인

역할: API 설계, DB 모델링, 인증 구현

---

## 프로젝트 개요

**Stock Radar**는 여러 웹 사이트에 분산된 컴퓨터 부품의 가격 및 재고 정보를 수집·비교하고,  
사용자가 설정한 조건에 따라 **실시간 알림을 제공하는 서비스**입니다.

가격 변동과 재고 소진이 잦은 컴퓨터 부품 특성상,
사용자가 직접 반복 확인해야 하는 문제를 해결하는 것을 목표로 하였습니다.

본 프로젝트는 팀 프로젝트로 진행되었으며,  
**본인은 백엔드 개발을 중심으로 실시간 처리 및 알림 시스템 구현과 전반적인 프론트앤드 개발을 담당**하였습니다.

---

## My Role & Contribution

### Backend (주 담당)

- Spring Boot 기반 백엔드 아키텍처 설계
- JPA를 활용한 도메인 모델링 및 데이터베이스 설계
- 컴퓨터 부품 **최저가 비교 로직 구현**
- 쇼핑몰 크롤링 데이터를 저장하기 위한 **데이터 정규화 구조 설계**
- **Kafka 기반 실시간 알림 시스템 설계 및 구현**
- 알림 채널 선택 기능 구현 (SMS / Email / Discord DM)
- 이메일 발송 성능 개선을 위한 **비동기 처리 구조 설계**
- REST API 설계 및 구현

### Frontend (부분 참여)

- 알림 설정 UI 연동
- Axios 기반 API 연동

---

## 주요 기능 및 구현 내용

### 실시간 재고 및 가격 수집
- 쇼핑몰 크롤링을 통해 가격·재고 데이터 수집
- 주기적 수집 후 기존 데이터와 비교하여 변경 사항 반영

### 최저가 자동 비교
- 판매처별 가격 데이터를 정규화
- 동일 부품 기준으로 최저가 자동 계산

### 실시간 알림 시스템
- Kafka를 활용하여 가격 변동 이벤트 처리
- 사용자가 설정한 조건 충족 시 알림 발송
- 알림 채널:
  - SMS
  - Email
  - Discord DM
- 사용자별 알림 채널 선택 가능

### 커뮤니티 기능
- 게시글 CRUD API 구현
- 페이징 처리

---

## 기술 스택

### Backend
- Spring Boot
- JPA (Hibernate)
- Kafka

### Frontend
- HTML5
- CSS3
- JavaScript
- Axios
- Bootstrap
- Thymeleaf

### Data
- 쇼핑몰 크롤링 데이터
- 실시간 이벤트 기반 처리

### Infra / Etc
- Docker
- Git / GitHub
- Gradle

---

## 트러블슈팅 & 배운 점

- 쇼핑몰별로 상이한 크롤링 데이터 구조  
  → 공통 도메인 기준으로 데이터 정규화 구조 설계
- 실시간 알림 처리 시 동기 방식의 한계 경험  
  → Kafka를 도입하여 이벤트 기반 비동기 처리로 개선
- 이메일 발송 속도 문제  
  → 외부 유료 서비스 없이 내부 비동기 구조로 성능 개선
- 알림 채널 확장을 고려한 구조 설계의 중요성 인지

---

## 데모 및 스크린샷

<img src="https://github.com/user-attachments/assets/c5310b5d-d00f-4436-aff4-7df29f318394" width="300" height="700"/>

---

## 프로젝트 정보

- 프로젝트 형태: 팀 프로젝트
- 담당 영역: **백엔드 중심 (실시간 처리 및 알림 시스템)**

---

## Contact

고병현  
- Email: rhqudgus99@gmail.com  
- GitHub: https://github.com/Hyun7en

---
⚠️ This project does NOT collect data from commercial marketplaces
such as Amazon, 11st, or SSG due to their Terms of Service.

All crawling targets were selected based on robots.txt
and public accessibility.

