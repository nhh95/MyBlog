# blog 제작프로젝트

---

## 1. 프로젝트 개요

- **프로젝트명**: blog 제작 프로젝트
- **개발 기간**: 2025/08/01 ~ 2025/09/02
- **인력구성: 1**명
- **배포현황**: 배포중
    - **사이트 주소**: https://blog.namdeveloper.com/
    - **GitHub**: https://github.com/nhh95/MyBlog
- **기술 스택**: JAVA,Spring Boot,HTML5,CSS,JavaScript,Mysql,Dokcer



---

## 2. 프로젝트 목적 및 담당역할

### 프로젝트 목적

> 이 프로젝트는 DDD(도메인 주도 설계) 기반의 패키지 구조를 채택하여 개발된 개인 블로그 프로젝트입니다. 사용자 인증 및 권한 관리, 게시글 CRUD, 파일 업로드/관리, 댓글 기능, 그리고 배포 자동화까지 포괄하는 풀스택 웹 애플리케이션입니다.
>

### 기획의도 및 목표

- **실사용을 위한 블로그제작** :  앞으로 계속 발전시키면서 실제로 사용할 블로그를 만드는것을 목표로 했습니다
- **실전 개발 경험 축적:** Spring Boot, Spring Security, JPA 등 핵심 프레임워크 기술을 깊이 이해하고 실제 프로젝트에 적용하는 것을 목표로 했습니다.
- **지속 가능한 코드 구조:** DDD 원칙을 도입하여 도메인별로 책임이 명확한 패키지 구조를 설계하고, 유지보수와 확장성을 고려한 아키텍처를 구축하는 데 집중했습니다.
- **CI/CD 환경 구축:** 개발부터 배포까지의 전 과정을 자동화하여, 효율적인 개발 파이프라인을 구축하고 실제 서비스 배포 경험을 쌓고자 했습니다.

### 

---

## 3. 개발환경 및 기술 스택

| 분류 | 내용 |
| --- | --- |
| 운영체제 | Windows 11, Linux(Ubuntu) |
| 서버 | Nginx,Apache Tomcat |
| 데이터베이스 | MySQL |
| 파일 스토리지 | MiniO(S3 호환 로컬 스토리지) |
| 개발 툴 | IntelliJ IDEA,DBeaver,MySql WorkBench |
| 협업/디자인/문서 | GitHub, Notion, Google Drive |
| 언어 | Java 17, HTML5, CSS, JavaScript, SQL |
| 백엔드 프레임워크 | Spring Boot, Spring Web,  Spring Security, Spring Boot Devtools,Spring Boot Validation, Spring Data JPA |
| 라이브러리 | Summernote |
| 데이터/빌드/유틸 | Gradle, Gson, Lombok,Commons IO |
| 프론트엔드 | Thymeleaf, jQuery, Ajax |
| 인프라 | Docker,Docker Compose,GitHub Actions |

### 새롭게 사용해본 기술

- MiniO
    - AWS S3 API 와 호환되는 로컬 파일스토리지인 MiniO를 사용해서 파일서버를 구축하고 파일 업로드,조회,삭제기능을 구현했습니다
- Summernote
    - 오픈소스 WYSIWYG 에디터인 Summernote 에디터를 사용해서 글 작성기능과 이미지를 첨부하는 기능 개발시간을 단축했습니다

---

## 프로젝트 아키텍쳐

<img width="2873" height="1775" alt="Image" src="https://github.com/user-attachments/assets/9e2e68db-5eb3-4eca-836c-93453984bb5b" />

## ERD

<img width="905" height="565" alt="Image" src="https://github.com/user-attachments/assets/0ad5d0a3-e116-4f0a-91b2-3f83a69f503c" />

## API 명세서

### 회원

| 메소드 | URL | 설명 |
| --- | --- | --- |
| GET | /join | 회원가입 페이지 |
| GET | /login | 로그인 페이지 |
| POST | /user/join | 회원가입 처리 |
| GET | /user/update/{email} | 회원정보 수정 페이지 |
| POST | /user/update/{email} | 회원정보 수정 처리 |

