package com.prography.backend.service;

import com.prography.backend.domain.member.entity.Member;
import com.prography.backend.domain.member.entity.MemberStatus;
import com.prography.backend.domain.member.entity.Role;
import com.prography.backend.domain.member.repository.MemberRepository;
import com.prography.backend.domain.member.service.AuthService;
import com.prography.backend.global.exception.BusinessException;
import com.prography.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공 - 비밀번호 일치")
    void login_success() {
        Member member = Member.builder()
                .loginId("admin")
                .passwordHash("hashed")
                .name("관리자")
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();

        given(memberRepository.findByLoginId("admin")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("admin1234", "hashed")).willReturn(true);

        Member result = authService.login("admin", "admin1234");

        assertThat(result).isSameAs(member);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 아이디")
    void login_fail_when_login_id_not_found() {
        given(memberRepository.findByLoginId("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("unknown", "pw"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_when_password_not_match() {
        Member member = Member.builder()
                .loginId("admin")
                .passwordHash("hashed")
                .name("관리자")
                .status(MemberStatus.ACTIVE)
                .role(Role.ADMIN)
                .build();

        given(memberRepository.findByLoginId("admin")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrong", "hashed")).willReturn(false);

        assertThatThrownBy(() -> authService.login("admin", "wrong"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("로그인 실패 - 탈퇴 회원")
    void login_fail_when_withdrawn_member() {
        Member member = Member.builder()
                .loginId("withdrawn")
                .passwordHash("hashed")
                .name("탈퇴회원")
                .status(MemberStatus.WITHDRAWN)
                .role(Role.MEMBER)
                .build();

        given(memberRepository.findByLoginId("withdrawn")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("pw", "hashed")).willReturn(true);

        assertThatThrownBy(() -> authService.login("withdrawn", "pw"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_WITHDRAWN);
    }
}