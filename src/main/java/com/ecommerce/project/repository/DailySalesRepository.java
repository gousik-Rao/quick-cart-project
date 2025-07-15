package com.ecommerce.project.repository;

import com.ecommerce.project.model.DailySales;
import com.ecommerce.project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailySalesRepository extends JpaRepository<DailySales, Long> {
    List<DailySales> findByDateAndSeller(LocalDate date, User seller);

    Page<DailySales> findAllBySeller(User seller, Pageable pageable);
}
