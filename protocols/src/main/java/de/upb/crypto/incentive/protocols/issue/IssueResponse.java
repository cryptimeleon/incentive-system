package de.upb.crypto.incentive.protocols.issue;

import de.upb.crypto.incentive.protocols.model.Token;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IssueResponse {
    private int id;
    private Token token;
}