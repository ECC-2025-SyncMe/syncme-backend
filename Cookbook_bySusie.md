./mvnw spring-boot:run

./mvnw clean package -DskipTests && aws s3 cp target/syncme-0.0.1-SNAPSHOT-aws.jar s3://syncme-lambda-deploy/ --region ap-northeast-2 && aws lambda update-function-code --function-name syncme-backend --s3-bucket syncme-lambda-deploy --s3-key syncme-0.0.1-SNAPSHOT-aws.jar --region ap-northeast-2 --no-cli-pager

[작업 순서]
1. 아래 조건에 맞게 '내가 맡은 부분'에 해당하는 부분만 전체 구현
2. 전체 엔드포인트 test 코드 구현
3. test 코드 로컬에서 실행되는지 확인, 넣지 않은 코드 없는지 확인
4. lambda 함수 작성
5. 배포하는 과정 md 파일 작성
6. lambda 배포, dynamo db 연결, api gateway로 연결, 커맨드창에서 확인
7. iam user 만들고 access key 발급 후 github actions에서 cicd 연결
8. 커맨드창에서 test 코드 웹 위에서 돌아가는지 확인
9. 구현해둔 코드에 맞춰서 api 명세서, erd 작성
10. 트러블 슈팅, 확장 가능성 탐구

[기획]
오늘 사용자 상태를 입력하면 캐릭터가 업데이트 되는 일기? 서비스
- 내용적으로: 게임 같은 느낌을 주고 싶고 귀엽게…
- 기능적으로 넣고 싶은 거: 자체 로그인 없이 구글 로그인만, 서버리스, nosql, CICD
- 와이어프레임
    기본을 다크 모드로, 포인트색 파란색, 서브색 회색
    
    1. 로그인 페이지: 로고가 정가운데 있음, 구글로 시작 버튼 있음 
    
    + 로딩: 로딩 시 주변이 회색에 로고가 움직이게, 오버레이 로딩 (페이지 아님)
    
    2. 홈 페이지: 닉네임, 상태3가지(에너지, 부담감, 열정), 오늘의 한 마디 랜덤으로 나오게, 중앙에 캐릭터가 서있는데 이때 캐릭터 총 상태 계산해서 100점 만점에 0~30점이면 지친이미지가 나오고, 31~70점이면 보통 이미지, 71~100점이면 활동적 이미지 나오게 함, 날짜, 아직 기입 안 한 날이면 기록 입력하라는 알람
    
    3. 업데이트 페이지: 상태 고를 수 있음 3가지(에너지, 부담감, 열정), 날짜는 자동 계산되고 ...날짜로 '기록' 버튼 누르면 기록되고 업데이트됨, 옆에 캐릭터도 같이 뜨기 때문에 이 캐릭터 100점 만점 계산한 것도 받아와야함
    
    4. 설정 페이지: 로그인된 이메일, 닉네임 설정, 로그아웃, 데이터 초기화, 계정 삭제, 이 웹에 관하여 이렇게 있고 전부 모달로 처리함 누르면 보이도록
    
    ![image.png](attachment:79b1ba66-75b1-4fe8-b3d7-646ed442fb35:image.png)
    
- api 엔드포인트
    - Auth / 인증 관련 (Google 로그인 기반)
        POST   /auth/google/login        // 구글 로그인 → JWT 발급
        POST   /auth/logout              // 로그아웃 (클라이언트용)
        GET    /auth/me                  // 현재 로그인 사용자 확인
    - User / 계정 정보
        GET    /users/me                 // 내 계정 정보 조회 (이메일, 닉네임 등)
        PATCH  /users/me/nickname        // 닉네임 설정/변경
        DELETE /users/me                 // 계정 삭제
    - Status / 오늘 상태 (핵심 도메인)
        GET    /status/today             // 오늘 상태 조회
        POST   /status/today             // 오늘 상태 기록 (업데이트)
        PUT    /status/today             // 오늘 상태 수정
        GET    /status/check             // 오늘 기록 여부 확인
    - Status History / 기록 히스토리
        GET    /status/history           // 전체 기록 리스트
        GET    /status/history/{date}    // 특정 날짜 기록 조회
        DELETE /status/history           // 모든 기록 초기화
    - Character / 캐릭터 상태 계산 결과
        GET    /character/current        // 현재 캐릭터 상태 (지침/보통/활력)
        GET    /character/score          // 내부 계산 점수 (100점 기준)
        GET    /character/summary        // 캐릭터 요약 문장
    - Calculation / 상태 계산 엔진 (논리 분리용)
        POST   /calculate/status         // 상태 입력 → 계산 결과 반환
        GET    /calculate/preview        // (옵션) 입력값 기반 미리보기
    - UI Content / 랜덤 문구 & 시스템 콘텐츠
        GET    /content/today-message    // 오늘의 한 마디 랜덤
        GET    /content/loading-message  // 로딩 중 문구
        GET    /content/about            // "이 웹에 관하여"
    - Settings / 설정 (모달용)
        GET    /settings                 // 설정 정보 전체
        DELETE /settings/data            // 데이터 초기화

추가 엔드포인트
    - User / 계정 정보
    GET /users/search?query={검색어}&type={검색타입}
- Friends / 친구
    POST   /friends/{userId}          // 팔로우 요청, userId = "u_a1b2c3d4e5f6"
    DELETE /friends/{userId}          // 언팔로우
    GET    /friends/following         // 내가 팔로우한 사람들
    GET    /friends/followers         // 나를 팔로우한 사람들
    GET    /friends                  // 친구 목록 (서로 팔로우)
    POST   /friends/{userId}/comments // 친구에게 댓글 작성 (양쪽 팔로우 필수)
    GET    /friends/{userId}/comments // 친구가 받은 댓글 조회
- Comments / 댓글
    GET    /comments/received         // 내가 받은 댓글 조회
    DELETE /comments/{commentId}      // 내가 작성한 댓글 삭제
- Home / 공개 마이홈 (공유)
    GET /home/{userId}
    GET /home/me/share-link

[조건]
- 아래 중 '내가 담당한 부분'만 구현하기
- 내가 담당한 부분: Auth, User, UI content, Settings, Friends, Home
- 내가 맡지 않은 부분: Status, Status History, Character, Calculation
