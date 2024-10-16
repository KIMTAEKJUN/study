package org.springframework.samples.petclinic.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 타겟을 메소드로 지정
@Target(ElementType.METHOD)
// 이 노테이션을 사용한 코드를 언제까지 유지할 것이냐? 런타임때까지 -> 그래야 스프링이 어노테이션 붙어있는 곳을 찾아 적용을 해주니까
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
}
