package com.dhami.paymentservice.constants;

public final class Routes {

    private Routes() {}

    public static final String API_V1 = "/api/v1";

    public static final class Payments {

        private Payments() {}

        public static final String ROOT = "/payments";
        public static final String ORDER_ID = "/order/{orderId}";
        public static final String GATEWAY_OUTCOME = "/gateway/outcome";
    }

    public static final class Orders {

        private Orders() {}

        public static final String ROOT = "/orders";
    }

    public static final class Carts {

        private Carts() {}

        public static final String ROOT = "/carts";
    }
}
