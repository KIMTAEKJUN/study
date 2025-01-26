> 얄코님의 **"갖고노는 MySQL 데이터베이스 by 얄코"** 인프런 강의를 수강한 후 쓴 글입니다. 
문제가 될 시 삭제하겠습니다.

## SELECT 전반 기능 훑어보기
### 1. 테이블의 모든 내용 보기

***(asterisk)**는 테이블의 모든 컬럼을 뜻한다.

```sql
SELECT * FROM Customers;
-- 이와 같이 주석을 달 수 있다. 
```
<br><br>

### 2. 원하는 column(열)만 골라서 보기

```sql
SELECT CustomerName FROM Customers;
```

```sql
SELECT CustomerName, ContactName, Country
FROM Customers;
```

****💡 테이블의 컬럼이 아닌 값도 선택할 수 있습니다.****

```sql
SELECT
	CustomerName, 1, 'Hello', NULL
FROM Customers;
```
<br><br>

### 3. 원하는 조건의 row(행)만 걸러서 보기

**WHERE** 구문 뒤에 조건을 붙여 원하는 데이터만 가져올 수 있다.

```sql
SELECT * FROM Orders
WHERE EmployeeID = 3;
```

```sql
SELECT * FROM OrderDetails 
WHERE Quantity < 5;
```
<br><br>

### 4. 원하는 순서로 데이터 가져오기

**ORDER BY** 구문을 사용해서 특정 컬럼을 기준으로 데이터를 정렬할 수 있다.
**ASC(오름차순)**이 기본이고, **DESC(내림차순)**는 뒤에 써줘야한다.
```sql
SELECT * FROM Customers
ORDER BY ContactName;
```

```sql
SELECT * FROM OrderDetails
ORDER BY ProductID, Quantity DESC;
```
<br><br>

### 5. 원하는 만큼만 데이터 가져오기

**LIMIT {가져올 갯수}** 또는 **LIMIT {건너뛸 갯수}, {가져올 갯수}** 를 사용하여, 원하는 위치에서 원하는 만큼만 데이터를 가져올 수 있다.

```sql
SELECT * FROM Customers
LIMIT 10;
```

```sql
SELECT * FROM Customers
LIMIT 0, 10;
```

```sql
SELECT * FROM Customers
LIMIT 30, 10;
```
<br><br>

### 6. 원하는 별명(alias)으로 데이터 가져오기

**AS**를 사용해서 컬럼명을 변경할 수 있다.

```sql
SELECT
  CustomerId AS ID,
  CustomerName AS NAME,
  Address AS ADDR
FROM Customers;
```

```sql
SELECT
  CustomerId AS '아이디',
  CustomerName AS '고객명',
  Address AS '주소'
FROM Customers;
```
<br><br>

### 7. 모두 활용해보기❗️
```sql
SELECT
  CustomerID AS '아이디',
  CustomerName AS '고객명',
  City AS '도시',
  Country AS '국가'
FROM Customers
WHERE
  City = 'London' OR Country = 'Mexico'
ORDER BY CustomerName
LIMIT 0, 5;
```
<br><br>

## 각종 연산자들
### 1. 사칙연산

#### **+**, **-**, *****, **/**, **%**, **MOD** 

의미는 각각 더하기, 빼기, 곱하기, 나누기, 나머지가 있다.

```sql
SELECT 1 + 2;
```

```sql
SELECT 5 - 2.5 AS DIFFERENCS;
```

```sql
SELECT 3 * (2 + 4) / 2, 'Hello';
-- SELECT 3 * (2 + 4) / 2 AS Number, 'Hello' AS Text;
```

```sql
SELECT 10 % 3;
```
<br><br>

**❗ 문자열에 사칙연산을 가하면 0으로 인식**

```sql
SELECT 'ABC + 3;
```

```sql
SELECT 'ABC' * 3;
```

```sql
SELECT '1' + '002' * 3;
-- 숫자로 구성된 문자열은 숫자로 자동인식
```

```sql
SELECT
	OrderID, ProductID,
	OrderID + ProductID AS SUM
FROM OrderDetails;
```

```sql
SELECT
	ProductName,
	Price / 2 AS HalfPrice
FROM Products;
```
<br><br>

### 2. 참/거짓 관련 연산자

#### TRUE, FALSE

```sql
SELECT TRUE, FALSE;
```

```sql
SELECT !TRUE, NOT 1, !FALSE, NOT FALSE;
```
<br><br>

💡 **MySQL에서는 TRUE는 1, FALSE는 0으로 저장된다.**

```sql
SELECT 0 = TRUE, 1 = TRUE, 0 = FALSE, 1 = FALSE;
```

```sql
SELECT * FROM Customers WHERE TRUE;
```

