package de.upb.crypto.incentive.services.credit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditResponse {
    private UUID id;
    private String serializedCreditResponse;
}