현재 프로젝트를 진행하면서, 관리자가 주요 지표를 한눈에 확인할 수 있는 통계 기능을 개발하게 되었습니다. 처음에는 쿼리에 대한 이해가 부족했기 때문에, 작성한 쿼리를 하나씩 분석하며 동작 원리를 파악하고자 합니다.

## 1. 어드민 통계 서비스 구현

세 가지 주요 통계를 비동기로 병렬 실행하여 응답 속도를 향상시키는 서비스입니다. `Promise.all`을 사용해 예약 통계, 매출 통계, 사용자 통계를 동시에 조회하며, 결과는 가공되어 반환됩니다.

```tsx
// src/admin/statistic/admin-statistic.service.ts
@Injectable()
export class AdminStatisticService {
  constructor(
    @InjectRepository(User)
    private readonly userRepository: Repository<User>,
    @InjectRepository(Reservation)
    private readonly reservationRepository: Repository<Reservation>
  ) {}

  async getStatistics() {
    const [reservationStats, revenueStats, userStats] = await Promise.all([
      // 예약 통계
      this.reservationRepository
        .createQueryBuilder("reservation")
        .select("reservation.status", "status")
        .addSelect("COUNT(*)", "count")
        .groupBy("reservation.status")
        .getRawMany(),

      // 매출 통계
      this.reservationRepository
        .createQueryBuilder("reservation")
        .select("to_char(reservation.createdAt, 'YYYY-MM')", "month")
        .addSelect("SUM(reservation.finalPrice)", "totalRevenue")
        .addSelect("COUNT(*)", "count")
        .where("reservation.status = :status", {
          status: ReservationStatus.COMPLETED,
        })
        .groupBy("month")
        .orderBy("month", "DESC")
        .limit(12)
        .getRawMany(),

      // 사용자 통계
      this.userRepository
        .createQueryBuilder("user")
        .select("COUNT(*)", "totalUsers")
        .addSelect(
          (qb) =>
            qb
              .select("COUNT(*)")
              .from(User, "u")
              .where("u.createdAt >= :date", {
                date: new Date(new Date().setMonth(new Date().getMonth() - 1)),
              }),
          "newUsers"
        )
        .getRawOne(),
    ]);

    return {
      reservations: {
        byStatus: reservationStats,
        total: reservationStats.reduce(
          (sum, stat) => sum + Number(stat.count),
          0
        ),
      },
      revenue: {
        monthly: revenueStats,
        total: revenueStats.reduce(
          (sum, stat) => sum + Number(stat.totalRevenue),
          0
        ),
      },
      users: {
        total: Number(userStats.totalUsers),
        newUsers: Number(userStats.newUsers),
      },
    };
  }
}
```

<br>

## 2. 예약 상태별 통계 쿼리

예약이 현재 어떤 상태인지를 파악하기 위한 통계입니다. 각 상태별 예약 건수를 집계합니다.

```tsx
this.reservationRepository
  .createQueryBuilder('reservation')
  .select('reservation.status', 'status')
  .addSelect('COUNT(*)', 'count')
  .groupBy('reservation.status')
  .getRawMany(),
```

### 2-1. 쿼리 분석

1. **createQueryBuilder('reservation')**
   - reservation 테이블에 대한 쿼리 빌더 생성
2. **select('reservation.status', 'status')**
   - 예약 상태 컬럼을 조회하고, 결과 컬럼명을 status로 지정
3. **addSelect('COUNT(\*)', 'count')**
   - 각 상태별 예약 건수를 count로 계산
4. **groupBy('reservation.status')**
   - 상태별로 그룹화하여 각각 몇 건인지 집계
5. **getRawMany()**
   - 쿼리 결과를 가공되지 않은 원시 데이터 배열 형태로 반환

<br>

## 3. **월별 매출 통계 쿼리**

완료된 예약을 기준으로, 매월 매출 합계와 예약 건수를 가져오는 쿼리입니다. PostgreSQL의 to_char 함수를 활용해 날짜를 월 단위 문자열로 변환하여 집계합니다. 추후엔 월간, 연간으로 나누려고 생각 중에 있습니다.

