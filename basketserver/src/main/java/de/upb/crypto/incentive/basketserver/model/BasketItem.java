package de.upb.crypto.incentive.basketserver.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Utility dataclass for representing Items in a Basket. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasketItem {
  Item item;
  int count;
}
