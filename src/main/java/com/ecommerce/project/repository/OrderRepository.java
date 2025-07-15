package com.ecommerce.project.repository;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.Order;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {


    Page<Order> findAllByEmail(@Email String email, Pageable pageable);

    List<Order> findByAddress(Address address);
}
