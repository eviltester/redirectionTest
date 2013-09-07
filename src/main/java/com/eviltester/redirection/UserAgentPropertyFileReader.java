package com.eviltester.redirection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class UserAgentPropertyFileReader {
    private final File userAgentPropertyFile;

    public UserAgentPropertyFileReader(File userAgentPropertyFile) {
        this.userAgentPropertyFile = userAgentPropertyFile;
    }

    public void populateUserAgentStrings(UserAgentStrings toCheck) throws IOException {
        FileInputStream input = new FileInputStream(userAgentPropertyFile);
        Properties userAgentsAsProperties = new Properties();

        userAgentsAsProperties.load(input);
        input.close();

        int totalUserAgents = Integer.valueOf(userAgentsAsProperties.getProperty("total_agents"));

        for(int currentAgent=1; currentAgent<=totalUserAgents; currentAgent++){

            String userAgentString = userAgentsAsProperties.getProperty("userAgentString_"+ currentAgent);
            String userAgentURL = userAgentsAsProperties.getProperty("userAgentURL_"+ currentAgent);

            toCheck.addUserAgent(new UserAgentString(userAgentString, userAgentURL));
        }
    }
}
