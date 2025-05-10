package com.github.konradb8.payment_methods.service;

import com.github.konradb8.payment_methods.model.Method;
import com.github.konradb8.payment_methods.model.Order;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class PaymentMethodService {
    public Map<String, BigDecimal> optimize(Collection<Order> orders, Collection<Method> methods) {
        Map<String, BigDecimal> result = new HashMap<>();
        Map<String, Method> methodMap = new HashMap<>();

        for (Method method : methods) {
            methodMap.put(method.getId(), method);
        }

        List<Order> sortedOrders = orders.stream()
                .map(order -> new AbstractMap.SimpleEntry<>(order, scoreOrder(order)))
                .sorted((a, b) -> b.getValue().compareTo(a.getValue())) // descending
                .map(Map.Entry::getKey)
                .toList();

        for (Order order : sortedOrders) {
            BigDecimal orderVal = order.getValue();
            List<String> orderPromotions = order.getPromotions();


            Method points = methodMap.get("PUNKTY");

            // płacenie punktami
            if (points != null && points.getLimit().compareTo(orderVal) >= 0) {
                BigDecimal reducedOrderVal = applyDiscount(orderVal, points.getDiscount());
                methodMap.put(points.getId(), subtractFromLimit(points, reducedOrderVal)); // Zaktualizowany limit w methodMap
                result.merge(points.getId(), reducedOrderVal, BigDecimal::add);
                continue;
            }

            // szukanie najlepszego rabatu
            if (orderPromotions != null) {
                Optional<Method> promotedMethods = methodMap.values().stream()
                        .filter(m -> orderPromotions.contains(m.getId()))
                        .filter(m -> m.getLimit().compareTo(orderVal) >= 0)
                        .max(Comparator.comparingInt(Method::getDiscount));

                if (promotedMethods.isPresent()) {
                    Method bestMethod = promotedMethods.get();
                    BigDecimal reducedOrderVal = applyDiscount(orderVal, bestMethod.getDiscount());
                    methodMap.put(bestMethod.getId(), subtractFromLimit(bestMethod, reducedOrderVal));
                    result.merge(bestMethod.getId(), reducedOrderVal, BigDecimal::add);
                    continue;
                }
            }


            // częściowo punktami
            if (points != null && points.getLimit().compareTo(orderVal.multiply(BigDecimal.valueOf(0.1))) >= 0) {

                BigDecimal reduced = orderVal.multiply(BigDecimal.valueOf(0.9));
                BigDecimal usedPoints = points.getLimit().min(orderVal);


                subtractFromLimit(points, usedPoints);

                result.merge(points.getId(), usedPoints, BigDecimal::add);

                BigDecimal remain = reduced.subtract(usedPoints);
                Method any = anyMethodWithLimit(methodMap.values(), remain);

                subtractFromLimit(any, remain);
                result.merge(any.getId(), remain, BigDecimal::add);
                continue;
            }

            // brak metod odpowiednich
            Method any = anyMethodWithLimit(methodMap.values(), orderVal);
            methodMap.put(any.getId(), subtractFromLimit(any, orderVal));
            result.merge(any.getId(), orderVal, BigDecimal::add);

        }

        return result;
    }

    private int scoreOrder(Order order) {
        int base = order.getValue().intValue();
        int promoBoost = order.getPromotions() != null ? order.getPromotions().size() * 10 : 0;
        return base + promoBoost;
    }


    // Pomocnicza funkcja – zwraca największą wartość oszczędności przy możliwej metodzie
    private BigDecimal getMaxDiscountValue(Order order, Map<String, Method> methods) {
        BigDecimal value = order.getValue();
        List<String> promos = order.getPromotions();

        return methods.values().stream()
                .filter(m -> promos == null || promos.contains(m.getId()))
                .filter(m -> m.getLimit().compareTo(value) >= 0)
                .map(m -> value.subtract(applyDiscount(value, m.getDiscount())))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private Method anyMethodWithLimit(Collection<Method> methods, BigDecimal remain) {
        return methods.stream()
                .filter(m -> m.getLimit().compareTo(remain) >= 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Brak dostępnej metody płatności dla kwoty: " + remain));
    }

    private Method subtractFromLimit(Method method, BigDecimal amount) {
        method.setLimit(method.getLimit().subtract(amount));
        return method;
    }

    private BigDecimal applyDiscount(BigDecimal value, int discount) {
        return value.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(discount).divide(BigDecimal.valueOf(100))));
    }
}