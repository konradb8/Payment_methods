package com.github.konradb8.payment_methods.test;

import com.github.konradb8.payment_methods.model.Method;
import com.github.konradb8.payment_methods.model.Order;
import com.github.konradb8.payment_methods.service.PaymentMethodService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodServiceTest {


    private final PaymentMethodService service = new PaymentMethodService();

    private Method m(String id, int discount, double limit) {
        return new Method(id, discount, BigDecimal.valueOf(limit));
    }

    private Order o(String id, double value, String... promotions) {
        return new Order(id, BigDecimal.valueOf(value), promotions.length > 0 ? Arrays.asList(promotions) : null);
    }

    private void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertNotNull(actual, "Wynik był null!");
        assertEquals(new BigDecimal(String.valueOf(expected)).setScale(2, RoundingMode.HALF_UP),
                actual.setScale(2, RoundingMode.HALF_UP));
    }



    @Test
    public void testOptimize_PromotionsAndPoints() {
        List<Method> methods = Arrays.asList(
                m("PUNKTY", 15, 100.0),
                m("mZysk", 10, 180.0),
                m("BosBankrut", 5, 200.0)
        );

        List<Order> orders = Arrays.asList(
                o("ORDER1", 100.0, "mZysk"),
                o("ORDER2", 200.0, "BosBankrut"),
                o("ORDER3", 150.0, "mZysk", "BosBankrut"),
                o("ORDER4", 50.0)
        );

        Map<String, BigDecimal> result = service.optimize(orders, methods);

        assertEquals(3, result.size());
        assertBigDecimalEquals(new BigDecimal("165.00"), result.get("mZysk"));
        assertBigDecimalEquals(new BigDecimal("190.00"), result.get("BosBankrut"));
        assertBigDecimalEquals(new BigDecimal("100.00"), result.get("PUNKTY"));
    }

    @Test
    void testOptimize_promotedMethod() {
        PaymentMethodService service = new PaymentMethodService();

        List<Method> methods = Arrays.asList(
                new Method("mZysk", 10, new BigDecimal("200.00")),
                new Method("BosBankrut", 5, new BigDecimal("100.00"))
        );

        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), List.of("mZysk"))
        );

        Map<String, BigDecimal> result = service.optimize(orders, methods);

        assertBigDecimalEquals(new BigDecimal("90.00"), result.get("mZysk"));
        assertEquals(1, result.size());
    }

    @Test
    void testOptimize_payWithPointsOnly() {
        PaymentMethodService service = new PaymentMethodService();

        List<Method> methods = List.of(
                new Method("PUNKTY", 15, new BigDecimal("150.00"))
        );

        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), List.of())
        );

        Map<String, BigDecimal> result = service.optimize(orders, methods);

        assertBigDecimalEquals(new BigDecimal("85.00"), result.get("PUNKTY"));
    }

    @Test
    void testOptimize_partialPointsAndCard() {
        PaymentMethodService service = new PaymentMethodService();

        List<Method> methods = Arrays.asList(
                new Method("PUNKTY", 15, new BigDecimal("10.00")),
                new Method("BosBankrut", 5, new BigDecimal("200.00"))
        );

        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), List.of())
        );

        Map<String, BigDecimal> result = service.optimize(orders, methods);

        assertBigDecimalEquals(new BigDecimal("10.00"), result.get("PUNKTY"));
        assertBigDecimalEquals(new BigDecimal("80.00"), result.get("BosBankrut"));
    }

    @Test
    void testOptimize_fallbackToAnyMethod() {
        PaymentMethodService service = new PaymentMethodService();

        List<Method> methods = List.of(
                new Method("BosBankrut", 0, new BigDecimal("300.00"))
        );

        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), null)
        );

        Map<String, BigDecimal> result = service.optimize(orders, methods);

        assertBigDecimalEquals(new BigDecimal("100.00"), result.get("BosBankrut"));
    }

    @Test
    void testOptimize_throwWhenNoMethodAvailable() {
        PaymentMethodService service = new PaymentMethodService();

        List<Method> methods = List.of(
                new Method("BosBankrut", 0, new BigDecimal("50.00"))
        );

        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.0000"), null)
        );

        assertThrows(IllegalStateException.class, () -> service.optimize(orders, methods));
    }

    // validation
    @Test
    void shouldThrowWhenNoMethodCanPay() {
        Order order = new Order("ORDER1",new BigDecimal("1000"), List.of());
        Method low = new Method("LOW", 100, new BigDecimal(0));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.optimize(List.of(order), List.of(low))
        );

        assertTrue(ex.getMessage().contains("Brak dostępnej metody"));
    }
    @Test
    void shouldHandleNullPromotions() {
        Order order = new Order("ORDER1",new BigDecimal("100"), null);
        Method fallback = new Method("FALLBACK", 0, new BigDecimal("200"));

        Map<String, BigDecimal> result = service.optimize(List.of(order), List.of(fallback));

        assertBigDecimalEquals(new BigDecimal("100.00"), result.get("FALLBACK"));
    }


}