```sql
SELECT * FROM Customers WHERE FALSE;
```
<br><br>

#### **IS**, **IS NOT**

의미는 각각 양쪽이 모두 TRUE 또는 FALSE, 한쪽은 TRUE, 한쪽은 FALSE

```sql
SELECT TRUE IS TRUE;
```

```sql
SELECT TRUE IS NOT FALSE;
```

```sql
SELECT (TRUE IS FALSE) IS NOT TRUE;
```
<br><br>

#### **AND**, **&&** / **OR**, **||**

의미는 각각 양쪽이 모두 TRUE일 때만 TRUE, 한쪽은 TRUE면 TRUE

```sql
SELECT TRUE AND FALSE, TRUE OR FALSE;
```

```sql
SELECT 2 + 3 = 6 OR 2 * 3 = 6;
```

```sql
SELECT * FROM Orders
WHERE
	CustomerId = 15 AND EmployeeId = 4;
```

```sql
SELECT * FROM Products
WHERE
	ProductName = 'Tofu' OR CategoryId = 8;
```

```sql
SELECT * FROM OrderDetails
WHERE
	ProductId = 20
	AND (OrderId = 10514 OR Quantity = 50);
```
<br><br>

#### **=** / **≠**, **<>** / **>**, **<** / **≥**, **≤**

의미는 각각 양쪽 값이 같음, 양쪽 같이 다름, (왼쪽, 오른쪽) 값이 더 큼, (왼쪽, 오른쪽) 값이 같거나 더 큼

```sql
SELECT 1 = 1, !(1 <> 1), NOT (1 < 2), 1 > 0 IS NOT FALSE;
```

```sql
SELECT 'A' = 'A', 'A' != 'B', 'A' < 'B', 'A' > 'B';
```

```sql
SELECT 'Apple' > 'Banana' OR 1 < 2 IS TRUE;
```
<br><br>

❗ **MySQL의 기본 사칙연산자는 대소문자 구분을 하지 않는다**

```sql
SELECT 'A' = 'a';
```

```sql
SELECT
  ProductName, Price,
  Price > 20 AS EXPENSIVE 
FROM Products;
```
<br><br>

💡 **테이블의 컬럼이 아닌 값으로 선택하기**

```sql
SELECT
  ProductName, Price,
  NOT Price > 20 AS CHEAP 
FROM Products;
```
<br><br>

#### **BETWEEN** {MIN}, **AND** {MAX} / **NOT BETWEEN** {MIN}, **AND** {MAX}

의미는 각각 두 값 사이에 있음, 두 값 사이가 아닌 곳에 있음

```sql
SELECT 5 BETWEEN 1 AND 10;
```

```sql
SELECT 'banana' NOT BETWEEN 'Apple' AND 'camera';
```

```sql
SELECT * FROM OrderDetails
WHERE ProductID BETWEEN 1 AND 4;
```

```sql
SELECT * FROM Customers
WHERE CustomerName BETWEEN 'b' AND 'c';
```
<br><br>

#### **IN** (...), **NOT IN (…)**

의미는 각각 괄호 안의 값들 가운데 있음, 괄호 안의 값들 가운데 없음

```sql
SELECT 1 + 2 IN (2, 3, 4)
```

```sql
SELECT 'Hello' IN (1, TRUE, 'hello')
```

```sql
SELECT * FROM Customers
WHERE City IN ('Torino', 'Paris', 'Portland', 'Madrid')
```
<br><br>

#### **LIKE** ‘…**%**…’, **LIKE** ‘…**_**…’

의미는 각각 0~N개 문자를 가진 패턴, _ 갯수만큼의 문자를 가진 패턴
```sql
SELECT
  'HELLO' LIKE 'hel%',
  'HELLO' LIKE 'H%',
  'HELLO' LIKE 'H%O',
  'HELLO' LIKE '%O',
  'HELLO' LIKE '%HELLO%',
  'HELLO' LIKE '%H',
  'HELLO' LIKE 'L%'
```

```sql
SELECT
  'HELLO' LIKE 'HEL__',
  'HELLO' LIKE 'h___O',
  'HELLO' LIKE 'HE_LO',
  'HELLO' LIKE '_____',
  'HELLO' LIKE '_HELLO',
  'HELLO' LIKE 'HEL_',
  'HELLO' LIKE 'H_O'
```

```sql
SELECT * FROM Employees
WHERE Notes LIKE '%economics%'
```

```sql
SELECT * FROM OrderDetails
WHERE OrderID LIKE '1025_'
```
<br><br>

## 숫자와 문자열을 다루는 함수들

<br><br>

## 시간/날짜 관련 및 기타 함수들

<br><br>

## 조건에 따라 그룹으로 묶기
