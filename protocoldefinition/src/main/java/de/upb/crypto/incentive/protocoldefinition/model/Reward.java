package de.upb.crypto.incentive.protocoldefinition.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reward {
  private String name;
  private int value;
}
