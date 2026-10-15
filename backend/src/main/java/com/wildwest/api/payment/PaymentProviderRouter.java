package com.wildwest.api.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class PaymentProviderRouter {

    private final Map<String, PaymentProvider> providers;
    private final String defaultProvider;

    public PaymentProviderRouter(
            Set<PaymentProvider> providerBeans,
            @Value("${wildwest.payment.default-provider:PAYSTACK}") String defaultProvider
    ) {
        this.providers = new java.util.HashMap<>();
        for (PaymentProvider p : providerBeans) {
            providers.put(p.name(), p);
        }
        this.defaultProvider = defaultProvider.toUpperCase();
    }

    public PaymentProvider current() {
        PaymentProvider provider = providers.get(defaultProvider);
        if (provider == null) {
            throw new IllegalStateException(
                    "No payment provider registered for: " + defaultProvider);
        }
        return provider;
    }

    public PaymentProvider byName(String name) {
        PaymentProvider provider = providers.get(name.toUpperCase());
        if (provider == null) {
            throw new IllegalArgumentException("Unknown provider: " + name);
        }
        return provider;
    }
}