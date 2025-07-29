package com.ecommerce.project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByCategoryOrderByPriceAsc(Category category, Pageable pageDetails);

    @Query("SELECT p FROM Product p where LOWER(p.productName) LIKE LOWER( CONCAT('%', :name, '%'))")
    Page<Product> searchProductByApproxName(@Param("names") String productName, Pageable pageDetails);

    Page<Product> findAllByUser(Specification<Product> specification, Pageable pageDetails, User currUser);
}
