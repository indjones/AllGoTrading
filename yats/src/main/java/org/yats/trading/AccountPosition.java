package org.yats.trading;

import org.yats.common.CommonExceptions;
import org.yats.common.Decimal;

public class AccountPosition extends Position {

    public String toStringCSV() {
        return new StringBuilder()
                .append(getProductId()).append(",")
                .append(internalAccount).append(",")
                .append(getSize().toString())
                .toString();
    }

    public String getInternalAccount() {
        return internalAccount;
    }

    public String getKey() {
        return productId +","+internalAccount;
    }

    public AccountPosition add(AccountPosition other) {
        return new AccountPosition(productId, internalAccount, size.add(other.size));
    }

    public AccountPosition add(Receipt receipt) {
        Decimal newSize = size.add(receipt.getCurrentTradedSizeSigned());
        return new AccountPosition(productId, internalAccount, newSize);
    }

    public AccountPosition(String _productId, String _internalAccount, Decimal _size) {
        super(_productId, _size);
        this.internalAccount = _internalAccount;
    }

    public boolean isSameAs(AccountPosition other) {
        if(internalAccount.compareTo(other.internalAccount)!=0) return false;
        return super.isSameAs(other);
    }

    public boolean isForAccount(String _account) {
        return (internalAccount.compareTo(_account)==0);
    }

    private String internalAccount;

    public static AccountPosition fromStringCSV(String csv) {
        String[] fieldStrings = csv.split(",");
        if(fieldStrings.length!=3) throw new CommonExceptions.FieldNotFoundException("found length=" + fieldStrings.length);
        return new AccountPosition(fieldStrings[0],fieldStrings[1],new Decimal(fieldStrings[2]));
    }

} // class
