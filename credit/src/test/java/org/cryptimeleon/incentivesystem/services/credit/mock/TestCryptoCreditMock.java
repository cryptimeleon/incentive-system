package org.cryptimeleon.incentivesystem.services.credit.mock;

import org.cryptimeleon.incentivesystem.cryptoprotocol.interfaces.provider.CreditInterface;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCryptoCreditMock implements CreditInterface {
    private String validRequest;
    private String validResponse;
    private String invalidResponse;

    @Override
    public String computeSerializedResponse(String serializedEarnRequest, long earnAmount) {
        if (serializedEarnRequest.equals(validRequest)) {
            return validResponse;
        }
        return invalidResponse;
    }
}
