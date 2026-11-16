package org.example.stockradar.feature.board.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stockradar.feature.board.dto.CommentDeleteRequestDto;
import org.example.stockradar.feature.board.dto.CommentRequestDto;
import org.example.stockradar.feature.board.dto.CommentResponseDto;
import org.example.stockradar.feature.board.service.CommentService;
import org.example.stockradar.global.exception.BusinessException;
import org.example.stockradar.global.exception.ErrorCode;
import org.example.stockradar.global.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


/**
 * @author Hyun7en
 */

//RestController 사용해서 해보기
@Slf4j
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 삽입 (Insert)
    @PostMapping("/insert")
    public ResponseEntity<ApiResponse<CommentRequestDto>> insertComment(@RequestBody CommentRequestDto commentDto, Authentication authentication) {
        log.info("Insert comment request: {}", commentDto);
        String memberId = requireLogin(authentication);
        CommentRequestDto savedComment = commentService.insertComment(commentDto, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(savedComment));
    }

    // 댓글 목록 조회 (페이징 처리 포함)
    @GetMapping("/read")
    public ResponseEntity<ApiResponse<Page<CommentResponseDto>>> getComments(@RequestParam Long boardId, @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(commentService.getComments(boardId, pageable)));
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> softDeleteComment(@RequestBody CommentDeleteRequestDto request, Authentication authentication) {
        String memberId = requireLogin(authentication);
        log.info("Delete comment request: commentId={}", request.getCommentId());
        if (!commentService.softDeleteComment(request, memberId)) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }
        return ResponseEntity.ok(ApiResponse.ok(null, "댓글 삭제에 성공했습니다."));
    }

    private String requireLogin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인 해주세요.");
        }
        return authentication.getName();
    }
}