### 게시판

| 메소드 | URL | 설명 |
| --- | --- | --- |
| GET | /post/{categoryName} | 카테고리별 게시글 목록 |
| GET | /post/{categoryName}/createpost | 글 작성 페이지 |
| POST | /post/{categoryName}/createpost | 글 작성 처리 |
| POST | /post/{categoryName}/deletePost/{id} | 글 삭제 처리 |
| GET | /post/{categoryName}/updatepost/{id} | 글 수정 페이지 |
| POST | /post/{categoryName}/updatepost/{id} | 글 수정 처리 |
| POST | /post/{categoryName}/updatepost/{id}/verify | 비회원 글 수정 비밀번호 확인 |
| GET | /post/{categoryName}/{id} | 특정 게시글 조회 |
| GET | /post/{categoryName}/{id}/delete-verify | 비회원 글 삭제 비밀번호 확인 페이지 |
| POST | /post/{categoryName}/{id}/delete-verify | 비회원 글 삭제 처리 |

### 댓글

| 메소드 | URL | 설명 |
| --- | --- | --- |
| POST | /api/comments | 댓글 작성 |
| GET | /api/comments/count/{postId} | 댓글 개수 조회 |
| DELETE | /api/comments/guest/{commentId} | 비회원 댓글 삭제 |
| GET | /api/comments/post/{postId} | 특정 게시글의 댓글 조회 |
| PUT | /api/comments/{commentId} | 댓글 수정 |
| DELETE | /api/comments/{commentId} | 댓글 삭제 (회원) |

### 이미지 파일

| 메소드 | URL | 설명 |
| --- | --- | --- |
| POST | /uploadSummernoteImageFile | Summernote 에디터 이미지 업로드 |
| POST | /deleteSummernoteImageFile | Summernote 에디터 이미지 삭제 |

### 공통

| 메소드 | URL | 설명 |
| --- | --- | --- |
| GET | / | 메인 페이지 |
|  | /error | 전역 에러 처리 페이지 |

---

## 4. 주요 기능

### 회원기능

- 세션 기반 회원가입
- 회원의 비밀번호,닉네임 수정기능

### 게시판기능

- 회원게시판, 비회원게시판 분리
- 목록조회,CRUD 기능
- 게시글 세션기반 조회수 중복 방지
- 글 작성시 이미지 첨부 기능

### 댓글기능

- AJAX를 사용한 회원/비회원 댓글 CRUD

---

## 5.구현

### **회원**

- **UserService**는 회원가입 시 중복 이메일을 검사하고, 비밀번호를 BCrypt로 암호화하여 저장합니다
- Spring Security의 **UserDetailsService**를 구현해 로그인 시 사용자 정보를 로드하고 역할(ROLE_USER, ROLE_ADMIN)을 부여합니다
- **UserController**는 **/join**, **/login**, **/user/update/{email}** 경로에 대한 가입·로그인·정보수정 페이지를 제공하며, 수정 시 권한 검사를 통해 본인 또는 관리자만 접근 가능하게 합니다
- **SecurityConfig**는 CSRF 토큰을 사용하고, 비밀번호 인코더 및 역할 계층(RoleHierarchy)을 등록하여 관리자→회원 순 권한 상속을 구성합니다

### **게시판**

- 게시글은 **PostEntity**가 사용자·비회원·카테고리와의 연관관계를 갖고 있으며, 댓글과 조회수 필드를 포함합니다
- **PostController**는 카테고리 기반 URL(**/post/{categoryName}**)을 사용해 목록·상세·작성·수정·삭제를 제공하며, 권한에 따라 글 작성 가능 여부를 제한합니다
- **PostService**는 카테고리별 조회, 비회원·회원 구분 게시글 작성, 조회수 중복 방지(**ViewedPostsHolder**) 등을 처리합니다
- **PostRepository**는 게시글별 댓글 수를 조회하는 커스텀 쿼리를 제공하여 목록 조회 시 댓글 개수를 함께 보여줍니다

