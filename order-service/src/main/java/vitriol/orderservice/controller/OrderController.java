package vitriol.orderservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vitriol.orderservice.dto.OrderDto;
import vitriol.orderservice.jpa.OrderEntity;
import vitriol.orderservice.messagequeue.KafkaProducer;
import vitriol.orderservice.messagequeue.OrderProducer;
import vitriol.orderservice.service.OrderService;
import vitriol.orderservice.vo.RequestOrder;
import vitriol.orderservice.vo.ResponseOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/order-service")
public class OrderController {

    private final Environment env;
    private final OrderService orderService;
    private final ModelMapper mapper;
    private final KafkaProducer kafkaProducer;
    private final OrderProducer orderProducer;

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's working in Order service on port %s", env.getProperty("local.server.port"));
    }

    // http://127.0.0.1/order-service/{user_id}/orders/
    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId, @RequestBody RequestOrder requestOrder) {
        log.info("Before add orders data");
        OrderDto orderDto = mapper.map(requestOrder, OrderDto.class);
        orderDto.setUserId(userId);

        /* JPA */
        OrderDto createdOrder = orderService.createOrder(orderDto);
        ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);

//        /* kafka */
//        orderDto.setOrderId(UUID.randomUUID().toString());
//        orderDto.setTotalPrice(requestOrder.getQty() * requestOrder.getUnitPrice());
//
//
//        /* send this order to kafka */
//        kafkaProducer.send("example-catalog-topic", orderDto);
//        orderProducer.send("orders", orderDto);
//
//        ResponseOrder responseOrder = mapper.map(orderDto, ResponseOrder.class);
        log.info("After added orders data");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<ResponseOrder>> getOrder(@PathVariable("userId") String userId) {

        log.info("Before retrieve orders data");
        Iterable<OrderEntity> orderList = orderService.getOrdersByUserId(userId);
        List<ResponseOrder> result = new ArrayList<>();
        orderList.forEach(o->{
            result.add(mapper.map(o, ResponseOrder.class));
        });

//        try {
//            Thread.sleep(1000);
//            throw new Exception("장애 발생");
//        } catch (InterruptedException e) {
//            log.warn(e.getMessage());
//        }

        log.info("After retrieved orders data");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
