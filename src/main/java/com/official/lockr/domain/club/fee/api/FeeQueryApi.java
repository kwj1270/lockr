package com.official.lockr.domain.club.fee.api;

import com.official.lockr.domain.auth.signin.domain.SignInSession;
import com.official.lockr.domain.club.fee.api.dto.FeeMyRecordResponse;
import com.official.lockr.domain.club.fee.api.dto.FeePolicyResponse;
import com.official.lockr.domain.club.fee.api.dto.FeeRecordResponse;
import com.official.lockr.domain.club.fee.api.dto.FeeRecordSummaryResponse;
import org.jooq.Configuration;
import org.jooq.generated.tables.daos.FeePoliciesDao;
import org.jooq.generated.tables.daos.FeeRecordsDao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.jooq.generated.tables.FeePoliciesJOOQEntity.FEE_POLICIES;
import static org.jooq.generated.tables.FeeRecordsJOOQEntity.FEE_RECORDS;
import static org.jooq.generated.tables.MembersJOOQEntity.MEMBERS;

@RestController
@RequestMapping("/api/v1/clubs/{clubId}")
public class FeeQueryApi {

    private final FeePoliciesDao feePoliciesDao;
    private final FeeRecordsDao feeRecordsDao;

    public FeeQueryApi(final Configuration configuration) {
        this.feePoliciesDao = new FeePoliciesDao(configuration);
        this.feeRecordsDao = new FeeRecordsDao(configuration);
    }

