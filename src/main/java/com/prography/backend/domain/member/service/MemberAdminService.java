package com.prography.backend.domain.member.service;

import com.prography.backend.domain.cohort.entity.Cohort;
import com.prography.backend.domain.cohort.entity.CohortMember;
import com.prography.backend.domain.cohort.entity.Part;
import com.prography.backend.domain.cohort.entity.Team;
import com.prography.backend.domain.cohort.repository.CohortMemberRepository;
import com.prography.backend.domain.cohort.repository.CohortRepository;
import com.prography.backend.domain.cohort.repository.PartRepository;
import com.prography.backend.domain.cohort.repository.TeamRepository;
import com.prography.backend.domain.deposit.entity.DepositHistory;
import com.prography.backend.domain.deposit.repository.DepositHistoryRepository;
import com.prography.backend.domain.member.entity.Member;
import com.prography.backend.domain.member.entity.MemberStatus;
import com.prography.backend.domain.member.entity.Role;
import com.prography.backend.domain.member.repository.MemberRepository;
import com.prography.backend.global.exception.BusinessException;
import com.prography.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberAdminService {

    private final MemberRepository memberRepository;
    private final CohortRepository cohortRepository;
    private final PartRepository partRepository;
    private final TeamRepository teamRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final DepositHistoryRepository depositHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Member> getMemberDashboard() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member getAdminMemberDetail(Long memberId) {
        return getMember(memberId);
    }

    @Transactional
    public Member createMember(
            String loginId,
            String rawPassword,
            String name,
            String phone,
            Role role,
            Long cohortId,
            Long partId,
            Long teamId // nullable 허용
    ) {
        if (memberRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.LOGIN_ID_DUPLICATED);
        }

        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_NOT_FOUND));
        Part part = partRepository.findById(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PART_NOT_FOUND));

        Team team = null;
        if (teamId != null) {
            team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
        }

        Member member = Member.builder()
                .loginId(loginId)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .name(name)
                .phone(phone)
                .status(MemberStatus.ACTIVE)
                .role(role == null ? Role.MEMBER : role)
                .build();

        try {
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.LOGIN_ID_DUPLICATED);
        }

        CohortMember cohortMember = CohortMember.builder()
                .cohort(cohort)
                .member(member)
                .part(part)
                .team(team)
                .deposit(CohortMember.INITIAL_DEPOSIT)
                .excuseCount(0)
                .build();
        cohortMemberRepository.save(cohortMember);

        depositHistoryRepository.save(
                DepositHistory.initial(
                        cohortMember,
                        CohortMember.INITIAL_DEPOSIT,
                        CohortMember.INITIAL_DEPOSIT,
                        "초기 보증금 설정"
                )
        );

        return member;
    }

    @Transactional
    public Member updateMember(
            Long memberId,
            String name,
            String phone,
            Long cohortId,
            Long partId,
            Long teamId
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        member.updateProfile(name, phone);

        // 과제 요구사항상 기수/파트/팀 수정 가능성 반영
        // 단, cohort_members는 (cohort, member) 유니크라 "기수 변경"은 신규 row/삭제가 필요할 수 있음.
        // 빠른 구현 기준: 같은 기수 내 part/team만 수정한다고 가정.
        if (cohortId != null) {
            CohortMember cm = cohortMemberRepository.findByCohortIdAndMemberId(cohortId, memberId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

            if (partId != null) {
                Part part = partRepository.findById(partId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.PART_NOT_FOUND));
                Team team = null;
                if (teamId != null) {
                    team = teamRepository.findById(teamId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
                }
                cm.updateAssignment(part, team);
            }
        }

        return member;
    }

    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.withdraw();
    }
}