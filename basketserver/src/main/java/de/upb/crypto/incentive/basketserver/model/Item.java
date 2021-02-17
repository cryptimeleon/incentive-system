package de.upb.crypto.incentive.basketserver.model;

import io.swagger.annotations.ApiModelProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dataclass representing items that be purchased in a basket. */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
  @ApiModelProperty(value = "${item.id}")
  UUID id;

  @ApiModelProperty(value = "${item.title}")
  String title;

  @ApiModelProperty(value = "${item.price}")
  long price;
}
