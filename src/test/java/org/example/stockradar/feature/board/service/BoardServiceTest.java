package org.example.stockradar.feature.board.service;

import org.example.stockradar.feature.auth.entity.Member;
import org.example.stockradar.feature.auth.entity.Role;
import org.example.stockradar.feature.auth.repository.MemberRepository;
import org.example.stockradar.feature.board.entity.Board;
import org.example.stockradar.feature.board.repository.BoardRepository;
import org.example.stockradar.global.exception.BusinessException;
import org.example.stockradar.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class BoardServiceTest {

    private final BoardRepository boardRepository = mock(BoardRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final BoardService boardService =
            new BoardService(boardRepository, mock(MemberRepository.class), passwordEncoder);

    private Board boardWrittenBy(String memberId) {
        Member author = new Member(memberId, "hashed", "작성자", null, Role.MEMBER);
        return Board.builder().boardId(1L).boardTitle("제목").member(author).build();
    }

    @Test
    void 없는_게시글_삭제는_BOARD_NOT_FOUND() {
        given(boardRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.deleteBoard(1L, "pw", "me@test.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BOARD_NOT_FOUND);
    }

    @Test
    void 작성자가_아니면_NOT_AUTHOR() {
        given(boardRepository.findById(1L)).willReturn(Optional.of(boardWrittenBy("author@test.com")));

        assertThatThrownBy(() -> boardService.deleteBoard(1L, "pw", "other@test.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_AUTHOR);
    }

    @Test
    void 비밀번호가_틀리면_false() {
        given(boardRepository.findById(1L)).willReturn(Optional.of(boardWrittenBy("me@test.com")));
        given(passwordEncoder.matches(any(), any())).willReturn(false);

        assertThat(boardService.deleteBoard(1L, "wrong", "me@test.com")).isFalse();
    }

    @Test
    void 없는_게시글_상세조회는_BOARD_NOT_FOUND() {
        given(boardRepository.findBoardResponseDtoByBoardId(1L)).willReturn(null);

        assertThatThrownBy(() -> boardService.findBoardResponseDtoByBoardId(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BOARD_NOT_FOUND);
    }
}
