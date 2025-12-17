package com.ecommerce.order.component;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import com.ecommerce.order.event.OrderCancelled;
import com.ecommerce.order.event.StockIncreaseRequested;
import com.ecommerce.order.event.StockRequested;
import com.ecommerce.order.event.TransactionRefundRequested;
import com.ecommerce.order.event.TransactionRequested;
@Component
public class EventPublisher {

    private static final Logger logger =
        LoggerFactory.getLogger(EventPublisher.class);

    private final StreamBridge streamBridge;

    public EventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishOrderCancelled(OrderCancelled event) {
        logger.info("OrderCancelled sent for sagaId {} with orderId {}",event.sagaId(),event.orderId());
        streamBridge.send("orderCancelled-out-0", event);
    }

    public void publishTransactionRequested(TransactionRequested event) {
        logger.info("TransactionRequested sent for sagaId {} with orderId {}",event.sagaId(),event.orderId());
        streamBridge.send("transactionRequested-out-0", event);
    }

    public void publishStockRequested(StockRequested event){
        logger.info("StockRequested sent for sagaId {} with orderId {}",event.sagaId(),event.orderId());
        streamBridge.send("stockRequested-out-0", event);
    }

    public void publishTransactionRefundRequested(TransactionRefundRequested event){
        logger.info("TransactionRefundRequested sent for sagaId {} with orderId {}",event.sagaId(),event.orderId());
        streamBridge.send("transactionRefundRequested-out-0", event);
    }

    public void publishStockIncreaseRequested(StockIncreaseRequested event){
        logger.info("StockIncreaseRequested sent for sagaId {} with orderId {}",event.sagaId(),event.orderId());
        streamBridge.send("stockIncreaseRequested-out-0", event);
    }
}
