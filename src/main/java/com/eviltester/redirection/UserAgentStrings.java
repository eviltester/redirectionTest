package com.eviltester.redirection;

import java.util.HashMap;
import java.util.Map;

public class UserAgentStrings {

    Map<String, UserAgentString> userAgents = new HashMap<String, UserAgentString>();

    public void addUserAgent(UserAgentString userAgent) {
        userAgents.put(userAgent.getUserAgentString(), userAgent);
    }
}
