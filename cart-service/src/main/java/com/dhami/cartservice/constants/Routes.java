package com.dhami.cartservice.constants;

public final class Routes {

    private Routes() {}

    public static final String API_V1 = "/api/v1";

    public static final class Carts {

        private Carts() {}

        public static final String ROOT = "/carts";
        public static final String ME = "/me";
        public static final String ID = "/{id}";
        public static final String ME_ITEMS = "/me/items";
        public static final String ME_ITEM_ID = "/me/items/{itemId}";
        public static final String ID_CHECKOUT = "/{id}/checkout";
        public static final String ID_CONVERT = "/{id}/convert";
    }

    public static final class Orders {

        private Orders() {}

        public static final String ROOT = "/orders";
        public static final String ID = "/{id}";
    }

    public static final class Products {

        private Products() {}

        public static final String ROOT = "/products";
        public static final String ID = "/{id}";
    }
}