    @GetMapping("/fee-policies")
    public ResponseEntity<FeePolicyResponse> getFeePolicy(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId
    ) {
        final var record = feePoliciesDao.ctx()
                .selectFrom(FEE_POLICIES)
                .where(FEE_POLICIES.CLUB_ID.eq(clubId))
                .fetchOne();
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new FeePolicyResponse(
                record.getId(),
                record.getClubId(),
                record.getAmount(),
                record.getDueDay(),
                record.getBankName(),
                record.getAccountNumber(),
                record.getAccountHolder()
        ));
    }

    @GetMapping("/fee-records")
    public ResponseEntity<FeeRecordSummaryResponse> getFeeRecords(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestParam final int year,
            @RequestParam final int month
    ) {
        final boolean isOperator = isOperator(clubId, signInSession.userId());

        final List<FeeRecordResponse> records;
        if (isOperator) {
            records = feePoliciesDao.ctx()
                    .select(
                            FEE_RECORDS.MEMBER_ID,
                            MEMBERS.NAME,
                            FEE_RECORDS.YEAR,
                            FEE_RECORDS.MONTH,
                            FEE_RECORDS.STATUS,
                            FEE_RECORDS.MEMO
                    )
                    .from(FEE_RECORDS)
                    .leftJoin(MEMBERS).on(MEMBERS.ID.eq(FEE_RECORDS.MEMBER_ID))
                    .where(FEE_RECORDS.CLUB_ID.eq(clubId))
                    .and(FEE_RECORDS.YEAR.eq(year))
                    .and(FEE_RECORDS.MONTH.eq(month))
                    .fetch()
                    .map(r -> new FeeRecordResponse(
                            r.get(FEE_RECORDS.MEMBER_ID),
                            r.get(MEMBERS.NAME),
                            r.get(FEE_RECORDS.YEAR),
                            r.get(FEE_RECORDS.MONTH),
                            r.get(FEE_RECORDS.STATUS),
                            r.get(FEE_RECORDS.MEMO)
                    ));
        } else {
            final String myMemberId = resolveMemberId(clubId, signInSession.userId());
            records = feePoliciesDao.ctx()
                    .select(
                            FEE_RECORDS.MEMBER_ID,
                            MEMBERS.NAME,
                            FEE_RECORDS.YEAR,
                            FEE_RECORDS.MONTH,
                            FEE_RECORDS.STATUS,
                            FEE_RECORDS.MEMO
                    )
                    .from(FEE_RECORDS)
                    .leftJoin(MEMBERS).on(MEMBERS.ID.eq(FEE_RECORDS.MEMBER_ID))
                    .where(FEE_RECORDS.CLUB_ID.eq(clubId))
                    .and(FEE_RECORDS.YEAR.eq(year))
                    .and(FEE_RECORDS.MONTH.eq(month))
                    .and(FEE_RECORDS.MEMBER_ID.eq(myMemberId))
                    .fetch()
                    .map(r -> new FeeRecordResponse(
                            r.get(FEE_RECORDS.MEMBER_ID),
                            r.get(MEMBERS.NAME),
                            r.get(FEE_RECORDS.YEAR),
                            r.get(FEE_RECORDS.MONTH),
                            r.get(FEE_RECORDS.STATUS),
                            r.get(FEE_RECORDS.MEMO)
                    ));
        }

        final int totalCount = records.size();
        final int paidCount = (int) records.stream().filter(r -> "PAID".equals(r.status())).count();
        final double paidRate = totalCount > 0 ? (double) paidCount / totalCount : 0.0;

        return ResponseEntity.ok(new FeeRecordSummaryResponse(records, totalCount, paidCount, paidRate));
    }

    @GetMapping("/fee-records/me")
    public ResponseEntity<FeeMyRecordResponse> getMyFeeRecord(
            @RequestAttribute("signInSession") final SignInSession signInSession,
            @PathVariable final String clubId,
            @RequestParam final int year,
            @RequestParam final int month
    ) {
        final String myMemberId = resolveMemberId(clubId, signInSession.userId());
        if (myMemberId == null) {
            return ResponseEntity.notFound().build();
        }

        final var record = feeRecordsDao.ctx()
                .selectFrom(FEE_RECORDS)
                .where(FEE_RECORDS.CLUB_ID.eq(clubId))
                .and(FEE_RECORDS.MEMBER_ID.eq(myMemberId))
                .and(FEE_RECORDS.YEAR.eq(year))
                .and(FEE_RECORDS.MONTH.eq(month))
                .fetchOne();

        final long totalCount = feeRecordsDao.ctx()
                .selectCount()
                .from(FEE_RECORDS)
                .where(FEE_RECORDS.CLUB_ID.eq(clubId))
                .and(FEE_RECORDS.YEAR.eq(year))
                .and(FEE_RECORDS.MONTH.eq(month))
                .fetchOne(0, Long.class);

        final long paidCount = feeRecordsDao.ctx()
                .selectCount()
                .from(FEE_RECORDS)
                .where(FEE_RECORDS.CLUB_ID.eq(clubId))
                .and(FEE_RECORDS.YEAR.eq(year))
                .and(FEE_RECORDS.MONTH.eq(month))
                .and(FEE_RECORDS.STATUS.eq("PAID"))
                .fetchOne(0, Long.class);

        final double paidRate = totalCount > 0 ? (double) paidCount / totalCount : 0.0;

        if (record == null) {
            return ResponseEntity.ok(new FeeMyRecordResponse(year, month, "UNPAID", null, paidRate));
        }

        return ResponseEntity.ok(new FeeMyRecordResponse(
                record.getYear(),
                record.getMonth(),
                record.getStatus(),
                record.getMemo(),
                paidRate
        ));
    }

    private boolean isOperator(final String clubId, final String userId) {
        final String role = feePoliciesDao.ctx()
                .select(MEMBERS.MEMBER_ROLE)
                .from(MEMBERS)
                .where(MEMBERS.CLUB_ID.eq(clubId))
                .and(MEMBERS.USER_ID.eq(userId))
                .and(MEMBERS.DELETED_AT.isNull())
                .fetchOne(MEMBERS.MEMBER_ROLE);
        return "PRESIDENT".equals(role) || "VICE_PRESIDENT".equals(role) || "TREASURER".equals(role);
    }

    private String resolveMemberId(final String clubId, final String userId) {
        return feePoliciesDao.ctx()
                .select(MEMBERS.ID)
                .from(MEMBERS)
                .where(MEMBERS.CLUB_ID.eq(clubId))
                .and(MEMBERS.USER_ID.eq(userId))
                .and(MEMBERS.DELETED_AT.isNull())
                .fetchOne(MEMBERS.ID);
    }
}
