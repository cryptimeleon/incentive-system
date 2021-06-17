package org.cryptimeleon.incentive.services.credit.mock;


import org.cryptimeleon.incentive.services.credit.interfaces.CreditInterface;

/*
 * Mock for use until crypto credit-earn protocol is implemented
 */
public class CryptoCreditMock implements CreditInterface {

    @Override
    public String computeSerializedResponse(String serializedEarnRequest, long earnAmount) {
        return "";
    }
}
