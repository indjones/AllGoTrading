package org.yats.connectivity.excel;

import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.DDEMLException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.client.DDEClientEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yats.common.IProvideProperties;
import org.yats.common.PropertiesReader;
import org.yats.common.Tool;
import org.yats.common.UniqueId;
import org.yats.connectivity.messagebus.StrategyToBusConnection;
import org.yats.trading.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

public class ExcelConnection implements IConsumeMarketData, IConsumeReceipt, DDEClientEventListener, IConsumeReports {

    final Logger log = LoggerFactory.getLogger(ExcelConnection.class);

    @Override
    public void onDisconnect() {
        System.out.println("onDisconnect()");
    }

    @Override
    public void onItemChanged(String s, String s2, String s3)
    {
        parseProductIds(s3);
        subscribeAllProductIds();
    }

    @Override
    public void onMarketData(MarketData marketData) {
        for (int i = 1; i < currentProductIDs.size(); i++) {
            int j = i + 1;
            if (marketData.hasProductId(currentProductIDs.elementAt(i))){
                try {
                    //Lv0
                    conversation.poke("R" + j + "C2", marketData.getBidSize().toString());
                    conversation.poke("R" + j + "C3", marketData.getBid().toString());
                    conversation.poke("R" + j + "C4", marketData.getAskSize().toString());
                    conversation.poke("R" + j + "C5", marketData.getAsk().toString());

                    //Lv1
                    if(marketData.getBook().getDepth(BookSide.BID)==2) {
                        conversation.poke("R" + j + "C6", marketData.getBook().getBookRow(BookSide.BID, 1).getSize().toString());
                        conversation.poke("R" + j + "C7", marketData.getBook().getBookRow(BookSide.BID, 1).getPrice().toString());
                    }
                    if(marketData.getBook().getDepth(BookSide.ASK)==2) {
                        conversation.poke("R" + j + "C8", marketData.getBook().getBookRow(BookSide.ASK, 1).getSize().toString());
                        conversation.poke("R" + j + "C9", marketData.getBook().getBookRow(BookSide.ASK, 1).getPrice().toString());
                    }

                    //Lv2
                    if(marketData.getBook().getDepth(BookSide.BID)==3) {
                        conversation.poke("R" + j + "C10", marketData.getBook().getBookRow(BookSide.BID, 2).getSize().toString());
                        conversation.poke("R" + j + "C11", marketData.getBook().getBookRow(BookSide.BID, 2).getPrice().toString());
                    }
                    if(marketData.getBook().getDepth(BookSide.ASK)==3) {
                        conversation.poke("R" + j + "C12", marketData.getBook().getBookRow(BookSide.ASK, 2).getSize().toString());
                        conversation.poke("R" + j + "C13", marketData.getBook().getBookRow(BookSide.ASK, 2).getPrice().toString());
                    }

                    

                } catch (DDEException e) {
                    e.printStackTrace();
                }
            }
        }

        // use strategyToBusConnection.sendSettings(...) to send settings to the strategies like in onReport(..) below

    }

    @Override
    public void onReceipt(Receipt receipt) {

    }

    @Override
    public void onReport(IProvideProperties p) {

        // reports from strategies are coming in here. send them to Excel

        // for now writing to console:
        System.out.println("Strategy reports: "+PropertiesReader.toString(p));

        //lets send the report back as settings to test the way back to the strategy
        strategyToBusConnection.sendSettings(p);
    }

    @Override
    public UniqueId getConsumerId() {
        return null;
    }

    public void subscribe(String pid)
    {
        strategyToBusConnection.subscribe(pid, this);
    }

    public boolean isExcelRunning() {
        return false;
    }

    public boolean isWorkbookOpenInExcel() {
        return false;
    }

    public void startExcelWithWorkbook() {
    }

    public void startDDE() {
        try
        {
            System.out.print("conversation.connect...");
            conversation.connect("Excel", "MarketData");
            System.out.println("done.");
            System.out.print("conversation.request...");
            String s = conversation.request("C1");
            System.out.println("done.");
            parseProductIds(s);
            subscribeAllProductIds();
            System.out.print("conversation.startAdvice...");
            conversation.startAdvice("C1");
            System.out.println("done.");
        }
        catch (DDEMLException e)
        {
            System.out.println("DDEMLException: 0x" + Integer.toHexString(e.getErrorCode())
                    + " " + e.getMessage());
        }
        catch (DDEException e)
        {
            System.out.println("DDEException: " + e.getMessage());
        }
    }

    public void stopDDE()
    {
        try {
            conversation.stopAdvice("C1");//This means all elements in column 1
            conversation.disconnect();
        } catch (DDEException e) {
            e.printStackTrace();
        }
    }

    public void go() throws InterruptedException, IOException {
        log.info("Starting ExcelConnection...");

//        thread = new Thread(this);
//        thread.start();

        if(Tool.isWindows()) startDDE();
        System.out.println("\n===");
        System.out.println("Initialization done.");
        System.out.println("Press enter to exit.");
        System.out.println("===\n");
        System.in.read();
        System.out.println("\nexiting...\n");

        if(Tool.isWindows()) stopDDE();
        if(Tool.isWindows()) Thread.sleep(1000);
        if(!Tool.isWindows())Thread.sleep(60000);

        log.info("ExcelConnection done.");
        System.exit(0);
    }

    public ExcelConnection(IProvideProperties _prop)
    {
        strategyToBusConnection = new StrategyToBusConnection(_prop);
        strategyToBusConnection.setMarketDataConsumer(this);
        strategyToBusConnection.setReceiptConsumer(this);
        strategyToBusConnection.setReportsConsumer(this);
        if(Tool.isWindows()) {
            conversation = new DDEClientConversation();  // cant use this on Linux
            conversation.setEventListener(this);
        } else {
            System.out.println("This is not Windows! DDEClient will not work!");
//            System.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    private void subscribeAllProductIds()
    {
        for(String pid : currentProductIDs)
        {
            subscribe(pid);
        }
    }

    private void parseProductIds(String s3) {
        String[] parts = s3.split("\r\n");
        currentProductIDs = new Vector<String>(Arrays.asList(parts));
    }


    private Vector<String> currentProductIDs=new Vector<String>();
    private StrategyToBusConnection strategyToBusConnection;
    private DDEClientConversation conversation;


} // class
