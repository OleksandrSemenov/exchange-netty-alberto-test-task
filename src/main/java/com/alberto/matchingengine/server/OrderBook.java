package com.alberto.matchingengine.server;

import com.alberto.matchingengine.server.domain.MarketOrder;
import lombok.extern.log4j.Log4j2;

import java.util.Comparator;
import java.util.PriorityQueue;

@Log4j2
public class OrderBook {
    // BUY orders: highest price first, then FIFO (earlier timestamp first).
    private final PriorityQueue<MarketOrder> buyOrders = new PriorityQueue<>(
            Comparator.comparingDouble(MarketOrder::getPrice).reversed()
                    .thenComparingLong(MarketOrder::getTimestamp)
    );
    // SELL orders: lowest price first, then FIFO.
    private final PriorityQueue<MarketOrder> sellOrders = new PriorityQueue<>(
            Comparator.comparingDouble(MarketOrder::getPrice)
                    .thenComparingLong(MarketOrder::getTimestamp)
    );

    public OrderBook() {
        // Initialize the order book with preset resting orders.
        // Example resting BUY orders:
        buyOrders.add(new MarketOrder("BUY", 100, "preset", 105.0));
        buyOrders.add(new MarketOrder("BUY", 50, "preset", 104.0));
        // Example resting SELL orders:
        sellOrders.add(new MarketOrder("SELL", 100, "preset", 100.0));
        sellOrders.add(new MarketOrder("SELL", 75, "preset", 101.0));
    }

    //method which logs the order book
    public void logOrderBook() {
        log.info("BUY ORDERS:");
        buyOrders.forEach(order -> log.info(order.getQuantity() + " @ " + order.getPrice()));
        log.info("SELL ORDERS:");
        sellOrders.forEach(order -> log.info(order.getQuantity() + " @ " + order.getPrice()));
    }

    public MarketOrder peekBuy() { return buyOrders.peek(); }
    public MarketOrder pollBuy() { return buyOrders.poll(); }
    public MarketOrder peekSell() { return sellOrders.peek(); }
    public MarketOrder pollSell() { return sellOrders.poll(); }
}