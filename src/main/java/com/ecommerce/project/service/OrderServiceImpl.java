package com.ecommerce.project.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Order;
import com.ecommerce.project.model.OrderItem;
import com.ecommerce.project.model.Payment;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderDTOResponse;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.OrderItemRepository;
import com.ecommerce.project.repository.OrderRepository;
import com.ecommerce.project.repository.PaymentRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.util.AuthUtil;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService{
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;
    private final AuthUtil authUtil;
    private final DailySalesService dailySalesService;

    public OrderServiceImpl(CartRepository cartRepository, AddressRepository addressRepository, OrderRepository orderRepository, PaymentRepository paymentRepository, OrderItemRepository orderItemRepository, ProductRepository productRepository, CartService cartService, ModelMapper modelMapper, AuthUtil authUtil, DailySalesService dailySalesService) {
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
        this.dailySalesService = dailySalesService;
    }

    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        Cart cart = cartRepository.findCartByEmail(emailId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart","email",emailId);
        }
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted.");
        order.setAddress(address);

        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        List<CartItem> cartItems = cart.getCartItems();
        if(cartItems.isEmpty()){
            throw new APIException("Cart is empty");
        }

        System.out.println("cartItems"+ cartItems.size());
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);
        order.setOrderItems(orderItems);

        List<CartItem> cartItemsCopy = new ArrayList<>(cart.getCartItems());
        cartItemsCopy.forEach(item -> {
            int quantity = item.getQuantity();
            Product product = item.getProduct();

            if(product.getSoldCount() == null)
                product.setSoldCount(0L);

            product.setQuantity(product.getQuantity() - quantity);
            product.setSoldCount(product.getSoldCount() + quantity);

            User seller = product.getUser();
            if(seller != null){
                if(seller.getTotalEarnings() == null){
                    seller.setTotalEarnings(0.);
                }
                if(seller.getBalance() == null){
                    seller.setBalance(0.);
                }
                seller.setTotalEarnings(seller.getTotalEarnings() + (product.getPrice() * quantity));
                seller.setBalance(seller.getBalance() + (product.getPrice() * quantity));
                dailySalesService.updateCreateSales(quantity, product.getSpecialPrice() * quantity, seller);
            }

            productRepository.save(product);

            cartService.deleteProductFromCart(cart.getCartId(), item.getProduct().getProductId());
        });

        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));

        orderDTO.setAddressId(addressId);

        return orderDTO;
    }

    @Override
    public OrderDTOResponse getUserOrders(Integer pageNumber, Integer pageSize, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equals("asc")? Sort.by("totalAmount").ascending() : Sort.by("totalAmount").descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        String email = authUtil.loggedInEmail();
        Page<Order> page = orderRepository.findAllByEmail(email, pageDetails);
        List<OrderDTO> orderDTOS = new ArrayList<>();
        for (Order userOrder : page.getContent()) {
            OrderDTO orderDTO = modelMapper.map(userOrder, OrderDTO.class);
            orderDTOS.add(orderDTO);
        }
        OrderDTOResponse orderDTOResponse = new OrderDTOResponse();
        orderDTOResponse.setContent(orderDTOS);
        orderDTOResponse.setPageNumber(page.getNumber());
        orderDTOResponse.setPageSize(page.getSize());
        orderDTOResponse.setTotalElements(page.getTotalElements());
        orderDTOResponse.setTotalPages(page.getTotalPages());
        orderDTOResponse.setLastPage(page.isLast());
        return orderDTOResponse;
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        System.out.println(orderDTO);
        return orderDTO;
    }
}