### **글작성**

- 글 작성 시 Summernote 에디터에서 업로드된 이미지를 임시 폴더에서 MinIO로 이동시키며, 게시글 본문의 **<img>** URL을 MinIO 경로로 교체합니다
- 비회원 글은 닉네임·비밀번호를 별도의 **GuestUserEntity**에 저장하고 게시글과 연관지어 비밀번호 확인 후 수정·삭제가 가능하도록합니다

### **댓글기능**

- **CommentService**는 회원·비회원 댓글을 모두 지원하며, 비회원 댓글은 닉네임·비밀번호를 저장 후 수정·삭제 시 비밀번호 검증을 수행합니다
- 댓글 CRUD와 댓글 개수 조회를 제공합니다

### **이미지처리**

- **FileController**는 Summernote 이미지 업로드를 임시 디렉터리에 저장하고,글 작성중  삭제 요청 시 임시 파일을 제거합니다
- **MinioService**는 MinIO 버킷에 이미지를 업로드·삭제하며 URL을 반환합니다
- **FileService**의 스케줄러는 24시간 이상 지난 임시 이미지 파일을 매일 03시에 정리합니다

---

## 6.배포 및 인프라구축

- **CI/CD**
    - 개발자가  ‘Main’ 브랜치에 푸시
    - GitHub Action Self-Hosted Runner 에서 작동
    - GitHub에 올려진 코드로 그래들 빌드
    - 그래들 빌드된 JAR를 도커 이미지로 빌드후 도커 허브에 푸시
    - SSH로 서버에서 도커 허브 이미지 풀
    - GitHub Secrets 에 있는 application.yml,docker-compose.yml을 서버에서 생성
    - 도커 컴포즈 실행
- **보안 관리**: **application.yml** 파일의 민감한 정보(DB 패스워드 등)는 **GitHub Actions Secrets**로 관리하여 보안을 강화했습니다. 애플리케이션 그래들 빌드할때와 도커 이미지,도커 허브에 보안사항이 포함되지 않도록 애플리케이션 실행시에 외부참조방식으로 진행했습니다
- **Nginx**를 **리버스 프록시**로 설정하여 HTTP/HTTPS 요청을 애플리케이션 컨테이너로 전달하며, 서비스는 **서브도메인**으로 구분하여 운영합니다.
- **Let's Encrypt**의 **Certbot**을 이용해 SSL/TLS 인증서 자동 발급 및 갱신합니다

---

## 7.문제 해결

- 사용자가 글 작성중에 이미지를 첨부하는 순간 바로 이미지 파일서버에 저장되는 부분
    - 사용자가 글작성중에 이미지를 첨부하면 바로 이미지 파일서버로 전송되서 저장되어 버리는 부분을 개선하는 방안이 필요했습니다
    - 사용자가 글작성중에 첨부한 이미지는 애플리케이션 에서 먼저 임시파일로 저장하고 사용자가 글 작성을 완료했을때 이미지가 이미지 파일서버로 전송되도록 구조 를 변경하고 애플리케이션에 있는 임시파일은 03시마다 지워지게 했습니다

- 조회수가 글을 조회할때마다 증가하는 부분
    - 같은 사용자가 글 조회시 한번만 증가해야하는데 글을 조회할때마다 계속 증가하는 로직을 개선하고싶었습니다
    - 세션에 ViewPostHodler 라는 인스턴스를 생성하고 인스턴스에 사용자가 조회한 게시물의 id를 저장하고 PostService에서 사용자가 조회한 게시물이 인스턴스에 없는경우 조회수를 증가시켜 DB에 저장하도록 했습니다

- 1MB 넘는 이미지 저장과 조회 불가
    - 1MB 미만 이미지는 저장과 조회가 가능하나 1MB가 초과하는 순간 저장할수없는 문제가 생겼습니다
    - Nginx 설정에서 Body 사이즈가 기본값이 1MB이기때문에 HTTP 요청 본문의 크기가 1MB를 넘어가면 전송이 안되는 문제였습니다
    - Nginx 설정에 client_max_body_size를 입력후 문제 해결했습니다

