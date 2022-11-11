package org.cryptimeleon.incentive.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasketItemNewDto {
    String id;
    String title;
    int price;
    int count;
}
