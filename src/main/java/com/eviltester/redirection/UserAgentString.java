package com.eviltester.redirection;

class UserAgentString {
    private String userAgent;
    private String userAgentDetailsURL;

    public UserAgentString(String theUserAgentString, String theURLForDetails){
        userAgent = theUserAgentString;
        userAgentDetailsURL = theURLForDetails;
    }

    public String getUserAgentString() {
        return userAgent;
    }

    public String getUserAgentURL() {
        return userAgentDetailsURL;
    }
}
