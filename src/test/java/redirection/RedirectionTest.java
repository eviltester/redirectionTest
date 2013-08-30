package redirection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RedirectionTest {

    // Change this to the physical location of your phantomjs.exe
    public static final File PHANTOMJS_EXE =
            new File(System.getProperty("user.dir"), "tools/phantomjs-1.9.1-windows/phantomjs.exe");

    // We might find duplicate oracles in the pages so use a Set to remove dupes
    static Set<String> checkUserAgents = new HashSet<String>();

    @BeforeClass
    public static void checkDependencies(){
        assertThat(PHANTOMJS_EXE.exists(), is(true));
    }

    public static void getAllUserAgents(){

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

        for(String oracleURL : oracleURLS){

            driver.navigate().to(oracleURL);

            List<WebElement> anchors = driver.findElements(By.cssSelector("ul > li > a"));
            int total = anchors.size();
            int current = 0;
            int toCheck=0;

            for(WebElement anchor : anchors){
                System.out.println("Building User Agent Oracle List: " + current + "/" + total + " | " + toCheck);
                String userAgent =  anchor.getText();
                if(oracleURL.contains("BlackBerry")){
                    checkUserAgents.add(userAgent);
                    toCheck++;
                }
                if(oracleURL.contains("Android")){
                    checkUserAgents.add(userAgent);
                    toCheck++;
                }
                if(userAgent.contains("(Blackberry;")){
                    checkUserAgents.add(userAgent);
                    toCheck++;
                }
                if(userAgent.startsWith("BlackBerry")){
                    checkUserAgents.add(userAgent);
                    toCheck++;
                }
                if(userAgent.contains("(iPhone;")){
                    checkUserAgents.add(userAgent);
                    toCheck++;
                }
                if(userAgent.contains("Windows Phone OS")){
                    checkUserAgents.add(userAgent);
                    toCheck++;
                }
                current++;
            }
        }

        // TODO: write out the oracle URLs to a file and read from file to make future test run faster
    }

    @Test
    public void explorePhantom(){

        List<String> failedAgents = new ArrayList<String>();

        // if you want a quick test then use the iphone string, otherwise getAllUserAgents
        getAllUserAgents();
        // checkUserAgents.add("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7");

        int total = checkUserAgents.size();
        int current = 0;
        int failedCount=0;

        // visit these urls and check that they redirect
        List<String> redirectFrom = new ArrayList<String>();
        // TODO: CHANGE THESE TO YOUR SITE URLS
        // redirectFrom.add("http://www.bbc.co.uk");
        redirectFrom.add("http://www.tfl.gov.uk");

        // should I clear cookies after every redirect attempt?
        boolean clearCookies = true;

        // consider it a redirect if the URL starts with ...
        // e.g. String redirectToStartsWith = "http://mobile.";
        // String redirectToStartsWith = "http://mob.";
        String redirectToStartsWith = "http://m.";

        for(String agent : checkUserAgents){
            System.out.println(current + "/" + total + " | " + failedCount);
            System.out.println(agent);

            DesiredCapabilities caps = new DesiredCapabilities();
            caps.setJavascriptEnabled(true);
            caps.setCapability("phantomjs.binary.path", PHANTOMJS_EXE.getAbsolutePath());
            caps.setCapability("phantomjs.page.settings.userAgent", agent);

            boolean exceptionRaised = false;
            PhantomJSDriver driver = new PhantomJSDriver(caps);

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
                }else{
                    System.out.print("FAILED : ");
                    failedAgents.add(agent);
                    failedCount++;
                }

                System.out.println(redirectedTo + " === from === " + redirectFromThisURL);

                driver.quit();  // otherwise you have a whole set of phantomjs.exe lying about
            }

            current++;
        }

        System.out.println("");
        System.out.println("The following " + failedAgents.size() + " user agents failed to redirect");
        System.out.println("=================================================");
        for(String failed : failedAgents){
            System.out.println(failed);
        }
        // expect redirect
        assertThat(failedAgents.size(), is(0));

        // TODO: write an html report that links back to the failed user agent strings on useragentstring.com

    }
}
