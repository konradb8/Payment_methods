package com.github.konradb8.payment_methods.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String id;
    private BigDecimal value;
    private List<String> promotions;

    public String toString(){
        return   id + ", value: " + value + ", promotions: " + promotions;
    }
}
