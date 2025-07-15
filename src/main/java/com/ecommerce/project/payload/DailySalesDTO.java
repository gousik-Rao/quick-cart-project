package com.ecommerce.project.payload;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DailySalesDTO {
    private Long id;
    private Long sellerId;
    private LocalDate date;
    private Integer soldCount = 0;
    private Double totalEarned = 0.0;
}
