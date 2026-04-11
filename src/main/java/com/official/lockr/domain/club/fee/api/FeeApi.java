package com.official.lockr.domain.club.fee.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.fee.api.dto.NotifyUnpaidRequest;
import com.official.lockr.domain.club.fee.api.dto.SetFeePolicyRequest;
import com.official.lockr.domain.club.fee.api.dto.UpdateFeeRecordRequest;
import com.official.lockr.domain.club.fee.application.usecase.NotifyUnpaidFeeUseCase;
import com.official.lockr.domain.club.fee.application.usecase.SetFeePolicyUseCase;
import com.official.lockr.domain.club.fee.application.usecase.UpdateFeeRecordUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}")
public class FeeApi {

    private final SetFeePolicyUseCase setFeePolicyUseCase;
    private final UpdateFeeRecordUseCase updateFeeRecordUseCase;
    private final NotifyUnpaidFeeUseCase notifyUnpaidFeeUseCase;

    public FeeApi(final SetFeePolicyUseCase setFeePolicyUseCase,
                  final UpdateFeeRecordUseCase updateFeeRecordUseCase,
                  final NotifyUnpaidFeeUseCase notifyUnpaidFeeUseCase) {
        this.setFeePolicyUseCase = setFeePolicyUseCase;
        this.updateFeeRecordUseCase = updateFeeRecordUseCase;
        this.notifyUnpaidFeeUseCase = notifyUnpaidFeeUseCase;
    }

    @PostMapping("/fee-policies")
    public ResponseEntity<Void> setPolicy(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final SetFeePolicyRequest request
    ) {
        setFeePolicyUseCase.setPolicy(request.toCommand(clubId, signInSession.userId()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fee-records/{memberId}")
    public ResponseEntity<Void> updateRecord(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String memberId,
            @RequestBody final UpdateFeeRecordRequest request
    ) {
        updateFeeRecordUseCase.updateRecord(request.toCommand(clubId, signInSession.userId(), memberId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fee-records/notify-unpaid")
    public ResponseEntity<Void> notifyUnpaid(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final NotifyUnpaidRequest request
    ) {
        notifyUnpaidFeeUseCase.notifyUnpaid(request.toCommand(clubId, signInSession.userId()));
        return ResponseEntity.ok().build();
    }
}
