package org.yats.trader.examples.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.Decimal;
import org.yats.trader.StrategyBase;
import org.yats.trading.*;

import java.util.HashMap;

public class Quoter extends StrategyBase {

    // the configuration file log4j.properties for Log4J has to be provided in the working directory
    // an example of such a file is at config/log4j.properties.
    // if Log4J gives error message that it need to be configured, copy this file to the working directory
    final Logger log = LoggerFactory.getLogger(Quoter.class);

    @Override
    public void onMarketData(MarketData marketData)
    {
        if(!isInitialised()) return;
        if(shuttingDown) return;
        if(marketData.hasProductId(tradeProductId)) onTPMarketData(marketData);
        if(marketData.hasProductId(refProductId)) onRPMarketData(marketData);
    }

    public void onTPMarketData(MarketData marketData) {
        log.info("TradeProduct: "+marketData);
        log.info("TradeProduct: "+marketData.getOfferBookAsCSV());
    }

    public void onRPMarketData(MarketData marketData) {
        log.info("RefProduct: "+marketData);

        if(!marketData.isSameFrontRowBidAs(prevRefMarketData)) {
            cancelOrders(BookSide.BID);
            sendBidRelativeTo(marketData.getBid());
        }
        if(!marketData.isSameFrontRowAskAs(prevRefMarketData)) {
            cancelOrders(BookSide.ASK);
            sendAskRelativeTo(marketData.getAsk());
        }

        prevRefMarketData = marketData;
    }


    @Override
    public void onReceipt(Receipt receipt)
    {
        if(!isInitialised()) return;
        if(shuttingDown) return;

        if(!receipt.hasProductId(tradeProductId)){
            log.error("Received receipt for unknown product: " + receipt);
            return;
        }

        if(receipt.getRejectReason().length()>0) {
            log.error("Received rejection "+receipt);
            orders.remove(receipt.getOrderId().toString());
            return;
//            shutdown();
//            System.exit(-1);
        }

        position = position.add(receipt.getPositionChange());
        log.info("position="+position);

        log.info("Received receipt: " + receipt);

        if(receipt.isEndState()) {
            orders.remove(receipt.getOrderId().toString());
        }

    }

    @Override
    public void init()
    {
        super.init();
        setInternalAccount(getConfig("internalAccount"));
        tradeProductId = getConfig("tradeProductId");
        refProductId = getConfig("referenceProductId");
        subscribe(tradeProductId);
        subscribe(refProductId);
        position = getPositionForProduct(tradeProductId);
        log.info("position="+position);
        tickSize =getConfigAsDecimal("tickSize");
        stepFactor=getConfigAsDouble("stepFactor");
        orderSize=getConfigAsDouble("orderSize");
    }

    @Override
    public void shutdown()
    {
        shuttingDown=true;
        cancelOrders();
    }

    private void sendBidRelativeTo(Decimal price) {
        double bidMarket=price.toDouble();
        Decimal bidPrice1 = Decimal.fromDouble(bidMarket*(1.0-stepFactor)- tickSize.toDouble()).roundToTickSize(tickSize);
        Decimal bidPrice2 = Decimal.fromDouble(bidMarket*(1.0-(2.0*stepFactor))- tickSize.toDouble()).roundToTickSize(tickSize);
        sendOrderIfNotExisting(BookSide.BID, bidPrice1);
        sendOrderIfNotExisting(BookSide.BID, bidPrice2);
    }

    private void sendAskRelativeTo(Decimal price) {
        double askMarket=price.toDouble();
        Decimal askPrice1 = Decimal.fromDouble(askMarket*(1.0+stepFactor)+ tickSize.toDouble()).roundToTickSize(tickSize);
        Decimal askPrice2 = Decimal.fromDouble(askMarket*(1.0+(2.0*stepFactor))+ tickSize.toDouble()).roundToTickSize(tickSize);
        sendOrderIfNotExisting(BookSide.ASK, askPrice1);
        sendOrderIfNotExisting(BookSide.ASK, askPrice2);
    }

    private void sendOrderIfNotExisting(BookSide _side, Decimal _price) {
        if(!orderExists(_side, _price)) {
            sendOrder(_side, _price);
        }
    }

    private boolean orderExists(BookSide side, Decimal price) {
        for(OrderNew order : orders.values()) {
            boolean sameSide = order.isForBookSide(side);
            boolean samePrice = order.getLimit().isEqualTo(price);
            if(samePrice && sameSide) return true;
        }
        return false;
    }

    private void sendOrder(BookSide side, Decimal bid)
    {
        OrderNew order = OrderNew.create()
                .withProductId(tradeProductId)
                .withInternalAccount(getInternalAccount())
                .withBookSide(side)
                .withLimit(bid)
                .withSize(Decimal.fromDouble(orderSize));
//        receivedOrderReceiptBidSide = false;
        orders.put(order.getOrderId().toString(), order);
        sendNewOrder(order);
    }

    private void cancelOrders() {
        for(OrderNew order : orders.values()) {
            OrderCancel o = order.createCancelOrder();
            sendOrderCancel(o);
        }
    }

    private void cancelOrders(BookSide side) {
        for(OrderNew order : orders.values()) {
            if(order.isForBookSide(side)) {
                OrderCancel o = order.createCancelOrder();
                sendOrderCancel(o);
            }
        }
    }


    public Quoter() {
        super();
        shuttingDown=false;
        position = Decimal.ZERO;
        orders = new HashMap<String, OrderNew>();
        prevRefMarketData = MarketData.NULL;
    }

    private Decimal position;
    private boolean shuttingDown;
    private String tradeProductId;
    private String refProductId;
    private HashMap<String, OrderNew> orders;
    private MarketData prevRefMarketData;

    private double stepFactor = 0.0;//0031;
    private Decimal tickSize = Decimal.ONE;
    private double orderSize = 0.01;


//    private boolean receivedOrderReceiptBidSide;

} // class

