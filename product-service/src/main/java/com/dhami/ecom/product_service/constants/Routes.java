package com.dhami.ecom.product_service.constants;

public final class Routes {

    private Routes() {}

    public static final String API_V1 = "/api/v1";

    public static final class Products {

        private Products() {}

        public static final String ROOT = "/products";
        public static final String ID = "/{id}";
        public static final String ID_DEDUCT_STOCK = "/{id}/deduct-stock";
        public static final String ID_RESTORE_STOCK = "/{id}/restore-stock";
    }

    public static final class Categories {

        private Categories() {}

        public static final String ROOT = "/categories";
        public static final String ID = "/{id}";
    }
}
