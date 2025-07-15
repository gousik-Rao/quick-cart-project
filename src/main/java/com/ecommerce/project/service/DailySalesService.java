package com.ecommerce.project.service;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.SalesResponse;
import org.springframework.stereotype.Service;

@Service
public interface DailySalesService {
    void updateCreateSales(int quantity, double totalEarned, User seller);

    SalesResponse getSales(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
}
