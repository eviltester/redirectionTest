package com.eviltester.redirection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.*;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RedirectionTest {

    // Change the code so that PHANTOMJS_EXE points to the physical location of your phantomjs.exe
    // public static final File PHANTOMJS_EXE =
    //        new File(System.getProperty("user.dir"), "tools/phantomjs-1.9.1-windows/phantomjs.exe");

    // I last ran this on mac so my settings were
    public static final File PHANTOMJS_EXE =
            new File(System.getProperty("user.dir"), "tools/phantomjs-2.1.1-macosx/bin/phantomjs");

    UserAgentStrings toCheck = new UserAgentStrings();

    @BeforeClass
    public static void checkDependencies(){
        assertThat(PHANTOMJS_EXE.exists(), is(true));
    }


    public void getAllUserAgents() throws IOException {

        File userAgentPropertyFile = new File(System.getProperty("user.dir"), "userAgentsToCheck.properties");
        UserAgentPropertyFileReader reader = new UserAgentPropertyFileReader(userAgentPropertyFile);
        reader.populateUserAgentStrings(toCheck);

        if(toCheck.userAgents.size()>0)
            return;



        // Create a list of Oracle URL pages,
        // we will scan these for user agent strings that we will use in the test
        // could even use these
        // http://www.useragentstring.com/pages/Mobile%20Browserlist/
        List<String> oracleURLS = new ArrayList<String>();
        oracleURLS.add("http://www.useragentstring.com/pages/All/");
        oracleURLS.add("http://www.useragentstring.com/_uas_BlackBerry_version_.php");
        oracleURLS.add("http://www.useragentstring.com/_uas_Android%20Webkit%20Browser_version_.php");

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability("phantomjs.binary.path", PHANTOMJS_EXE.getAbsolutePath());
        WebDriver driver = new PhantomJSDriver(caps);



        for (String oracleURL : oracleURLS) {

            driver.navigate().to(oracleURL);

            List<WebElement> anchors = driver.findElements(By.cssSelector("ul > li > a"));
            int total = anchors.size();
            int current = 0;



            for (WebElement anchor : anchors) {
                System.out.println("Building User Agent Oracle List: " + current + "/" + total + " | " + toCheck.userAgents.size());
                UserAgentString userAgent = new UserAgentString(anchor.getText(), anchor.getAttribute("href"));
                if (oracleURL.contains("BlackBerry")) {
                    toCheck.addUserAgent(userAgent);
                }
                if (oracleURL.contains("Android")) {
                    toCheck.addUserAgent(userAgent);
                }
                if (userAgent.getUserAgentString().contains("(Blackberry;")) {
                    toCheck.addUserAgent(userAgent);
                }
                if (userAgent.getUserAgentString().startsWith("BlackBerry")) {
                    toCheck.addUserAgent(userAgent);
                }
                if (userAgent.getUserAgentString().contains("(iPhone;")) {
                    toCheck.addUserAgent(userAgent);
                }
                if (userAgent.getUserAgentString().contains("Windows Phone OS")) {
                    toCheck.addUserAgent(userAgent);
                }
                current++;
            }
        }

        UserAgentPropertyFileWriter writer = new UserAgentPropertyFileWriter(userAgentPropertyFile);
        writer.writeUserAgentsToPropertyFile(toCheck);

    }


    @Test
    public void explorePhantom() throws IOException {

        List<UserAgentString> failedAgents = new ArrayList<UserAgentString>();
        List<UserAgentString> passedAgents = new ArrayList<UserAgentString>();

        // if you want a quick test then use the iphone string, otherwise getAllUserAgents
        getAllUserAgents();
        // checkUserAgents.add("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7");

        int total = toCheck.userAgents.size();
        int current = 0;
        int failedCount=0;
        int passedCount=0;

        // visit these urls and check that they redirect
        List<String> redirectFrom = new ArrayList<String>();
        // TODO: CHANGE THESE TO YOUR SITE URLS
        redirectFrom.add("https://youtube.com");
        //redirectFrom.add("http://www.tfl.gov.uk");

        // should I clear cookies after every redirect attempt?
        boolean clearCookies = true;

        // consider it a redirect if the URL starts with ...
        // e.g. String redirectToStartsWith = "https://mobile.";
        // String redirectToStartsWith = "https://mob.";
        String redirectToStartsWith = "https://m.";

        for(UserAgentString userAgent : toCheck.userAgents.values()){

            String agent = userAgent.getUserAgentString();

            System.out.println(current + "/" + total + " | " + failedCount);
            System.out.println(agent);

            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setJavascriptEnabled(true);
            caps.setCapability("phantomjs.binary.path", PHANTOMJS_EXE.getAbsolutePath());
            caps.setCapability("phantomjs.page.settings.userAgent", agent);
            WebDriver driver = new PhantomJSDriver(caps);

            boolean exceptionRaised = false;



            for(String redirectFromThisURL : redirectFrom){

                try{
                    driver.navigate().to(redirectFromThisURL);

                }catch(Exception e){
                    // swallow it
                    e.printStackTrace();
                    exceptionRaised = true;
                }finally{
                    if(clearCookies){
                        driver.manage().deleteAllCookies();
                    }
                }

                String redirectedTo = driver.getCurrentUrl();

                if(!exceptionRaised && redirectedTo.startsWith(redirectToStartsWith)){
                    System.out.print("PASSED : ");
                    passedAgents.add(userAgent);
                    passedCount++;
                }else{
                    System.out.print("FAILED : ");
                    failedAgents.add(userAgent);
                    failedCount++;
                }

                System.out.println(redirectedTo + " === from === " + redirectFromThisURL);

                driver.quit();  // otherwise you have a whole set of phantomjs.exe lying about
            }

            current++;
        }

        File outputFile = new File(System.getProperty("user.dir"), "htmlOutput" + String.valueOf(System.currentTimeMillis()) + ".htm");

        PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));

        System.out.println("");
        System.out.println("The following " + failedAgents.size() + " user agents failed to redirect");
        for(String toRedirectFrom : redirectFrom){
            System.out.println(toRedirectFrom + " to " + redirectToStartsWith);
        }
        System.out.println("=================================================");
        for(UserAgentString failed : failedAgents){
            System.out.println(failed.getUserAgentString());
        }

        output.println("<html><head><title>HTML Output of User-Agent Check</title></head><body>");
        output.println("<b>The following " + failedAgents.size() + " user agents failed to redirect</b>");
        for(String toRedirectFrom : redirectFrom){
            output.println("<p>" + toRedirectFrom + " to " + redirectToStartsWith + "</p>");
        }

        output.println("<hr/><p><strong>Failed</strong></p><ul>");
        for(UserAgentString failed : failedAgents){
            output.println("<li><a href='" + failed.getUserAgentURL() + "'>" + failed.getUserAgentString() + "</a></li>");
        }
        output.println("</ul>");

        output.println("<hr/><p><strong>Passed</strong></p><ul>");
        for(UserAgentString passed : passedAgents){
            output.println("<li><a href='" + passed.getUserAgentURL() + "'>" + passed.getUserAgentString() + "</a></li>");
        }
        output.println("</ul>");

        output.println("</body></html>");
        output.close();

        // expect redirect
        assertThat(failedAgents.size(), is(0));
    }
}
