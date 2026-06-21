package com.azaatar.eventguard.domain;

import java.math.BigDecimal;


// One PaymentRecord object is a domain object that translates to a single line read from the payments CSV

public record PaymentRecord (

    String paymentId, //    paymentId: String
    String accountId, //    accountId: String
    String name, //    name: String
    String email, //    email: String
    BigDecimal amount, //    amount: BigDecimal
    String currency, //    currency: String
    PaymentStatus status //    status: String

)
{}