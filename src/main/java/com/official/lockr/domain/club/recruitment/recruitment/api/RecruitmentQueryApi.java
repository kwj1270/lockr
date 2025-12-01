package com.official.lockr.domain.club.recruitment.recruitment.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.recruitment.recruitment.api.dto.RecruitmentResponse;
import com.official.lockr.domain.club.recruitment.recruitment.domain.Recruitment;
import com.official.lockr.domain.club.recruitment.recruitment.domain.RecruitmentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.isNull;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}/recruitments")
public class RecruitmentQueryApi {

    private final RecruitmentRepository recruitmentRepository;

    public RecruitmentQueryApi(final RecruitmentRepository recruitmentRepository) {
        this.recruitmentRepository = recruitmentRepository;
    }

    @GetMapping("/{recruitmentId}")
    public ResponseEntity<RecruitmentResponse> getRecruitmentById(
            final HttpSession httpSession,
            @PathVariable final String clubId,
            @PathVariable final String recruitmentId
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Recruitment recruitment = recruitmentRepository.findById(recruitmentId);
        if (isNull(recruitment) || !recruitment.getClubId().equals(clubId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(RecruitmentResponse.from(recruitment));
    }
}
