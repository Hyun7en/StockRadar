package org.example.stockradar.feature.board.service;

import org.example.stockradar.feature.auth.entity.Member;
import org.example.stockradar.feature.auth.entity.Role;
import org.example.stockradar.feature.auth.repository.MemberRepository;
import org.example.stockradar.feature.board.dto.CommentDeleteRequestDto;
import org.example.stockradar.feature.board.dto.CommentRequestDto;
import org.example.stockradar.feature.board.entity.Comments;
import org.example.stockradar.feature.board.repository.BoardRepository;
import org.example.stockradar.feature.board.repository.CommentRepository;
import org.example.stockradar.global.exception.BusinessException;
import org.example.stockradar.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class CommentServiceTest {

    private final CommentRepository commentRepository = mock(CommentRepository.class);
    private final BoardRepository boardRepository = mock(BoardRepository.class);
    private final CommentService commentService = new CommentService(
            mock(PasswordEncoder.class), commentRepository, boardRepository, mock(MemberRepository.class));

    private CommentDeleteRequestDto deleteRequest(long commentId) {
        return CommentDeleteRequestDto.builder().commentId(commentId).password("pw").build();
    }

    @Test
    void 없는_댓글_삭제는_COMMENT_NOT_FOUND() {
        given(commentRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.softDeleteComment(deleteRequest(1L), "me@test.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    void 작성자가_아니면_NOT_AUTHOR() {
        Member author = new Member("author@test.com", "hashed", "작성자", null, Role.MEMBER);
        given(commentRepository.findById(1L))
                .willReturn(Optional.of(Comments.builder().commentId(1L).member(author).build()));

        assertThatThrownBy(() -> commentService.softDeleteComment(deleteRequest(1L), "other@test.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_AUTHOR);
    }

    @Test
    void 없는_게시글에_댓글_작성은_BOARD_NOT_FOUND() {
        given(boardRepository.findById(9L)).willReturn(Optional.empty());
        CommentRequestDto request = CommentRequestDto.builder().boardId(9L).content("내용").build();

        assertThatThrownBy(() -> commentService.insertComment(request, "me@test.com"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BOARD_NOT_FOUND);
    }
}
