package com.official.lockr.domain.club.lineup.squad.infrastructure;

import com.official.lockr.domain.club.lineup.squad.domain.Squad;
import com.official.lockr.domain.club.lineup.squad.domain.SquadPlayer;
import com.official.lockr.domain.club.lineup.squad.domain.vo.SquadPlayerRole;
import com.official.lockr.global.ddd.DomainEventPublisher;
import com.official.lockr.global.vo.BackNumber;
import com.official.lockr.global.vo.Foot;
import com.official.lockr.global.vo.Position;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.generated.tables.SquadPlayersJOOQEntity.SQUAD_PLAYERS;
import static org.jooq.generated.tables.SquadsJOOQEntity.SQUADS;

@DisplayName("JOOQSquadRepository 테스트")
class JOOQSquadRepositoryTest {

    private Connection connection;
    private DSLContext dsl;
    private JOOQSquadRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        // H2 in-memory database
        connection = DriverManager.getConnection(
            "jdbc:h2:mem:test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
            "sa",
            ""
        );

        Configuration configuration = new DefaultConfiguration()
            .set(connection)
            .set(SQLDialect.H2);

        dsl = DSL.using(configuration);

        // Create tables
        dsl.execute("CREATE TABLE squads (" +
            "id VARCHAR(128) PRIMARY KEY, " +
            "club_id VARCHAR(128) NOT NULL, " +
            "created_at TIMESTAMP NOT NULL, " +
            "updated_at TIMESTAMP NOT NULL, " +
            "deleted_at TIMESTAMP" +
            ")");

        dsl.execute("CREATE TABLE squad_players (" +
            "id VARCHAR(128) PRIMARY KEY, " +
            "member_id VARCHAR(128) NOT NULL, " +
            "squad_id VARCHAR(128) NOT NULL, " +
            "profile_image VARCHAR(255), " +
            "name VARCHAR(100), " +
            "positions VARCHAR(255), " +
            "birth VARCHAR(10), " +
            "height VARCHAR(10), " +
            "weight VARCHAR(10), " +
            "foot VARCHAR(10), " +
            "back_number INT NOT NULL, " +
            "player_role VARCHAR(20) NOT NULL, " +
            "created_at TIMESTAMP NOT NULL, " +
            "updated_at TIMESTAMP NOT NULL, " +
            "deleted_at TIMESTAMP" +
            ")");

        DomainEventPublisher eventPublisher = events -> {};
        repository = new JOOQSquadRepository(configuration, eventPublisher);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("Squad 저장 시 SquadPlayer가 올바르게 저장되어야 한다")
    void shouldSaveSquadPlayer() {
        // Given
        String squadId = "squad-1";
        String clubId = "club-1";
        String playerId = "player-1";
        String memberId = "member-1";
        String playerName = "홍길동";

        SquadPlayer player = new SquadPlayer(
            playerId,
            squadId,
            memberId,
            playerName,
            "https://example.com/profile.jpg",
            "1995-01-01",
            "180",
            "75",
            Foot.RIGHT,
            List.of(Position.ST),
            new BackNumber(10),
            SquadPlayerRole.CAPTAIN,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null
        );

        Squad squad = new Squad(
            squadId,
            clubId,
            new ArrayList<>(List.of(player)),
            LocalDateTime.now(),
            LocalDateTime.now(),
            null
        );

        // When
        repository.save(squad);

        // Then
        String savedName = dsl.select(SQUAD_PLAYERS.NAME)
            .from(SQUAD_PLAYERS)
            .where(SQUAD_PLAYERS.ID.eq(playerId))
            .fetchOne(SQUAD_PLAYERS.NAME);

        assertThat(savedName).isEqualTo(playerName);
    }

    @Test
    @DisplayName("Squad 조회 시 SquadPlayer가 올바르게 복원되어야 한다")
    void shouldRetrieveSquadPlayer() {
        // Given
        String squadId = "squad-2";
        String clubId = "club-2";
        String playerId = "player-2";
        String memberId = "member-2";
        String playerName = "타나카";
        LocalDateTime now = LocalDateTime.now();

        // Insert squad
        dsl.insertInto(SQUADS)
            .set(SQUADS.ID, squadId)
            .set(SQUADS.CLUB_ID, clubId)
            .set(SQUADS.CREATED_AT, now)
            .set(SQUADS.UPDATED_AT, now)
            .execute();

        // Insert player
        dsl.insertInto(SQUAD_PLAYERS)
            .set(SQUAD_PLAYERS.ID, playerId)
            .set(SQUAD_PLAYERS.MEMBER_ID, memberId)
            .set(SQUAD_PLAYERS.SQUAD_ID, squadId)
            .set(SQUAD_PLAYERS.PROFILE_IMAGE, "https://example.com/profile.jpg")
            .set(SQUAD_PLAYERS.NAME, playerName)
            .set(SQUAD_PLAYERS.POSITIONS, "CM")
            .set(SQUAD_PLAYERS.BIRTH, "1993-03-15")
            .set(SQUAD_PLAYERS.HEIGHT, "175")
            .set(SQUAD_PLAYERS.WEIGHT, "70")
            .set(SQUAD_PLAYERS.FOOT, "LEFT")
            .set(SQUAD_PLAYERS.BACK_NUMBER, 8)
            .set(SQUAD_PLAYERS.PLAYER_ROLE, "BASIC")
            .set(SQUAD_PLAYERS.CREATED_AT, now)
            .set(SQUAD_PLAYERS.UPDATED_AT, now)
            .execute();

        // When
        Squad retrievedSquad = repository.findByClubId(clubId);

        // Then
        assertThat(retrievedSquad).isNotNull();
        assertThat(retrievedSquad.getSquadPlayers()).hasSize(1);

        SquadPlayer retrievedPlayer = retrievedSquad.getSquadPlayers().get(0);
        assertThat(retrievedPlayer.getName()).isEqualTo(playerName);
        assertThat(retrievedPlayer.getUserId()).isEqualTo(memberId);
    }
}