package de.upb.crypto.incentive.services.credit.mock;

import de.upb.crypto.incentive.cryptoprotocol.interfaces.provider.CreditInterface;

public class CryptoCreditMock implements CreditInterface {

    @Override
    public String computeSerializedResponse(String serializedEarnRequest, long earnAmount) {
        return "";
    }
}
