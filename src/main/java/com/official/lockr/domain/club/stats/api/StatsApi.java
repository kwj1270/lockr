package com.official.lockr.domain.club.stats.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.stats.api.dto.MatchRecordResponse;
import com.official.lockr.domain.club.stats.api.dto.RecordMatchRequest;
import com.official.lockr.domain.club.stats.api.dto.UpdateMatchRequest;
import com.official.lockr.domain.club.stats.application.usecase.DeleteMatchUseCase;
import com.official.lockr.domain.club.stats.application.usecase.RecordMatchUseCase;
import com.official.lockr.domain.club.stats.application.usecase.UpdateMatchUseCase;
import com.official.lockr.domain.club.stats.domain.MatchRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RequestMapping("/api/v1/clubs/{clubId}/stats")
@RestController
public class StatsApi {

    private final RecordMatchUseCase recordMatchUseCase;
    private final UpdateMatchUseCase updateMatchUseCase;
    private final DeleteMatchUseCase deleteMatchUseCase;

    public StatsApi(final RecordMatchUseCase recordMatchUseCase,
                    final UpdateMatchUseCase updateMatchUseCase,
                    final DeleteMatchUseCase deleteMatchUseCase) {
        this.recordMatchUseCase = recordMatchUseCase;
        this.updateMatchUseCase = updateMatchUseCase;
        this.deleteMatchUseCase = deleteMatchUseCase;
    }

    @PostMapping("/matches")
    public ResponseEntity<MatchRecordResponse> recordMatch(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final RecordMatchRequest request
    ) {
        final MatchRecord matchRecord = recordMatchUseCase.record(
                request.toCommand(clubId, signInSession.userId())
        );
        return ResponseEntity
                .created(URI.create("/clubs/" + clubId + "/stats/matches/" + matchRecord.getId()))
                .body(MatchRecordResponse.from(matchRecord));
    }

    @PostMapping("/matches/{recordId}/update")
    public ResponseEntity<MatchRecordResponse> updateMatch(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String recordId,
            @RequestBody final UpdateMatchRequest request
    ) {
        final MatchRecord matchRecord = updateMatchUseCase.update(
                request.toCommand(recordId, clubId, signInSession.userId())
        );
        return ResponseEntity.ok(MatchRecordResponse.from(matchRecord));
    }

    @PostMapping("/matches/{recordId}/delete")
    public ResponseEntity<Void> deleteMatch(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String recordId
    ) {
        deleteMatchUseCase.delete(clubId, recordId, signInSession.userId());
        return ResponseEntity.ok().build();
    }
}
