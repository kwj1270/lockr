package com.official.lockr.domain.club.fee.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.fee.api.dto.MarkPaidRequest;
import com.official.lockr.domain.club.fee.api.dto.MarkUnpaidRequest;
import com.official.lockr.domain.club.fee.api.dto.NotifyUnpaidRequest;
import com.official.lockr.domain.club.fee.api.dto.SetFeePolicyRequest;
import com.official.lockr.domain.club.fee.api.dto.UpdateFeeRecordMemoRequest;
import com.official.lockr.domain.club.fee.api.dto.UpdateFeeRecordRequest;
import com.official.lockr.domain.club.fee.application.usecase.MarkPaidFeeRecordUseCase;
import com.official.lockr.domain.club.fee.application.usecase.MarkUnpaidFeeRecordUseCase;
import com.official.lockr.domain.club.fee.application.usecase.NotifyUnpaidFeeUseCase;
import com.official.lockr.domain.club.fee.application.usecase.SetFeePolicyUseCase;
import com.official.lockr.domain.club.fee.application.usecase.UpdateFeeRecordMemoUseCase;
import com.official.lockr.domain.club.fee.application.usecase.UpdateFeeRecordUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}")
public class FeeApi {

    private final SetFeePolicyUseCase setFeePolicyUseCase;
    private final UpdateFeeRecordUseCase updateFeeRecordUseCase;
    private final NotifyUnpaidFeeUseCase notifyUnpaidFeeUseCase;
    private final MarkPaidFeeRecordUseCase markPaidFeeRecordUseCase;
    private final MarkUnpaidFeeRecordUseCase markUnpaidFeeRecordUseCase;
    private final UpdateFeeRecordMemoUseCase updateFeeRecordMemoUseCase;

    public FeeApi(final SetFeePolicyUseCase setFeePolicyUseCase,
                  final UpdateFeeRecordUseCase updateFeeRecordUseCase,
                  final NotifyUnpaidFeeUseCase notifyUnpaidFeeUseCase,
                  final MarkPaidFeeRecordUseCase markPaidFeeRecordUseCase,
                  final MarkUnpaidFeeRecordUseCase markUnpaidFeeRecordUseCase,
                  final UpdateFeeRecordMemoUseCase updateFeeRecordMemoUseCase) {
        this.setFeePolicyUseCase = setFeePolicyUseCase;
        this.updateFeeRecordUseCase = updateFeeRecordUseCase;
        this.notifyUnpaidFeeUseCase = notifyUnpaidFeeUseCase;
        this.markPaidFeeRecordUseCase = markPaidFeeRecordUseCase;
        this.markUnpaidFeeRecordUseCase = markUnpaidFeeRecordUseCase;
        this.updateFeeRecordMemoUseCase = updateFeeRecordMemoUseCase;
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

    @PostMapping("/fee-records/notify")
    public ResponseEntity<Void> notifyUnpaid(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestBody final NotifyUnpaidRequest request
    ) {
        notifyUnpaidFeeUseCase.notifyUnpaid(request.toCommand(clubId, signInSession.userId()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fee-records/{memberId}/paid")
    public ResponseEntity<Void> markPaid(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String memberId,
            @RequestBody final MarkPaidRequest request
    ) {
        markPaidFeeRecordUseCase.markPaid(request.toCommand(clubId, signInSession.userId(), memberId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fee-records/{memberId}/unpaid")
    public ResponseEntity<Void> markUnpaid(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String memberId,
            @RequestBody final MarkUnpaidRequest request
    ) {
        markUnpaidFeeRecordUseCase.markUnpaid(request.toCommand(clubId, signInSession.userId(), memberId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/fee-records/{memberId}/memo")
    public ResponseEntity<Void> updateMemo(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @PathVariable final String memberId,
            @RequestBody final UpdateFeeRecordMemoRequest request
    ) {
        updateFeeRecordMemoUseCase.updateMemo(request.toCommand(clubId, signInSession.userId(), memberId));
        return ResponseEntity.ok().build();
    }
}
