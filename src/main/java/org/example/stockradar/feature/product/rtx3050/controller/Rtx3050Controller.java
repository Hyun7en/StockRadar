package org.example.stockradar.feature.product.rtx3050.controller;


import lombok.RequiredArgsConstructor;
import org.example.stockradar.feature.crawl.service.ProductStockService;
import org.example.stockradar.feature.product.dto.ProductResponseDto;
import org.example.stockradar.feature.product.service.ProductALLServiceWithCache;
import org.example.stockradar.feature.product.service.ProductStockServiceWithCache;
import org.example.stockradar.global.exception.BusinessException;
import org.example.stockradar.global.exception.ErrorCode;
import org.example.stockradar.global.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("product")
@RequiredArgsConstructor
public class Rtx3050Controller {
    private static final Logger logger = LoggerFactory.getLogger(Rtx3050Controller.class);

    private final ProductStockService productStockService;
    private final ProductStockServiceWithCache productServicewithCache;
    private final ProductALLServiceWithCache productALLServiceWithCache;

    @GetMapping("gpu/rtx3050")
    public String rtx3050() {
        return "product/gpu/rtx3050";
    }

    //시나리오 1
//        @GetMapping("api/gpu/rtx3050")
//    public ResponseEntity<?> gpuRtx3050Api() {
//        logger.info("RTX 3050 API 요청");
//
//        try {
//            // 모든 제품 정보 가져오기
//            List<ProductResponseDto> allProducts = productStockService.getProductsWithStockAndPrice();
//
//            // RTX 3050 제품만 필터링
//            List<ProductResponseDto> rtx3050Products = allProducts.stream()
//                    .filter(product -> product.getProductName() != null &&
//                            (product.getProductName().toLowerCase().contains("rtx 3050") ||
//                                    product.getProductName().toLowerCase().contains("rtx3050")))
//                    .collect(Collectors.toList());
//
//            if (rtx3050Products.isEmpty()) {
//                logger.warn("RTX 3050 제품을 찾을 수 없습니다.");
//                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
//
//            }
//
//            logger.info("RTX 3050 제품 {} 개를 찾았습니다.", rtx3050Products.size());
//
//            // 성공 응답 DTO 생성
//            ProductResponseDto responseDto = ProductResponseDto.builder()
//                    .status(200)
//                    .message("RTX 3050 제품 조회 성공")
//                    .data(rtx3050Products) // ProductResponseDto에 data 필드 추가 필요
//                    .build();
//
//            return ResponseEntity.ok(responseDto);
//
//        } catch (Exception e) {
//            logger.error("RTX 3050 제품 조회 중 오류 발생: {}", e.getMessage(), e);
//            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
//
//        }
//        return null;
//    }

    //시나리오 2
//    @GetMapping("api/gpu/rtx3050")
//    public ResponseEntity<?> gpuRtx3050Api() {
//        logger.info("RTX 3050 API 요청");
//
//        try {
//            // 모든 제품 정보 가져오기 (캐시 활용)
//            List<ProductResponseDto> allProducts = productServicewithCache.getAllProductsWithCachedStock();
//
//            // RTX 3050 제품만 필터링
//            List<ProductResponseDto> rtx3050Products = allProducts.stream()
//                    .filter(product -> product.getProductName() != null &&
//                            (product.getProductName().toLowerCase().contains("rtx 3050") ||
//                                    product.getProductName().toLowerCase().contains("rtx3050")))
//                    .collect(Collectors.toList());
//
//            if (rtx3050Products.isEmpty()) {
//                logger.warn("RTX 3050 제품을 찾을 수 없습니다.");
//                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
//            }
//
//            logger.info("RTX 3050 제품 {} 개를 찾았습니다.", rtx3050Products.size());
//
//            // 성공 응답 DTO 생성
//            ProductResponseDto responseDto = ProductResponseDto.builder()
//                    .status(200)
//                    .message("RTX 3050 제품 조회 성공")
//                    .data(rtx3050Products)
//                    .build();
//
//            return ResponseEntity.ok(responseDto);
//
//        } catch (Exception e) {
//            logger.error("RTX 3050 제품 조회 중 오류 발생: {}", e.getMessage(), e);
//            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
//        }
//        return null;
//    }
//
    // 시나리오 3, 4, 5를 위한 새로운 엔드포인트
    @GetMapping("api/gpu/rtx3050")
    public ResponseEntity<ApiResponse<List<ProductResponseDto>>> gpuRtx3050CachedAllApi() {
        List<ProductResponseDto> products = productALLServiceWithCache.getProductsByKeyword("rtx 3050");
        if (products.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

}



