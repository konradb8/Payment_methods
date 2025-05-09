package com.github.konradb8.payment_methods.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Method {
    private String id;
    private Integer discount;
    private BigDecimal limit;

    public String toString(){
        return id + ", discount: " + discount + ", limit: " + limit;
    }
}
