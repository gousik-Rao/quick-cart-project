package com.ecommerce.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderDTOResponse;
import com.ecommerce.project.payload.OrderRequestDTO;
import com.ecommerce.project.payload.StripePaymentDTO;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.StripeService;
import com.ecommerce.project.util.AuthUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final AuthUtil authUtil;
    private final OrderService orderService;
    private final StripeService stripeService;

    public OrderController(AuthUtil authUtil, OrderService orderService, StripeService stripeService) {
        this.authUtil = authUtil;
        this.orderService = orderService;
        this.stripeService = stripeService;
    }

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod,
                                                  @RequestBody OrderRequestDTO orderRequestDTO){
        String emailId = authUtil.loggedInEmail();
        System.out.println("orderRequestDTo"+ orderRequestDTO);
        OrderDTO order = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping("/order/stripe-client-secret")
    public ResponseEntity<String> createStripeClientSecret(@RequestBody StripePaymentDTO stripePaymentDto) throws StripeException {
        PaymentIntent paymentIntent = stripeService.paymentIntent(stripePaymentDto);
        return new ResponseEntity<>(paymentIntent.getClientSecret(), HttpStatus.CREATED);
    }

    @GetMapping("/orders")
    public ResponseEntity<OrderDTOResponse> getUserOrders(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_DIR, required = false)String sortOrder){
         OrderDTOResponse orderDTOResponse = orderService.getUserOrders(pageNumber, pageSize, sortOrder);
         return new ResponseEntity<>(orderDTOResponse,HttpStatus.OK);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long orderId){
        OrderDTO orderDTO = orderService.getOrderById(orderId);
        return new ResponseEntity<>(orderDTO,HttpStatus.OK);
    }
}
