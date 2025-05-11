package com.github.konradb8.payment_methods;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.konradb8.payment_methods.model.Method;
import com.github.konradb8.payment_methods.model.Order;
import com.github.konradb8.payment_methods.service.PaymentMethodService;
import com.github.konradb8.payment_methods.util.JsonLoader;


import java.math.BigDecimal;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {

        if(args.length < 2) {
            System.err.println("Należy przekazać 2 pliki json");
            return;
        }

        String ordersPath = args[0];
        String methodsPath = args[1];

        Collection<Order> orders = JsonLoader.loadJson(ordersPath, new TypeReference<>() {
        });
        Collection<Method> methods = JsonLoader.loadJson(methodsPath, new TypeReference<>() {
        });

        PaymentMethodService paymentMethodService = new PaymentMethodService();
        try {
            Map<String, BigDecimal> result = paymentMethodService.optimize(orders, methods);
            result.forEach((k, v) -> System.out.println(k + ": " + v));
        }catch(Exception e) {
            System.out.println("Błąd: " + e.getMessage() );
        }




    }
}