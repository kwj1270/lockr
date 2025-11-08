package com.official.lockr.domain.club.contract.api;

import com.official.lockr.domain.auth.domain.signin.SignInSession;
import com.official.lockr.domain.club.contract.api.dto.SignIndividualUserContractRequest;
import com.official.lockr.domain.club.contract.api.dto.SignRepresentativeContractRequest;
import com.official.lockr.domain.club.contract.application.SignRepresentativeContractUseCase;
import com.official.lockr.domain.club.contract.application.SignIndividualUserContractUseCase;
import com.official.lockr.domain.club.contract.domain.Contract;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}/contract")
public class ContractApi {

    private final SignRepresentativeContractUseCase signRepresentativeContractUseCase;
    private final SignIndividualUserContractUseCase signIndividualUserContractUseCase;

    public ContractApi(final SignRepresentativeContractUseCase signRepresentativeContractUseCase,
                       final SignIndividualUserContractUseCase signIndividualUserContractUseCase
    ) {
        this.signRepresentativeContractUseCase = signRepresentativeContractUseCase;
        this.signIndividualUserContractUseCase = signIndividualUserContractUseCase;
    }

    @PostMapping
    public ResponseEntity<Contract> propose(
            @PathVariable("clubId") final String clubId,
            @RequestBody final SignRepresentativeContractRequest request,
            final HttpSession httpSession
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Contract contract = signRepresentativeContractUseCase.sign(request.toCommand(clubId, signIn.userId()));
        return ResponseEntity.created(URI.create("/api/v1/contract/" + clubId + "/" + contract.getId())).body(contract);
    }

    @PostMapping("/sign")
    public ResponseEntity<Contract> sign(
            @PathVariable final String clubId,
            @RequestBody final SignIndividualUserContractRequest request,
            final HttpSession httpSession
    ) {
        final SignInSession signIn = (SignInSession) httpSession.getAttribute("signIn");
        final Contract contract = signIndividualUserContractUseCase.sign(request.toCommand(clubId, signIn.userId()));
        return ResponseEntity.created(URI.create("/api/v1/clubs/" + clubId + "/contract/" + contract.getId())).body(contract);
    }
}
