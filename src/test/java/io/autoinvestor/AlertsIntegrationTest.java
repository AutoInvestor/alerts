package io.autoinvestor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.autoinvestor.application.*;
import io.autoinvestor.domain.model.Decision;
import io.autoinvestor.domain.model.UserId;
import io.autoinvestor.infrastructure.read_models.alerts.InMemoryAlertsReadModelRepository;
import io.autoinvestor.infrastructure.read_models.users.InMemoryInboxReadModelRepository;
import io.autoinvestor.infrastructure.repositories.portfolio.InMemoryPortfolioRepository;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AlertsIntegrationTest {

    @Autowired private RegisterUserCommandHandler registerUserHandler;

    @Autowired private RegisterPortfolioAssetCommandHandler registerPortfolioAssetHandler;

    @Autowired private EmitAlertsCommandHandler emitAlertsHandler;

    @Autowired private GetAlertsQueryHandler getAlertsQueryHandler;

    @Autowired private InMemoryPortfolioRepository portfolioRepository;

    @Autowired private InMemoryInboxReadModelRepository inboxReadModel;

    @Autowired private InMemoryAlertsReadModelRepository alertsReadModel;

    @Autowired private MockMvc mockMvc;

    @BeforeEach
    void resetState() {
        inboxReadModel.clear();
        alertsReadModel.clear();
        portfolioRepository.clear();
    }

    @Test
    void registerUserHandler_shouldCreateInboxAndPortfolioUser() {
        // GIVEN: a brand-new userId that does not exist yet
        String userId = "user-1";
        int riskLevel = 10;

        // PRECONDITION: inboxReadModel has no mapping for "user-1"
        assertThat(inboxReadModel.getInboxId(UserId.from(userId))).isEmpty();

        // WHEN: we invoke the RegisterUserCommandHandler
        RegisterUserCommand cmd = new RegisterUserCommand(userId, riskLevel);
        registerUserHandler.handle(cmd);

        // THEN: the InMemoryInboxReadModelRepository should now contain an inboxId for "user-1"
        assertThat(inboxReadModel.getInboxId(UserId.from(userId))).isPresent();

        // AND: the InMemoryPortfolioRepository should have registered the user internally
        // We know that existsPortfolioAsset returns false for any asset because none added yet,
        // so simply verify that getUsersIdByAssetAndRiskLevel yields an empty list for an arbitrary
        // asset.
        List<UserId> usersForBtcRisk10 =
                portfolioRepository.getUsersIdByAssetAndRiskLevel("BTC", 10);
        assertThat(usersForBtcRisk10).isEmpty();
    }

    @Test
    void registerPortfolioAssetHandler_shouldAttachAssetToExistingUser() {
        String userId = "user-2";
        int riskLevel = 5;
        String assetId = "AAPL";

        // First, register the user so that an inbox exists and portfolioRepository has that user
        registerUserHandler.handle(new RegisterUserCommand(userId, riskLevel));

        // PRECONDITION: this user does not yet have "AAPL" in their portfolio
        boolean before = portfolioRepository.existsPortfolioAsset(userId, assetId);
        assertThat(before).isFalse();

        // WHEN: we add the portfolio asset
        RegisterPortfolioAssetCommand addCmd = new RegisterPortfolioAssetCommand(userId, assetId);
        registerPortfolioAssetHandler.handle(addCmd);

        // THEN: the portfolioRepository should reflect that "user-2" now has AAPL
        boolean after = portfolioRepository.existsPortfolioAsset(userId, assetId);
        assertThat(after).isTrue();

        // AND: getUsersIdByAssetAndRiskLevel("AAPL", 5) should return exactly user-2
        List<UserId> matched =
                portfolioRepository.getUsersIdByAssetAndRiskLevel(assetId, riskLevel);
        assertThat(matched).hasSize(1).allMatch(uid -> uid.value().equals(userId));
    }

    @Test
    void emitAlertsHandler_shouldCreateAndStoreAlertForMatchingUsers() {
        String userId = "user-3";
        int riskLevel = 3;
        String assetId = "GOOG";
        Decision decision = Decision.BUY;

        // 1) register the user:
        registerUserHandler.handle(new RegisterUserCommand(userId, riskLevel));

        // 2) attach "GOOG" to user-3's portfolio:
        registerPortfolioAssetHandler.handle(new RegisterPortfolioAssetCommand(userId, assetId));

        // PRECONDITION: alertsReadModel.get(userId) is empty
        List<AlertDTO> beforeAlerts = alertsReadModel.get(userId);
        assertThat(beforeAlerts).isEmpty();

        // WHEN: we emit an alert for asset "GOOG" at riskLevel=3 with decision=BUY
        EmitAlertsCommand emitCmd = new EmitAlertsCommand(assetId, decision.name(), riskLevel);
        emitAlertsHandler.handle(emitCmd);

        // THEN: alertsReadModel.get(userId) should contain exactly one AlertDTO
        List<AlertDTO> afterAlerts = alertsReadModel.get(userId);
        assertThat(afterAlerts).hasSize(1);

        AlertDTO alertDto = afterAlerts.getFirst();
        assertThat(alertDto.userId()).isEqualTo(userId);
        assertThat(alertDto.assetId()).isEqualTo(assetId);
        assertThat(alertDto.type()).isEqualTo(decision.name());
        assertThat(alertDto.date()).isNotNull();
    }

    @Test
    void getAlertsQueryHandler_shouldReturnAllSavedAlertsForUser() {
        String userId = "user-4";
        int riskLevel = 2;
        String assetId1 = "MSFT";
        String assetId2 = "TSLA";
        Decision decision1 = Decision.SELL;
        Decision decision2 = Decision.HOLD;

        // Setup: register user, attach two assets, emit two alerts
        registerUserHandler.handle(new RegisterUserCommand(userId, riskLevel));
        registerPortfolioAssetHandler.handle(new RegisterPortfolioAssetCommand(userId, assetId1));
        registerPortfolioAssetHandler.handle(new RegisterPortfolioAssetCommand(userId, assetId2));

        // Emit first alert (MSFT, SELL)
        emitAlertsHandler.handle(new EmitAlertsCommand(assetId1, decision1.name(), riskLevel));

        // Emit second alert (TSLA, HOLD)
        emitAlertsHandler.handle(new EmitAlertsCommand(assetId2, decision2.name(), riskLevel));

        // WHEN: we query via GetAlertsQueryHandler
        GetDecisionsQuery query = new GetDecisionsQuery(userId);
        List<GetAlertsQueryResponse> responses = getAlertsQueryHandler.handle(query);

        // THEN: We should see exactly two entries, one for MSFT and one for TSLA (in insertion
        // order)
        assertThat(responses).hasSize(2);

        // Order is not strictly guaranteed, so verify that the set of (assetId, type) matches
        assertThat(responses)
                .extracting(GetAlertsQueryResponse::assetId, GetAlertsQueryResponse::type)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(assetId1, decision1.name()),
                        org.assertj.core.groups.Tuple.tuple(assetId2, decision2.name()));
    }

    @Test
    void getAlertsController_endpointShouldReturnJsonListOfAlerts() throws Exception {
        String userId = "user-5";
        int riskLevel = 7;
        String assetId = "NFLX";
        Decision decision = Decision.SELL;

        // 1) Register user & portfolio & emit one alert (same setup as above)
        registerUserHandler.handle(new RegisterUserCommand(userId, riskLevel));
        registerPortfolioAssetHandler.handle(new RegisterPortfolioAssetCommand(userId, assetId));
        emitAlertsHandler.handle(new EmitAlertsCommand(assetId, decision.name(), riskLevel));

        // 2) Perform an HTTP GET against /alerts with X-User-Id=user-5
        mockMvc.perform(
                        get("/alerts")
                                .header("X-User-Id", userId)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Expect a JSON array of size 1
                .andExpect(jsonPath("$.length()").value(1))
                // The first (and only) element should have "assetId": "NFLX" and "type": "SELL"
                .andExpect(jsonPath("$[0].assetId").value(assetId))
                .andExpect(jsonPath("$[0].type").value(decision.name()));
    }
}
