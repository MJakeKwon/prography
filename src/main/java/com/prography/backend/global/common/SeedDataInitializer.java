package com.prography.backend.global.common;

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
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SeedDataInitializer implements CommandLineRunner {

    private final CohortRepository cohortRepository;
    private final PartRepository partRepository;
    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final DepositHistoryRepository depositHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (memberRepository.findByLoginId("admin").isPresent()) {
            return;
        }

        Cohort cohort10 = cohortRepository.findByGeneration(10)
                .orElseGet(() -> cohortRepository.save(Cohort.builder().generation(10).name("10기").build()));

        Cohort cohort11 = cohortRepository.findByGeneration(11)
                .orElseGet(() -> cohortRepository.save(Cohort.builder().generation(11).name("11기").build()));

        createPartsIfAbsent(cohort10);
        createPartsIfAbsent(cohort11);
        createTeamsIfAbsent(cohort11);

        Member admin = memberRepository.save(
                Member.builder()
                        .loginId("admin")
                        .passwordHash(passwordEncoder.encode("admin1234"))
                        .name("관리자")
                        .phone("010-0000-0000")
                        .status(MemberStatus.ACTIVE)
                        .role(Role.ADMIN)
                        .build()
        );

        Part serverPart11 = partRepository.findByCohortIdAndName(cohort11.getId(), "SERVER")
                .orElseThrow();

        CohortMember adminCohortMember = cohortMemberRepository.save(
                CohortMember.builder()
                        .cohort(cohort11)
                        .member(admin)
                        .part(serverPart11)
                        .team(null)
                        .deposit(CohortMember.INITIAL_DEPOSIT)
                        .excuseCount(0)
                        .build()
        );

        depositHistoryRepository.save(
                DepositHistory.initial(
                        adminCohortMember,
                        CohortMember.INITIAL_DEPOSIT,
                        CohortMember.INITIAL_DEPOSIT,
                        "초기 보증금 설정"
                )
        );
    }

    private void createPartsIfAbsent(Cohort cohort) {
        createPartIfAbsent(cohort, "SERVER");
        createPartIfAbsent(cohort, "WEB");
        createPartIfAbsent(cohort, "iOS");
        createPartIfAbsent(cohort, "ANDROID");
        createPartIfAbsent(cohort, "DESIGN");
    }

    private void createPartIfAbsent(Cohort cohort, String name) {
        partRepository.findByCohortIdAndName(cohort.getId(), name)
                .orElseGet(() -> partRepository.save(
                        Part.builder().cohort(cohort).name(name).build()
                ));
    }

    private void createTeamsIfAbsent(Cohort cohort11) {
        createTeamIfAbsent(cohort11, "Team A");
        createTeamIfAbsent(cohort11, "Team B");
        createTeamIfAbsent(cohort11, "Team C");
    }

    private void createTeamIfAbsent(Cohort cohort, String name) {
        teamRepository.findByCohortIdAndName(cohort.getId(), name)
                .orElseGet(() -> teamRepository.save(
                        Team.builder().cohort(cohort).name(name).build()
                ));
    }
}