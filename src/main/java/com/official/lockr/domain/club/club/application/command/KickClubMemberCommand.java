package com.official.lockr.domain.club.club.application.command;

public record KickClubMemberCommand(String clubId, String requestUserId, String targetMemberId) {
}
