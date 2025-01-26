package hello.hellospring.service;

import hello.hellospring.domain.Member;
import hello.hellospring.repository.MemoryMemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

// 단위 테스트
class MemberServiceTest {
    MemberService memberService;
    MemoryMemberRepository memberRepository;

    @BeforeEach
    public void beforeEach() {
        memberRepository = new MemoryMemberRepository();
        memberService = new MemberService(memberRepository);
    }

    @AfterEach
    public void afterEach() {
        memberRepository.storeClear();
    }

    @Test
    void 회원가입() {
        // given
        Member member = new Member();
        member.setName("taekjun");

        // when
        Long saveId = memberService.join(member);

        // then
        Member findMember = memberService.findOne(saveId).get();
        assertThat(member.getName()).isEqualTo(findMember.getName());
    }

    @Test
    void 중복_회원_예외() {
        // given
        Member member1 = new Member();
        member1.setName("taekjun");

        Member member2 = new Member();
        member2.setName("taekjun");

        // when
        memberService.join(member1);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> memberService.join(member2));
        assertThat(e.getMessage()).isEqualTo("이미 존재하는 회원임 ㅋ");

        // try {
        //    memberService.join(member2);
        //    fail("");
        // } catch (IllegalStateException e) {
        //    assertThat(e.getMessage()).isEqualTo("이미 존재하는 회원임 ㅋ");
        // }

        // then
    }

    @Test
    void findMembers() {
    }

    @Test
    void findOne() {

    }
}