```tsx
this.reservationRepository
  .createQueryBuilder('reservation')
  .select("to_char(reservation.createdAt, 'YYYY-MM')", 'month')
  .addSelect('SUM(reservation.finalPrice)', 'totalRevenue')
  .addSelect('COUNT(*)', 'count')
  .where('reservation.status = :status', { status: ReservationStatus.COMPLETED })
  .groupBy('month')
  .orderBy('month', 'DESC')
  .limit(12)
  .getRawMany(),
```

### 3-1. **쿼리 분석**

1. **to_char(reservation.createdAt, 'YYYY-MM')**
   - 예약 생성일을 YYYY-MM 형식으로 변환해 월 단위로 그룹화
2. **addSelect('SUM(reservation.finalPrice)', 'totalRevenue')**
   - 월별로 완료된 예약의 매출 합계를 계산
3. **addSelect('COUNT(\*)', 'count')**
   - 완료된 예약의 건수를 계산
4. **where('reservation.status = :status')**
   - 완료 상태의 예약만 통계 대상에 포함
5. **groupBy('month')**
   - 월 단위로 결과 그룹화
6. **orderBy('month', 'DESC')**
   - 최신 월부터 내림차순으로 정렬
7. **limit(12)**
   - 최근 12개월까지만 데이터 조회

<br>

## 4. **사용자 통계 쿼리**

전체 사용자 수와 최근 1개월 내에 가입한 사용자 수를 한 번에 가져오는 쿼리입니다. addSelect에 서브쿼리를 활용합니다.

```tsx
this.userRepository
  .createQueryBuilder('user')
  .select('COUNT(*)', 'totalUsers')
  .addSelect(
    (qb) =>
      qb
        .select('COUNT(*)')
        .from(User, 'u')
        .where('u.createdAt >= :date', {
          date: new Date(new Date().setMonth(new Date().getMonth() - 1)),
        }),
    'newUsers',
  )
  .getRawOne(),
```

### 4-1. **쿼리 분석**

1. **select('COUNT(\*)', 'totalUsers')**
   - 전체 사용자 수를 totalUsers로 조회
2. **addSelect(서브쿼리, 'newUsers')**
   - 최근 1개월 이내에 가입한 사용자 수를 newUsers로 조회
   - 서브쿼리 내부에서 createdAt 기준 필터링
3. **서브쿼리 내의 where 조건**
   - createdAt >= :date 조건을 통해, 현재 시점 기준으로 한 달 이내에 가입한 사용자만 필터링하여 계산
   - 날짜는 현재 날짜에서 한 달 전으로 계산
4. **getRawOne()**
   - 단일 객체를 원시 형태로 반환

<br>

## 5. 결과 데이터 가공

쿼리에서 반환된 문자열 형태의 숫자 데이터를 숫자로 변환하고, reduce 메서드를 활용해 총합을 계산합니다.

```tsx
return {
  reservations: {
    byStatus: reservationStats,
    total: reservationStats.reduce((sum, stat) => sum + Number(stat.count), 0),
  },
  revenue: {
    monthly: revenueStats,
    total: revenueStats.reduce(
      (sum, stat) => sum + Number(stat.totalRevenue),
      0
    ),
  },
  users: {
    total: Number(userStats.totalUsers),
    newUsers: Number(userStats.newUsers),
  },
};
```

<br>

## 6. 마무리

이번 글에서는 어드민이 확인할 수 있는 통계 기능을 어떻게 구현했는지 정리해보았습니다. 처음에 쿼리 작성이 익숙하지 않아 이해하기 어려웠지만, 하나하나 분석하며 동작 원리를 이해하고 개선할 수 있었습니다.
이 글이 비슷한 기능을 구현하려는 분들께 조금이나마 도움이 되었기를 바랍니다 🙌
끝까지 읽어주셔서 감사합니다 😊
