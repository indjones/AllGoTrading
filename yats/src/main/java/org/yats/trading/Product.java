package org.yats.trading;

import org.yats.common.UniqueId;

public class Product {

//    public static ProductNULL NULL = new ProductNULL();

    public boolean isRoute(String r) {
        return (route.compareTo(r)==0);
    }

    public boolean isProductAsPairChainable(Product product)
    {

        if(   product.hasUnderlyingId(underlyingId)
           || product.hasUnderlyingId(unitId)
           || product.hasUnitId(underlyingId)
           || product.hasUnitId(unitId)
           )
        {
            return (!product.hasProductId(productId));
        }
        return false;
    }


    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", exchange='" + exchange + '\'' +
                ", bloombergId='" + bloombergId + '\'' +
                ", name='" + name + '\'' +
                ", route='" + route + '\'' +
                ", underlyingId='" + underlyingId + '\'' +
                ", unitId='" + unitId + '\'' +
                '}';
    }

//    public boolean isSameAs(Product other) {
//        return other.hasProductId(productId);
//    }

    public boolean hasProductId(String pid) {
        return productId.compareTo(pid) == 0;
    }

    public boolean hasUnitId(String pid) {
        return unitId.compareTo(pid) == 0;
    }

    public boolean hasUnderlyingId(String pid) {
        return underlyingId.compareTo(pid) == 0;
    }

    public String getProductId() {
        return productId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public String getBloombergId() {
        return bloombergId;
    }

    public String getName() {
        return name;
    }

    public String getRoute() {
        return route;
    }

    public String getUnderlyingId() {
        return underlyingId;
    }

    public String getUnitId() {
        return unitId;
    }

    public String toStringCSV() {
        return ""+productId+","+symbol+","+exchange+","+bloombergId+","+name+","+route+","+ underlyingId +","+unitId;
    }

    public Product withProductId(String p) {
        productId = p;
        return this;
    }

    public Product withSymbol(String s) {
        symbol=s;
        return this;
    }

    public Product withExchange(String e) {
        exchange=e;
        return this;
    }

    public Product withBloombergId(String s) {
        bloombergId = s;
        return this;
    }

    public Product withName(String s) {
        name = s;
        return this;
    }

    public Product withRoute(String s) {
        route = s;
        return this;
    }

    public Product withUnderlyingId(String s) {
        underlyingId = s;
        return this;
    }

    public Product withUnitId(String s) {
        unitId = s;
        return this;
    }

    public Product(String productId, String symbol, String exchange) {
        this.productId = productId;
        this.symbol = symbol;
        this.exchange = exchange;
    }

    public Product(String productId, String symbol, String exchange, String _underlying, String _unitId) {
        this.productId = productId;
        this.symbol = symbol;
        this.exchange = exchange;
        underlyingId = _underlying;
        unitId = _unitId;
    }

    public Product() {
        productId = new UniqueId().toString();
    }

    private String productId;
    private String symbol;
    private String exchange;
    private String bloombergId;
    private String name;
    private String route;
    private String underlyingId;
    private String unitId;

    public boolean isNoRateProduct() {
        return unitId.compareTo(underlyingId)==0;
    }


//    private static class ProductNULL extends Product {
//        public String getProductId() { throw new RuntimeException("This is null object!");}
//        public String getSymbol() {
//            throw new RuntimeException("This is null object!");
//        }
//        public String getExchange() {
//            throw new RuntimeException("This is null object!");
//        }
//        private ProductNULL() { super("NULL","NULL","NULL");}
//    }

} // class
