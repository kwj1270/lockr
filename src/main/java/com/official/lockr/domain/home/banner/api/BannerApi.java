package com.official.lockr.domain.home.banner.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.home.banner.api.dto.ActivateBannerRequest;
import com.official.lockr.domain.home.banner.api.dto.BannerResponse;
import com.official.lockr.domain.home.banner.api.dto.CreateBannerRequest;
import com.official.lockr.domain.home.banner.api.dto.ReorderBannerRequest;
import com.official.lockr.domain.home.banner.api.dto.UpdateBannerRequest;
import com.official.lockr.domain.home.banner.application.command.DeleteBannerCommand;
import com.official.lockr.domain.home.banner.application.usecase.CreateBannerUseCase;
import com.official.lockr.domain.home.banner.application.usecase.DeleteBannerUseCase;
import com.official.lockr.domain.home.banner.application.usecase.ReorderBannerUseCase;
import com.official.lockr.domain.home.banner.application.usecase.ToggleBannerActiveUseCase;
import com.official.lockr.domain.home.banner.application.usecase.UpdateBannerUseCase;
import com.official.lockr.domain.home.banner.domain.Banner;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.AdminDao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

import static org.jooq.generated.Tables.ADMIN;

@RestController
@RequestMapping("/api/v1/banners")
public class BannerApi {

    private final CreateBannerUseCase createBannerUseCase;
    private final UpdateBannerUseCase updateBannerUseCase;
    private final DeleteBannerUseCase deleteBannerUseCase;
    private final ToggleBannerActiveUseCase toggleBannerActiveUseCase;
    private final ReorderBannerUseCase reorderBannerUseCase;
    private final AdminDao adminDao;

    public BannerApi(final CreateBannerUseCase createBannerUseCase,
                     final UpdateBannerUseCase updateBannerUseCase,
                     final DeleteBannerUseCase deleteBannerUseCase,
                     final ToggleBannerActiveUseCase toggleBannerActiveUseCase,
                     final ReorderBannerUseCase reorderBannerUseCase,
                     final Configuration configuration) {
        this.createBannerUseCase = createBannerUseCase;
        this.updateBannerUseCase = updateBannerUseCase;
        this.deleteBannerUseCase = deleteBannerUseCase;
        this.toggleBannerActiveUseCase = toggleBannerActiveUseCase;
        this.reorderBannerUseCase = reorderBannerUseCase;
        this.adminDao = new AdminDao(configuration);
    }

    @PostMapping
    public ResponseEntity<BannerResponse> create(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @RequestBody final CreateBannerRequest request
    ) {
        validateAdmin(signInSession.userId());
        final Banner banner = createBannerUseCase.create(request.toCommand());
        return ResponseEntity
                .created(URI.create("/api/v1/banners/" + banner.getId()))
                .body(BannerResponse.from(banner));
    }

    @PostMapping("/{bannerId}/update")
    public ResponseEntity<Void> update(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String bannerId,
            @RequestBody final UpdateBannerRequest request
    ) {
        validateAdmin(signInSession.userId());
        updateBannerUseCase.update(request.toCommand(bannerId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{bannerId}/delete")
    public ResponseEntity<Void> delete(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String bannerId
    ) {
        validateAdmin(signInSession.userId());
        deleteBannerUseCase.delete(new DeleteBannerCommand(bannerId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{bannerId}/activate")
    public ResponseEntity<Void> activate(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String bannerId,
            @RequestBody final ActivateBannerRequest request
    ) {
        validateAdmin(signInSession.userId());
        toggleBannerActiveUseCase.toggle(request.toCommand(bannerId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{bannerId}/order")
    public ResponseEntity<Void> reorder(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String bannerId,
            @RequestBody final ReorderBannerRequest request
    ) {
        validateAdmin(signInSession.userId());
        reorderBannerUseCase.reorder(request.toCommand(bannerId));
        return ResponseEntity.ok().build();
    }

    private void validateAdmin(final String userId) {
        final boolean isAdmin = adminDao.ctx()
                .fetchExists(
                        adminDao.ctx().selectOne()
                                .from(ADMIN)
                                .where(ADMIN.USER_ID.eq(userId))
                                .and(ADMIN.DELETED_AT.isNull())
                );
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자 권한이 필요합니다.");
        }
    }
}
