package com.reactlibrary.Controllers;

public class Constants {

    public static String API_KEY;
    public static String MERCHANT_ID;
    public static String API_VERSION;

    public enum ERRORS {
        CANNOT_VERIFY_THE_REQUEST(567, "CANNOT_VERIFY_THE_REQUEST"),
        CANNOT_PROCESS(568, "CANNOT_PROCESS"),
        INVALID_SERVER_RESPONSE(569, "INVALID_SERVER_RESPONSE"),
        SERVICE_UNAVAILABLE(570, "SERVICE_UNAVAILABLE"),
        INTERNAL_ERROR(571, "INTERNAL_ERROR"),
        USER_ACTION_CLOSED(600, "USER_ACTION_CLOSED"),
        TOO_LARGE_RETRY_ATTEMPTS(601, "TOO_LARGE_RETRY_ATTEMPTS");

        public int CODE;
        public String MESSAGE;

        private ERRORS(int code, String message) {
            this.CODE = code;
            this.MESSAGE = message;
        }
    }

    private static String API = "https://dev.directpay.lk/v1/mpg/api";

    public enum ROUTES {
        GET_SESSION("/sdk/session"), ADD_CARD("/sdk/cardAdd"), LIST_CARD("/sdk/cardList"), REMOVE_CARD("/sdk/cardDelete"), PAY_CARD("/sdk/cardPay");
        public String URL;

        private ROUTES(String namespace) {
            this.URL = API + namespace;
        }
    }

    public enum HEADERS {
        DP_MERCHANT("Directpay-Merchant"), X_API_KEY("x-api-key"), DIGEST("Digest"), CONTENT_TYPE("Content-Type");

        public String KEY;

        HEADERS(String KEY) {
            this.KEY = KEY;
        }
    }

    public enum HTTP_CODE {
        OK(200), BAD_REQUEST(400), ERROR(500);
        public int CODE;

        HTTP_CODE(int CODE) {
            this.CODE = CODE;
        }

    }
}
