package com.github.konradb8.payment_methods;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.konradb8.payment_methods.model.Method;
import com.github.konradb8.payment_methods.model.Order;
import com.github.konradb8.payment_methods.service.PaymentMethodService;
import com.github.konradb8.payment_methods.util.JsonLoader;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {

        if(args.length < 2) {
            System.err.println("To start application 2 json files are needed");
            return;
        }

        String ordersPath = args[0];
        String methodsPath = args[1];

        List<Order> orders = JsonLoader.loadJson(ordersPath, new TypeReference<List<Order>>() {});
        List<Method> methods = JsonLoader.loadJson(methodsPath, new TypeReference<List<Method>>() {});

//        System.out.println("Orders:");
//        orders.forEach(System.out::println);
//        System.out.println("Methods:");
//        methods.forEach(System.out::println);

        PaymentMethodService paymentMethodService = new PaymentMethodService();
        Map<String, BigDecimal> result = paymentMethodService.optimize(orders, methods);

        result.forEach((k, v) -> System.out.println(k + ": " + v));



    }
}