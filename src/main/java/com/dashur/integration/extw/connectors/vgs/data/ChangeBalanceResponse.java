package com.dashur.integration.extw.connectors.vgs.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.math.BigDecimal;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class ChangeBalanceResponse extends Response {
  @JacksonXmlProperty(localName = "ECSYSTEMTRANSACTIONID")
  private String ecSystemTransactionId;

  @JacksonXmlProperty(localName = "BALANCE")
  private BigDecimal balance;
}
