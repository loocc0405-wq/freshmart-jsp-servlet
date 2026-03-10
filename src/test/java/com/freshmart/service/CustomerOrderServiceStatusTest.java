package com.freshmart.service;

import com.freshmart.enums.OrderStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class CustomerOrderServiceStatusTest {

    @Test
    void parseStatus_shouldAcceptCanceled() throws Exception {
        CustomerOrderService service = new CustomerOrderService();
        Method method = CustomerOrderService.class.getDeclaredMethod("parseStatus", String.class);
        method.setAccessible(true);

        OrderStatus result = (OrderStatus) method.invoke(service, "CANCELED");

        assertEquals(OrderStatus.CANCELED, result);
    }

    @Test
    void parseStatus_shouldAcceptCancelledAndMapToCanceled() throws Exception {
        CustomerOrderService service = new CustomerOrderService();
        Method method = CustomerOrderService.class.getDeclaredMethod("parseStatus", String.class);
        method.setAccessible(true);

        OrderStatus result = (OrderStatus) method.invoke(service, "CANCELLED");

        assertEquals(OrderStatus.CANCELED, result);
    }

    @Test
    void parseStatus_shouldIgnoreCaseAndSpaces() throws Exception {
        CustomerOrderService service = new CustomerOrderService();
        Method method = CustomerOrderService.class.getDeclaredMethod("parseStatus", String.class);
        method.setAccessible(true);

        OrderStatus result = (OrderStatus) method.invoke(service, "  completed  ");

        assertEquals(OrderStatus.COMPLETED, result);
    }

    @Test
    void parseStatus_shouldReturnNullForBlank() throws Exception {
        CustomerOrderService service = new CustomerOrderService();
        Method method = CustomerOrderService.class.getDeclaredMethod("parseStatus", String.class);
        method.setAccessible(true);

        OrderStatus result = (OrderStatus) method.invoke(service, "   ");

        assertNull(result);
    }

    @Test
    void parseStatus_shouldReturnNullForInvalidStatus() throws Exception {
        CustomerOrderService service = new CustomerOrderService();
        Method method = CustomerOrderService.class.getDeclaredMethod("parseStatus", String.class);
        method.setAccessible(true);

        OrderStatus result = (OrderStatus) method.invoke(service, "INVALID_STATUS");

        assertNull(result);
    }
}