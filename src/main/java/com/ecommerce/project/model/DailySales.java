package com.ecommerce.project.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_sales", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"seller_id", "date"})
})
@Data
@NoArgsConstructor
@Getter
@Setter
public class DailySales {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_sales_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "sold_count", nullable = false)
    private Integer soldCount = 0;

    @Column(name = "total_earned", nullable = false)
    private Double totalEarned = 0.0;

    public DailySales(User seller, LocalDate date) {
        this.seller = seller;
        this.date = date;
    }
}
