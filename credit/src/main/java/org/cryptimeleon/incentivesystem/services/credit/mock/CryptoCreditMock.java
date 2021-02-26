package org.cryptimeleon.incentivesystem.services.credit.mock;

import org.cryptimeleon.incentivesystem.cryptoprotocol.interfaces.provider.CreditInterface;

/*
 * Mock for use until crypto credit-earn protocol is implemented
 */
public class CryptoCreditMock implements CreditInterface {

    @Override
    public String computeSerializedResponse(String serializedEarnRequest, long earnAmount) {
        return "";
    }
}
