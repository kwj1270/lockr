package com.official.lockr.domain.club.board.domain.entry;

import com.official.lockr.domain.club.common.Position;
import com.official.lockr.domain.club.board.domain.entry.player.FieldPlayer;
import com.official.lockr.domain.club.common.Location;

import java.util.*;
import java.util.stream.Collectors;

public class FormationMatcher {

    /**
     * 현재 선수들의 위치를 기반으로 포메이션의 포지션에 최적으로 매칭합니다.
     * 가장 가까운 거리를 기준으로 그리디 알고리즘을 사용합니다.
     *
     * @param players 현재 필드에 있는 선수들
     * @param formation 적용할 포메이션
     * @return 선수 ID와 새로운 포지션의 매핑
     */
    public static Map<String, Position> playersToFormation(final List<FieldPlayer> players, final Formation formation) {
        if (players.isEmpty()) {
            return Collections.emptyMap();
        }

        final List<Position> formationPositions = new ArrayList<>(formation.getPositions());
        final Map<String, Position> playerPositionMap = new HashMap<>();
        final Set<Position> assignedPositions = new HashSet<>();

        // GK는 항상 GK 포지션에 매칭
        final Optional<FieldPlayer> gkPlayer = players.stream()
                .filter(FieldPlayer::isGk)
                .findFirst();

        if (gkPlayer.isPresent()) {
            playerPositionMap.put(gkPlayer.get().getSquadPlayerId(), Position.GK);
            assignedPositions.add(Position.GK);
        }

        // GK가 아닌 선수들을 매칭
        final List<FieldPlayer> nonGkPlayers = players.stream()
                .filter(player -> !player.isGk())
                .toList();

        final List<Position> availablePositions = formationPositions.stream()
                .filter(pos -> !assignedPositions.contains(pos))
                .collect(Collectors.toList());

        // 각 선수에 대해 가장 가까운 포지션 찾기 (그리디)
        for (FieldPlayer player : nonGkPlayers) {
            if (availablePositions.isEmpty()) {
                break;
            }

            final Location currentLocation = player.getLocation();
            final Position closestPosition = findClosestPosition(currentLocation, availablePositions);

            playerPositionMap.put(player.getSquadPlayerId(), closestPosition);
            availablePositions.remove(closestPosition);
        }

        return playerPositionMap;
    }

    /**
     * 현재 선수들의 위치를 기반으로 가장 적합한 포메이션을 추론합니다.
     *
     * @param players 현재 필드에 있는 선수들
     * @return 가장 적합한 포메이션
     */
    public static Formation inferFormation(final List<FieldPlayer> players) {
        if (players.size() != 11) {
            return Formation.FORMATION_4_3_3; // 기본 포메이션
        }

        Formation bestFormation = Formation.FORMATION_4_3_3;
        double minTotalDistance = Double.MAX_VALUE;

        // 각 포메이션에 대해 총 거리 계산
        for (Formation formation : Formation.values()) {
            if (formation.getRequiredPlayerCount() != players.size()) {
                continue; // 선수 수가 맞지 않으면 스킵
            }

            double totalDistance = calculateTotalDistance(players, formation);
            if (totalDistance < minTotalDistance) {
                minTotalDistance = totalDistance;
                bestFormation = formation;
            }
        }

        return bestFormation;
    }

    /**
     * 선수들과 포메이션 간의 총 거리를 계산합니다.
     */
    private static double calculateTotalDistance(final List<FieldPlayer> players, final Formation formation) {
        final Map<String, Position> matching = playersToFormation(players, formation);
        double totalDistance = 0.0;

        for (FieldPlayer player : players) {
            final Position assignedPosition = matching.get(player.getSquadPlayerId());
            if (assignedPosition != null) {
                final Location currentLocation = player.getLocation();
                totalDistance += calculateDistance(
                        currentLocation.x(), currentLocation.y(),
                        assignedPosition.getX(), assignedPosition.getY()
                );
            }
        }

        return totalDistance;
    }

    /**
     * 주어진 위치에서 가장 가까운 포지션을 찾습니다.
     */
    private static Position findClosestPosition(final Location location, final List<Position> positions) {
        Position closestPosition = positions.get(0);
        double minDistance = calculateDistance(
                location.x(), location.y(),
                closestPosition.getX(), closestPosition.getY()
        );

        for (Position position : positions) {
            double distance = calculateDistance(
                    location.x(), location.y(),
                    position.getX(), position.getY()
            );

            if (distance < minDistance) {
                minDistance = distance;
                closestPosition = position;
            }
        }

        return closestPosition;
    }

    /**
     * 두 점 사이의 유클리드 거리를 계산합니다.
     */
    private static double calculateDistance(final int x1, final int y1, final int x2, final int y2) {
        final int dx = x2 - x1;
        final int dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