- 도커 컨테이너 DNS 해석 문제
    - 전날까지 잘 작동하던 애플리케이션의 이미지 조회와 이미지 첨부 기능에 문제가 생겼습니다
    - 개발환경에서 로컬서버 구동시에는 이미지 조회와 이미지 첨부기능이 정상작동하는것을 확인했습니다
    - 도커 컨테이너 에서 네트워크환경 점검을 해보니 이미지파일서버의 주소를 해석을 못하는것을 확인했습니다
    - 다른 컴퓨터에서 도메인과 DDNS 주소 점검시 정상임을 확인했습니다
    - 도커 컴포즈 실행시에 dns 지정후 실행해보니 주소 해석 실패했습니다
    - 도커 컨테이너 네트워크를 호스트네트워크로 변경후 실행해보니 실패했습니다
    - 서버 네트워크에 reslov.conf 에 dns추가후 실행 해보니 실패했습니다
    - 문득 도커 파일 생성시에 경량화 기반인openjdk:17-jdk-alpine 이미지로 빌드 한다는것이 생각났습니다
    - 찾아보니 Alpine은 musl libc 라는 데비안 계열과는 다른 라이브러리를 사용하는데 구 버젼의 경우 DNS TCP Fallback 문제가 있는것을 알았습니다
    - 데비안 계열 openjdk:17-jdk-slim 으로 도커 이미지 빌드후 문제 해결 완료했습니다

---

### **개선 사항**

- **초기 설계 보강**: 개발 과정에서의 리팩토링을 줄이기 위해 다음 프로젝트에서는 초기 설계에 더 많은 시간을 투자할 계획입니다.

---

## 8. 스크린샷

- 메인화면

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/f4f2311d-0ae3-4c86-8dd5-96fa2678ff00" />

- 프로젝트 게시판
  
  <img width="2295" height="1937" alt="Image" src="https://github.com/user-attachments/assets/8c41d03b-9baa-4f59-97fc-8abb0201f5f9" />
  
- 로그인 화면

    <img width="1240" height="1270" alt="Image" src="https://github.com/user-attachments/assets/ffa5dcd8-2d4a-407f-822b-0b4a503867f9" />


- 회원가입 화면

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/fded2b6f-8fa3-4e96-91ef-8d9d46438216" />


- 회원 자유게시판

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/a142ea13-ec6c-4f24-a71c-3c084a36966c" />


- 회원 글 작성

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/b4d84d54-e05a-42af-a500-402a80906db0" />


- 회원 글 수정

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/dbf09016-8802-4bbb-96eb-94faf0f76e2b" />


- 회원 글 보기

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/d22650f0-a9b6-4573-b14c-7aa9993985a2" />


- 회원 댓글 수정

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/24e7d484-f913-4a2e-bcb5-eefa92fdd4f4" />


- 비회원 자유게시판

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/739ed386-329c-4151-8449-7fce5dfbd477" />


- 비회원 글 작성

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/141d23b7-f2d2-4378-8fc5-5e9754a8a773" />


- 비회원 글 수정시 비밀번호 검증화면

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/4b833f6e-f831-41dd-988b-8b1dcc16644b" />


- 비회원 글 수정

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/b58d206a-06d6-4a5e-87e7-d33c69d3ce96" />


- 비회원 글 삭제 검증 화면

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/de04f313-b548-4b0d-81c1-1e7a6310dfca" />


- 비회원 글보기

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/64c81ace-9cb3-474a-8282-4c71413c4167" />

- 비회원 댓글작성

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/8ca539bc-9d1e-40d7-b459-82db9a32fc90" />


- 비회원 댓글 수정

    <img width="2082" height="1319" alt="Image" src="https://github.com/user-attachments/assets/28975b86-9f5b-4ce4-b8b3-bbfc3a7f7a96" />



---
