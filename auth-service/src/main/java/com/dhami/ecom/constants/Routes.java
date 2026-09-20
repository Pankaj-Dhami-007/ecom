package com.dhami.ecom.constants;

public final class Routes {

    private Routes() {}

    public static final String API_V1 = "/api/v1";
    public static final String ROOT_PATH = "/";

    public static final class Auth {

        private Auth() {}

        public static final String ROOT = "/auth" + API_V1;
        public static final String REGISTER = "/register";
        public static final String LOGIN = "/login";
        public static final String REFRESH = "/auth/refresh";
        public static final String MATCHER = ROOT + "/**";
    }
}
