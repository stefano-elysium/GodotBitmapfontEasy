package com.dashur.integration.extw.connectors.qt.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class RollbackRequest extends Request {
  @JsonProperty("txnId")
  private String txnId;

  @JsonProperty("betId")
  private String betId;

  @JsonProperty("playerId")
  private String playerId;

  @JsonProperty("roundId")
  private String roundId;

  @JsonProperty("amount")
  private BigDecimal amount;

  @JsonProperty("currency")
  private String currency;

  @JsonProperty("bonusType")
  private String bonusType;

  @JsonProperty("promoCode")
  private String promoCode;

  @JsonProperty("gameId")
  private String gameId;

  @JsonProperty("created")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss.SSS'+00:00'")
  private Date created;

  @JsonProperty("completed")
  private Boolean completed;
}
