package org.example.stockradar.global.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    @RestController
    static class TestController {
        @GetMapping("/business")
        void business() {
            throw new BusinessException(ErrorCode.BOARD_NOT_FOUND);
        }

        @GetMapping("/business-custom-message")
        void businessCustomMessage() {
            throw new BusinessException(ErrorCode.NOT_AUTHOR, "작성자만 삭제할 수 있습니다.");
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("내부 정보가 담긴 메시지");
        }

        @PostMapping("/validate")
        void validate(@Valid @RequestBody Body body) {
        }
    }

    record Body(@NotBlank String title) {
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void BusinessException은_ErrorCode의_상태와_코드로_응답한다() throws Exception {
        mockMvc.perform(get("/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.BOARD_NOT_FOUND.getErrorCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.BOARD_NOT_FOUND.getDescription()));
    }

    @Test
    void BusinessException에_지정한_메시지가_그대로_내려간다() throws Exception {
        mockMvc.perform(get("/business-custom-message"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.NOT_AUTHOR.getErrorCode()))
                .andExpect(jsonPath("$.message").value("작성자만 삭제할 수 있습니다."));
    }

    @Test
    void 검증_실패는_400과_필드_메시지() throws Exception {
        mockMvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT.getErrorCode()))
                .andExpect(jsonPath("$.message").value(containsString("title")));
    }

    @Test
    void 예상하지_못한_예외는_500이고_내부_메시지를_노출하지_않는다() throws Exception {
        mockMvc.perform(get("/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INTERNAL_SERVER_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.message").value("예기치 못한 서버 오류가 발생했습니다."));
    }
}
