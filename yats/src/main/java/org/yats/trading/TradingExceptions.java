package org.yats.trading;

public class TradingExceptions {

    public static class RateConverterException extends RuntimeException {
        public RateConverterException(String msg) {
            super(msg);
        }
    }

    public static class ItemNotFoundException extends RuntimeException {
        public ItemNotFoundException(String msg) {
            super(msg);
        }
    }
    public static class FieldIsNullException extends RuntimeException {
        public FieldIsNullException(String msg) {
            super(msg);
        }
    }

    public static class UnknownIdException extends RuntimeException {
        public UnknownIdException(String s) { super(s);
        }
    }
} // class
