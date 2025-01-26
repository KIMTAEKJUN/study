package hello.hellospring.repository;

import hello.hellospring.domain.Member;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

// 테스트 코드는 서로 순서, 의존 관계 없이 설계가 되야한다. -> 그러므로 하나의 테스트 코드가 끝날 때 마다 저장소나 공용 데이터들을 지워줘야한다.
public class MemoryMemberRepositoryTest {
    MemoryMemberRepository repository = new MemoryMemberRepository();

    @AfterEach
    public void afterEach() {
        repository.storeClear();
    }

    @Test
    public void save() {
        Member member = new Member();
        member.setName("taekjun");

        repository.save(member);

        Member result = repository.findById(member.getId()).get();
        assertThat(result).isEqualTo(member);
    }

    @Test
    public void findByName() {
        Member member1 = new Member();
        member1.setName("taekjun1");
        repository.save(member1);

        Member member2 = new Member();
        member2.setName("taekjun2");
        repository.save(member2);

        Member result = repository.findByName("taekjun1").get();
        assertThat(result).isEqualTo(member1);
    }

    @Test
    public void findAll() {
        Member member1 = new Member();
        member1.setName("taekjun1");
        repository.save(member1);

        Member member2 = new Member();
        member2.setName("taekjun2");
        repository.save(member2);

        List<Member> result = repository.findAll();
        assertThat(result.size()).isEqualTo(2);
    }
}
