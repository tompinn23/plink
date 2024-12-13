package org.int13h.plink.server;

import java.util.Set;

public class HttpStatus {

    public static final HttpStatus OK = createStandard(200, "OK");
    public static final HttpStatus CREATED = createStandard(201, "Created");
    public static final HttpStatus ACCEPTED = createStandard(202, "Accepted");
    public static final HttpStatus NON_AUTHORITATIVE_INFORMATION = createStandard(203, "Non-Authoritative Information");
    public static final HttpStatus NO_CONTENT = createStandard(204, "No Content");
    public static final HttpStatus RESET_CONTENT = createStandard(205, "Reset Content");
    public static final HttpStatus PARTIAL_CONTENT = createStandard(206, "Partial Content");

    public static final HttpStatus MULTIPLE_CHOICES = createStandard(300, "Multiple Choices");
    public static final HttpStatus MOVED_PERMANENTLY = createStandard(301, "Moved Permanently");
    public static final HttpStatus FOUND = createStandard(302, "Found");
    public static final HttpStatus SEE_OTHER = createStandard(303, "See Other");
    public static final HttpStatus NOT_MODIFIED = createStandard(304, "Not Modified");

    public static final HttpStatus TEMPORARY_REDIRECT = createStandard(307, "Temporary Redirect");
    public static final HttpStatus PERMANENT_REDIRECT = createStandard(308, "Permanent Redirect");

    public static final HttpStatus BAD_REQUEST = createStandard(400, "Bad Request");
    public static final HttpStatus UNAUTHORIZED = createStandard(401, "Unauthorized");
    public static final HttpStatus PAYMENT_REQUIRED = createStandard(402, "Payment Required");
    public static final HttpStatus FORBIDDEN = createStandard(403, "Forbidden");
    public static final HttpStatus NOT_FOUND = createStandard(404, "Not Found");
    public static final HttpStatus METHOD_NOT_ALLOWED = createStandard(405, "Method Not Allowed");
    public static final HttpStatus NOT_ACCEPTABLE = createStandard(406, "Not Acceptable");
    public static final HttpStatus PROXY_AUTHENTICATION_REQUIRED = createStandard(407, "Proxy Authentication Required");
    public static final HttpStatus REQUEST_TIMEOUT = createStandard(408, "Request Timeout");
    public static final HttpStatus CONFLICT = createStandard(409, "Conflict");
    public static final HttpStatus GONE = createStandard(410, "Gone");
    public static final HttpStatus LENGTH_REQUIRED = createStandard(411, "Length Required");
    public static final HttpStatus PRECONDITION_FAILED = createStandard(412, "Precondition Failed");
    public static final HttpStatus CONTENT_TOO_LARGE = createStandard(413, "Content Too Large");
    public static final HttpStatus URI_TOO_LONG = createStandard(414, "URI Too Long");
    public static final HttpStatus UNSUPPORTED_MEDIA_TYPE = createStandard(415, "Unsupported Media Type");
    public static final HttpStatus RANGE_NOT_SATISFIABLE = createStandard(416, "Range Not Satisfiable");
    public static final HttpStatus EXPECTATION_FAILED = createStandard(417, "Expectation Failed");
    public static final HttpStatus IM_A_TEAPOT = createStandard(418, "I'm a teapot");
    public static final HttpStatus MISDIRECTED_REQUEST = createStandard(421, "Misdirected Request");

    public static final HttpStatus TOO_EARLY = createStandard(425, "Too Early");
    public static final HttpStatus UPGRADE_REQUIRED = createStandard(426, "Upgrade Required");
    public static final HttpStatus PRECONDITION_REQUIRED = createStandard(428, "Precondition Required");
    public static final HttpStatus TOO_MANY_REQUESTS = createStandard(429, "Too Many Requests");
    public static final HttpStatus REQUEST_HEADER_FIELD_TOO_LARGE = createStandard(431, "Request Header Field Too Large");
    public static final HttpStatus UNAVAILABLE_FOR_LEGAL_REASONS = createStandard(451, "Unavailable For Legal Reasons");

    public static final HttpStatus INTERNAL_SERVER_ERROR = createStandard(500, "Internal Server Error");
    public static final HttpStatus NOT_IMPLEMENTED = createStandard(501, "Not Implemented");
    public static final HttpStatus BAD_GATEWAY = createStandard(502, "Bad Gateway");
    public static final HttpStatus SERVICE_UNAVAILABLE = createStandard(503, "Service Unavailable");
    public static final HttpStatus GATEWAY_TIMEOUT = createStandard(504, "Gateway Timeout");
    public static final HttpStatus HTTP_VERSION_NOT_SUPPORTED = createStandard(505, "HTTP Version Not Supported");
    public static final HttpStatus VARIANT_ALSO_NEGOTIATES = createStandard(506, "Variant Also Negotiates");
    public static final HttpStatus NOT_EXTENDED = createStandard(510, "Not Extended");
    public static final HttpStatus NETWORK_AUTHENTICATION_REQUIRED = createStandard(511, "Network Authentication Required");

    private final int code;
    private final String reason;

    private final boolean standard;

    public HttpStatus(int code, String reason) {
        this(code, reason, false);
    }

    private static HttpStatus createStandard(int code, String reason) {
        return new HttpStatus(code, reason, true);
    }

    private HttpStatus(int code, String reason, boolean standard) {
        this.code = code;
        this.reason = reason;
        this.standard = standard;
    }

    public int getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }

    public boolean isStandard() {
        return standard;
    }
}
