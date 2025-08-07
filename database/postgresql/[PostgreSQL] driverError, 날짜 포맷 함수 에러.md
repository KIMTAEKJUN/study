쿼리문을 통해 사용자 통계, 예약 통계 등을 가져오는 기능을 만들었는데 아래와 같은 에러가 떴다.

```
driverError: error: column "%Y-%m" does not exist
```

## 문제의 코드

```typescript
this.reservationRepository
        .createQueryBuilder('reservation')
        .select('DATE_FORMAT(reservation.createdAt, "%Y-%m")', 'month')
        .addSelect('SUM(estimatedPrice)', 'totalRevenue')
        .addSelect('COUNT(*)', 'count')
        .where('reservation.status = :status', { status: ReservationStatus.COMPLETED })
        .groupBy('month')
        .orderBy('month', 'DESC')
        .limit(12)
        .getRawMany(),
```

현재 TypeORM + PostgreSQL를 사용하고 있는데 알고보니 PostgreSQL에서 사용하는 날짜 포맷 함수가 달라서 에러가 뜬거였다. 다음엔 쿼리문과 함수 등을 제대로 공부 후 이해한 다음 사용해야겠다.

<br>

## 수정한 코드

```typescript
this.reservationRepository
        .createQueryBuilder('reservation')
        .select("to_char(reservation.createdAt, 'YYYY-MM')", 'month')
        .addSelect('SUM(reservation.estimatedPrice)', 'totalRevenue')
        .addSelect('COUNT(*)', 'count')
        .where('reservation.status = :status', { status: ReservationStatus.COMPLETED })
        .groupBy('month')
        .orderBy('month', 'DESC')
        .limit(12)
        .getRawMany(),
```

DATE_FORMAT -> to_char로 수정 후 에러가 잘 고쳐졌다!
