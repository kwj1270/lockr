# language: ko
기능: 홈 카드 관리
  사용자가 홈 화면에 표시할 클럽 카드를 관리할 수 있다.

  배경:
    먼저 "user-001" 사용자가 존재한다
    그리고 "user-001"은 "FC서울" 클럽의 멤버이다
    그리고 "user-001"은 "FC부산" 클럽의 멤버이다
    그리고 "user-001"은 "FC대전" 클럽의 멤버이다

  시나리오: 핀된 클럽이 없으면 빈 배열 반환
    먼저 "user-001"은 핀된 클럽이 없다
    만약 "user-001"이 홈 카드 목록을 조회한다
    그러면 빈 배열이 반환된다

  시나리오: 사용자가 클럽을 핀 설정한다
    만약 "user-001"이 clubs 배열로 핀 설정한다:
      | clubId  | backgroundColor |
      | FC서울   | #FF5733         |
    그러면 핀 설정이 성공한다
    그리고 홈 카드에 "FC서울"이 backgroundColor "#FF5733"과 함께 표시된다

  시나리오: 최대 2개 클럽만 핀 설정 가능
    만약 "user-001"이 clubs 배열로 3개 클럽을 핀 설정하려고 한다:
      | clubId  | backgroundColor |
      | FC서울   | #FF5733         |
      | FC부산   | #33FF57         |
      | FC대전   | #3357FF         |
    그러면 핀 설정이 실패한다

  시나리오: 가입하지 않은 클럽은 핀 설정 불가
    먼저 "user-001"은 "FC광주" 클럽의 멤버가 아니다
    만약 "user-001"이 clubs 배열로 핀 설정한다:
      | clubId  | backgroundColor |
      | FC광주   | #FF5733         |
    그러면 핀 설정이 실패한다

  시나리오: 홈 카드 조회 시 카드 정보가 포함된다
    먼저 "user-001"이 clubs 배열로 핀 설정했다:
      | clubId  | backgroundColor |
      | FC서울   | #FF5733         |
    그리고 "FC서울" 클럽에 다음 주 훈련 일정이 있다
    만약 "user-001"이 홈 카드 목록을 조회한다
    그러면 "FC서울" 카드에 다음 정보가 포함된다:
      | 필드              | 설명                    |
      | backgroundColor  | 사용자 지정 배경색       |
      | memberCount      | 클럽 멤버 수            |
      | nextScheduleDate | 다음 일정 날짜          |
      | location         | 클럽 활동 지역          |

  시나리오: 핀 설정을 변경하면 기존 핀이 교체된다
    먼저 "user-001"이 "FC서울"을 핀 설정했다
    만약 "user-001"이 clubs 배열로 핀 설정한다:
      | clubId  | backgroundColor |
      | FC부산   | #33FF57         |
    그러면 홈 카드에 "FC부산"만 표시된다
    그리고 "FC서울"은 더 이상 표시되지 않는다
