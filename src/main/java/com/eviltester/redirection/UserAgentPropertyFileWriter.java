package com.eviltester.redirection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

public class UserAgentPropertyFileWriter {
    private final File userAgentPropertyFile;

    public UserAgentPropertyFileWriter(File userAgentPropertyFile) {
        this.userAgentPropertyFile = userAgentPropertyFile;
    }

    public void writeUserAgentsToPropertyFile(UserAgentStrings toCheck) throws IOException {
        Properties userAgentsAsProperties = new Properties();

        int userAgentID=1;

        for(UserAgentString userAgent : toCheck.userAgents.values()){
            userAgentsAsProperties.setProperty("userAgentString_"+userAgentID, userAgent.getUserAgentString());
            userAgentsAsProperties.setProperty("userAgentURL_"+userAgentID, userAgent.getUserAgentURL());
            userAgentID++;
        }

        userAgentsAsProperties.setProperty("total_agents", String.valueOf(toCheck.userAgents.size()));

        Writer output = new FileWriter(userAgentPropertyFile);
        userAgentsAsProperties.store(output, "User Agents To check");
        output.close();
    }
}
