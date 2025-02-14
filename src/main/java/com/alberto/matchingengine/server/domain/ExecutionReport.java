package com.alberto.matchingengine.server.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionReport {
    private String type;
    private String initialQuantity;
    private Double executedPrice;
    private Integer executedQuantity;
    private String accountId;
    private String status;
}