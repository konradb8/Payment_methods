package com.github.konradb8.payment_methods.service;

import com.github.konradb8.payment_methods.model.Method;
import com.github.konradb8.payment_methods.model.Order;

import java.math.BigDecimal;
import java.util.*;

public class PaymentMethodService {
    public Map<String, BigDecimal> optimize(List<Order> orders, List<Method> methods) {
        Map<String, BigDecimal> result = new HashMap<>();
        Map<String, Method> methodMap = new HashMap<>();

        for (Method method : methods) {
            methodMap.put(method.getId(), method);
        }

        for (Order order : orders) {
            BigDecimal orderVal = order.getValue();
            List<String> orderPromotions = order.getPromotions();


            Method points = methodMap.get("PUNKTY");

            // Płacenie punktami
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
                    methodMap.put(bestMethod.getId(), subtractFromLimit(bestMethod, reducedOrderVal)); // Zaktualizowany limit w methodMap
                    result.merge(bestMethod.getId(), reducedOrderVal, BigDecimal::add);
                    continue;
                }
            }


            // częściowo punktami
            if (points != null && points.getLimit().compareTo(orderVal.multiply(BigDecimal.valueOf(0.1))) >= 0) {

                BigDecimal usedPoints = points.getLimit().min(orderVal);
                BigDecimal reduced = orderVal.multiply(BigDecimal.valueOf(0.9));

                subtractFromLimit(points, usedPoints); // points = subtractFromLimit(points, usedPoints);
                methodMap.put(points.getId(), points);

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



    private Method anyMethodWithLimit(Collection<Method> methods, BigDecimal remain) {
        return methods.stream()
                .filter(m -> m.getLimit().compareTo(remain) >= 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Brak dostępnej metody płatności dla kwoty: " + remain));
    }


    private Method subtractFromLimit(Method method, BigDecimal amount) {
        BigDecimal newLimit = method.getLimit().subtract(amount);
        method.setLimit(newLimit);
        return method;

    }

    private BigDecimal applyDiscount(BigDecimal value, int discount) {
        return value.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(discount).divide(BigDecimal.valueOf(100))));
    }
}
