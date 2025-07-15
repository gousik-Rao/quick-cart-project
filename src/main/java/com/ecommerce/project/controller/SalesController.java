package com.ecommerce.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.SalesResponse;
import com.ecommerce.project.service.DailySalesService;

@RestController
@RequestMapping("/api")
public class SalesController {

    private final DailySalesService dailySalesService;

    public SalesController(DailySalesService dailySalesService) {
        this.dailySalesService = dailySalesService;
    }

    @GetMapping("/seller/sales")
    public ResponseEntity<SalesResponse> getUserSales(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_SALES_BY, required = false) String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR, required = false)String sortOrder
    ){
        SalesResponse salesResponse = dailySalesService.getSales(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(salesResponse, HttpStatus.OK);
    }


}
