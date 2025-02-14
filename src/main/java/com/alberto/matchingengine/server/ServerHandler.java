package com.alberto.matchingengine.server;

import com.alberto.matchingengine.server.domain.ExecutionReport;
import com.alberto.matchingengine.server.domain.MarketOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.log4j.Log4j2;

import static com.alberto.matchingengine.MatchingEngineServer.orderBook;

@Log4j2
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("Received order: " + msg);
        try {
            // Parse the incoming JSON into a unified MarketOrder object.
            // For client-sent MARKET orders, the 'price' field is null.
            MarketOrder marketOrder = mapper.readValue(msg, MarketOrder.class);
            // Process the market order and create an execution report.
            ExecutionReport report = processMarketOrder(marketOrder);
            // Convert the report to JSON and send it back (append newline for the delimiter).
            String response = mapper.writeValueAsString(report) + "\n";
            ctx.writeAndFlush(response);
        } catch (Exception e) {
            log.error("Error processing order", e.getMessage());
            // On error, send an error report.
            ExecutionReport errorReport = ExecutionReport.builder()
                .type("exe_report")
                .initialQuantity(null)
                .executedPrice(null)
                .executedQuantity(null)
                .accountId("unknown")
                .status("ERROR")
                .build();
            String response = mapper.writeValueAsString(errorReport) + "\n";
            ctx.writeAndFlush(response);
        }
    }

    /**
     * Process a market order: attempt to match it against the order book.
     */
    private ExecutionReport processMarketOrder(MarketOrder marketOrder) {
        ExecutionReport report = new ExecutionReport();
        // Synchronize on the order book to ensure thread safety.
        synchronized(orderBook) {
            if (marketOrder.getType().equalsIgnoreCase("BUY")) {
                // For a BUY market order, look at the best (lowest-priced) SELL order.
                MarketOrder bestSell = orderBook.peekSell();
                if (bestSell != null && bestSell.getQuantity() >= marketOrder.getQuantity()) {
                    int initialQuantity = bestSell.getQuantity();
                    bestSell.setQuantity(bestSell.getQuantity() - marketOrder.getQuantity());
                    if (bestSell.getQuantity() == 0) {
                        orderBook.pollSell(); // remove order if fully executed
                    }
                    report = createExecutionReport("exe_report", String.valueOf(initialQuantity), bestSell.getPrice(), marketOrder.getQuantity(), marketOrder.getAccountId(), "FILLED");
                } else {
                    report = createRejectedExecutionReport(marketOrder.getAccountId());
                }
            } else if (marketOrder.getType().equalsIgnoreCase("SELL")) {
                // For a SELL market order, look at the best (highest-priced) BUY order.
                MarketOrder bestBuy = orderBook.peekBuy();
                if (bestBuy != null && bestBuy.getQuantity() >= marketOrder.getQuantity()) {
                    int initialQuantity = bestBuy.getQuantity();
                    bestBuy.setQuantity(bestBuy.getQuantity() - marketOrder.getQuantity());
                    if (bestBuy.getQuantity() == 0) {
                        orderBook.pollBuy(); // remove order if fully executed
                    }
                    report = createExecutionReport("exe_report", String.valueOf(initialQuantity), bestBuy.getPrice(), marketOrder.getQuantity(), marketOrder.getAccountId(), "FILLED");
                } else {
                    report = createRejectedExecutionReport(marketOrder.getAccountId());
                }
            } else {
                report = createRejectedExecutionReport(marketOrder.getAccountId());
            }
        }
        orderBook.logOrderBook();
        return report;
    }

    //method which creates REJECTED execution report using createExecutionReport method
    public ExecutionReport createRejectedExecutionReport(String accountId){
        return createExecutionReport("exe_report", null, null, null, accountId, "REJECTED");
    }


    public ExecutionReport createExecutionReport(String type, String initialQuantity, Double executedPrice, Integer executedQuantity, String accountId, String status){
        return ExecutionReport.builder()
                .type(type)
                .initialQuantity(initialQuantity)
                .executedPrice(executedPrice)
                .executedQuantity(executedQuantity)
                .accountId(accountId)
                .status(status)
                .build();
    }

}