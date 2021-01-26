package de.upb.crypto.incentive.basketserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    UUID id;
    String title;
    int price;
}
