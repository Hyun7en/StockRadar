package org.example.stockradar.feature.board.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stockradar.feature.board.dto.BoardDeleteRequestDto;
import org.example.stockradar.feature.board.dto.BoardRequestDto;
import org.example.stockradar.feature.board.dto.BoardResponseDto;
import org.example.stockradar.feature.board.service.BoardService;
import org.example.stockradar.global.exception.BusinessException;
import org.example.stockradar.global.exception.ErrorCode;
import org.example.stockradar.global.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Hyun7en
 */

//게시판은 다양한 방법 써서 구현 해보기
@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * 모든 게시글 목록을 조회하여 모델에 추가한 후, 게시글 목록 페이지로 이동합니다.
     */
    @GetMapping("/list")
    public String getBoardList(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<BoardResponseDto> boardList = boardService.findBoardListDto(pageable);
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    /**
     * 게시글 작성 페이지로 이동합니다.
     */
    @GetMapping("/write")
    public String boardWrite() {
        return "board/write";
    }

    /**
     * 게시글 작성 후 db로 데이터 전송
     */
    @PostMapping("/insert")
    public String boardInsert(@Valid BoardRequestDto boardRequestDto, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증되지 않은 사용자입니다.");
        }

        // 인증된 사용자 ID 가져오기 (예: JWT의 subject를 사용하여 memberId 반환)
        String memberId = String.valueOf(authentication.getName());

        boardService.saveBoard(boardRequestDto,memberId);
        return "redirect:/board/list";
    }


    /**
     * 게시글 상세 페이지로 이동합니다.
     */
    @GetMapping("/detail")
    public String boardDetail(@RequestParam Long boardId, Model model) {
        BoardResponseDto board = boardService.findBoardResponseDtoByBoardId(boardId);
        model.addAttribute("board", board);
        return "board/detail";
    }

    /**
     * 게시글 수정/삭제 폼 페이지로 이동합니다.
     */
    @GetMapping("/form")
    public String boardForm(@RequestParam Long boardId, Model model) {
        BoardResponseDto board = boardService.findBoardResponseDtoByBoardId(boardId);
        model.addAttribute("board", board);
        return "board/form";
    }

    /**
     * 게시글 수정 처리
     */
    @PostMapping("/update")
    public String boardUpdate(@RequestParam Long boardId, @Valid BoardRequestDto boardRequestDto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증되지 않은 사용자입니다.");
        }
        boardService.updateBoard(boardId, boardRequestDto, authentication.getName());
        return "redirect:/board/detail?boardId=" + boardId;
    }

    /**
     * 게시글 삭제 처리
     */
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteBoard(@RequestBody BoardDeleteRequestDto request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "로그인 해주세요.");
        }

        boolean isDeleted = boardService.deleteBoard(request.getBoardId(), request.getPassword(), authentication.getName());
        if (!isDeleted) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH, "비밀번호가 올바르지 않습니다.");
        }

        return ResponseEntity.ok(ApiResponse.ok(Map.of("redirectUrl", "/board/list"), "게시글이 삭제되었습니다."));
    }

    /**
     * 게시글 제목 검색
     */
    @GetMapping("/search")
    public String boardSearch(@RequestParam String keyword,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        // DTO 반환으로 변경 (예: BoardResponseDto 또는 BoardListDto)
        Page<BoardResponseDto> boardList = boardService.searchBoards(keyword, pageable);
        model.addAttribute("boardList", boardList); // "board"로 통일
        model.addAttribute("keyword", keyword);
        return "board/list";
    }

}
