package com.ecommerce.stockapp.service;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.github.cdimascio.dotenv.Dotenv;

public class PaymentService {
    private final String publishableKey;

    public PaymentService() {
        Dotenv dotenv = Dotenv.load();
        // Remplacez par votre clé secrète de test
        Stripe.apiKey =  dotenv.get("sk_test_cle_secrete");
        this.publishableKey = dotenv.get("pk_test_cle_public");
    }

    public String createPaymentIntent(long amountInCents) throws Exception {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("eur")
                .addPaymentMethodType("card")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getClientSecret();
    }

    public String getPublishableKey() {
        return publishableKey;
    }
}
