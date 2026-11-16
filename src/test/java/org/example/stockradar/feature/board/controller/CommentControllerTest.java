package org.example.stockradar.feature.board.controller;

import org.example.stockradar.feature.board.dto.CommentResponseDto;
import org.example.stockradar.feature.board.service.CommentService;
import org.example.stockradar.global.exception.ErrorCode;
import org.example.stockradar.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentControllerTest {

    private final CommentService commentService = mock(CommentService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CommentController(commentService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 댓글_조회는_data에_페이지를_담는다() throws Exception {
        given(commentService.getComments(eq(1L), any()))
                .willReturn(new PageImpl<>(List.of(CommentResponseDto.builder().commentId(7L).content("hi").build()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/comment/read").param("boardId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].commentId").value(7))
                .andExpect(jsonPath("$.data.totalPages").value(1));
    }

    @Test
    void 비로그인_댓글_삭제는_401() throws Exception {
        mockMvc.perform(post("/comment/delete").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commentId\":1,\"password\":\"pw\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.UNAUTHORIZED.getErrorCode()));
    }

    @Test
    void 비밀번호가_틀리면_400_PASSWORD_MISMATCH() throws Exception {
        given(commentService.softDeleteComment(any(), eq("me"))).willReturn(false);

        mockMvc.perform(post("/comment/delete").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commentId\":1,\"password\":\"pw\"}")
                        .principal(new UsernamePasswordAuthenticationToken("me", null, List.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.PASSWORD_MISMATCH.getErrorCode()));
    }

    @Test
    void 댓글_삭제_성공은_message를_담는다() throws Exception {
        given(commentService.softDeleteComment(any(), eq("me"))).willReturn(true);

        mockMvc.perform(post("/comment/delete").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"commentId\":1,\"password\":\"pw\"}")
                        .principal(new UsernamePasswordAuthenticationToken("me", null, List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글 삭제에 성공했습니다."));
    }
}
