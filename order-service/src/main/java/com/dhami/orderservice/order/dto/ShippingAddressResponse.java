package com.dhami.orderservice.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddressResponse {

    private String fullName;
    private String phone;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;
}
