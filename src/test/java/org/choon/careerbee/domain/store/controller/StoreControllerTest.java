package org.choon.careerbee.domain.store.controller;

import static org.choon.careerbee.fixture.MemberFixture.createMember;
import static org.choon.careerbee.fixture.ticket.TicketFixture.createTicket;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.choon.careerbee.domain.auth.entity.enums.TokenType;
import org.choon.careerbee.domain.member.entity.Member;
import org.choon.careerbee.domain.member.repository.MemberRepository;
import org.choon.careerbee.domain.store.domain.enums.TicketType;
import org.choon.careerbee.domain.store.dto.request.TicketPurchaseReq;
import org.choon.careerbee.domain.store.repository.TicketRepository;
import org.choon.careerbee.util.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("티켓 구매 API - 정상적으로 티켓 구매가 처리된다")
    void purchaseTicket_success() throws Exception {
        // given
        Member member = memberRepository.save(
            createMember("buyer", "buyer@test.com", 10_000L)
        );
        member.plusPoint(1000);
        ticketRepository.save(createTicket(10, 10, "red.png", TicketType.RED));

        String token = "Bearer " + jwtUtil.createToken(member.getId(), TokenType.ACCESS_TOKEN);
        TicketPurchaseReq ticketPurchaseReq = new TicketPurchaseReq(TicketType.RED);

        // when & then
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/v1/tickets")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(ticketPurchaseReq)))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.httpStatusCode").value(204))
            .andExpect(jsonPath("$.message").value("티켓 구매에 성공하였습니다."))
            .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("티켓 수량 조회 API가 정상적으로 티켓 수량을 반환한다")
    void fetchTicketQuantity_success() throws Exception {
        // given
        ticketRepository.saveAllAndFlush(
            List.of(
                createTicket(1000, 3, "red.png", TicketType.RED),
                createTicket(1200, 5, "green.png", TicketType.GREEN),
                createTicket(1500, 7, "blue.png", TicketType.BLUE)
            )
        );

        // when & then
        mockMvc.perform(get("/api/v1/tickets")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("티켓 수량 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.redCount").value(3))
            .andExpect(jsonPath("$.data.greenCount").value(5))
            .andExpect(jsonPath("$.data.blueCount").value(7));
    }


    @Test
    @DisplayName("회원 티켓 수량 조회 API - 티켓 구매 이력이 없을 경우 0으로 반환")
    void fetchMemberTicketQuantity_noPurchaseHistory_returnsZero() throws Exception {
        // given
        Member member = memberRepository.save(createMember("member2", "nopurchase@test.com", 2L));
        String token = "Bearer " + jwtUtil.createToken(member.getId(), TokenType.ACCESS_TOKEN);

        // when & then
        mockMvc.perform(get("/api/v1/members/tickets")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("회원 티켓 수량 조회에 성공하였습니다."))
            .andExpect(jsonPath("$.data.redCount").value(0))
            .andExpect(jsonPath("$.data.greenCount").value(0))
            .andExpect(jsonPath("$.data.blueCount").value(0));
    }
}
