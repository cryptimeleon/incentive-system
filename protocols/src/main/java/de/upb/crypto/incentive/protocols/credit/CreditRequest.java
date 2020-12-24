package de.upb.crypto.incentive.protocols.credit;

import de.upb.crypto.incentive.protocols.model.Token;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditRequest {
    private int id;
    private Token token;
    private int increase;
    private long basketId;
}
