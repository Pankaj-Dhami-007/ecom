package com.dhami.orderservice.constants;

public final class Routes {

    private Routes() {}

    public static final String API_V1 = "/api/v1";

    public static final class Orders {

        private Orders() {}

        public static final String ROOT = "/orders";
        public static final String ID = "/{id}";
        public static final String ID_CANCEL = "/{id}/cancel";
        public static final String ID_STATUS = "/{id}/status";
        public static final String ID_RETRY_PAYMENT = "/{id}/retry-payment";
        public static final String ID_CONFIRM = "/{id}/confirm";
        public static final String ID_PAYMENT_FAILED = "/{id}/payment-failed";
    }

    public static final class Products {

        private Products() {}

        public static final String ROOT = "/products";
        public static final String ID = "/{id}";
        public static final String ID_DEDUCT_STOCK = "/{id}/deduct-stock";
        public static final String ID_RESTORE_STOCK = "/{id}/restore-stock";
        public static final String ID_RESERVE_STOCK = "/{id}/reserve-stock";
        public static final String ID_CONFIRM_RESERVATION = "/{id}/confirm-reservation";
        public static final String ID_RELEASE_RESERVATION = "/{id}/release-reservation";
    }

    public static final class Payments {

        private Payments() {}

        public static final String ROOT = "/payments";
    }
}
