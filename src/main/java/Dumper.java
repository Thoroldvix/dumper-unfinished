
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.IOException;
import java.util.*;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
public class Dumper {
    public static double actualPrice;
    private static List<Seller> sellers = new ArrayList<Seller>();
    private static List<Double> prices = new ArrayList<Double>();
    private static List<Integer> stocks = new ArrayList<Integer>();
    private static final Seller ME = new Seller("datassgold");
    private String listingNumber;
    private String url;
    private ChromeOptions options;
    private  WebDriver driver;
    private String listingPage;
    private double limit;
    public Dumper() {

    }

    public Dumper(Server server, double limit) {
        this.limit = limit;
        switch (server){
            case GOLEMAG:
              url = "https://www.g2g.com/userBar/regional?reg_cur=USD&reg_lang=en&getParam=%7B%22server%22%3A%2230779%22%2C%22faction%22%3A%221086%22%2C%22" +
                      "online%22%3A%22checked%22%2C%22sorting%22%3A%22price%40asc%22%2C%22gameId%22%3A%222522%22%2C%22prodType%22%3A%2219248%22%7D&previousPath=productListing%2Findex";
                listingNumber = "3757518";
                listingPage = "2522";
                break;
            case WHITEMANE:
                url = "https://www.g2g.com/userBar/regional?reg_cur=USD&reg_lang=en&getParam=%7B%22server%22%3A%2230805%22%2C%22faction%22%3A%22543%22%2C%22online%22%3A%22" +
                        "checked%22%2C%22sorting%22%3A%22price%40asc%22%2C%22gameId%22%3A%222299%22%2C%22prodType%22%3A%2219249%22%7D&previousPath=productListing%2Findex";
                listingNumber = "5560757";
                listingPage = "2299";
                break;
        }

    }

    public void doStuff() throws InterruptedException, IOException {
        if (url != null) {
            try {
                Document document = Jsoup.connect(url).get();
                getMyPrice(document);
                getMyStock(document);
                getNames(document);
                getPrices(document);
                getStock(document);
                for (int i = 0; i < sellers.size(); i++) {
                    sellers.get(i).setPrice(prices.get(i));
                    sellers.get(i).setStock(stocks.get(i));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Seller seller : sellers) {
                System.out.print(seller.getName() + " Price: " + seller.getPrice());
                System.out.println(" Stock: " + seller.getStock());

            }
            System.out.println();
            System.out.println(ME.getPrice());
            System.out.println(ME.getStock());
            if (isPriceChanged()) {
                    //switchPrice(limit);
            }
//            if (backToNormal()){
//
//            }
            else
                sellers.clear();
        }
        System.out.println();
    }

    public void getMyPrice(Document html) {
        Elements myP = html.select(String.format("span#ppu-%s",listingNumber));
        for (Element e : myP) {
            if (e == null) {
                System.out.println("No active listing");
            } else
                ME.setPrice(Double.parseDouble(e.text().substring(e.text().lastIndexOf("0."))));
        }
    }

    public void getMyStock(Document html) {
        Elements myS = html.select(String.format("span#fc-qty-%s",listingNumber));
        for (Element e : myS) {
            if (e == null) {
                System.out.println("No active listing");
            } else
                ME.setStock(Integer.parseInt(e.text()
                        .substring(0, e.text().indexOf(" "))
                        .replaceAll(",", "")));
        }

    }

    public void getPrices(Document html) {
        Elements price = html.select("div.products__statistic");
        for (int i = 0; i < sellers.size(); i++) {
            for (Element p : price) {
                String pc = p.text().substring(p.text().lastIndexOf("0."));
                prices.add(Double.parseDouble(pc));
            }
        }
    }

    public void getStock(Document html) {
        Elements stock = html.select("div.products__statistic");
        for (Element e : stock) {
            int st = Integer.parseInt(e.select("span.products__statistic-item").text()
                    .replaceFirst("[\\d]", "").replaceAll("[^\\d]", ""));
            stocks.add(st);
        }
    }

    public void getNames(Document html) {
        Elements names = html.select("div.seller__title a.seller__name");
        for (Element n : names) {
            sellers.add(new Seller(n.text()));
        }
    }

    public void switchPrice(double limit) throws InterruptedException, IOException {
        if  (actualPrice > limit) {
            System.out.println("Switching price...");
            driver.findElement(By.xpath("//div[@class='manage__table-actions-detail']//a[@class='g2g_products_price editable editable-click']"))
                    .click();
            Thread.sleep(1000);
            driver.findElement(By.xpath("//div[@class='editable-input notranslate']//span[@class='editable-clear-x']"))
                    .click();
            Thread.sleep(1000);
            driver.findElement(By.xpath("//div[@class='editable-input notranslate']//input[@class='input-large']"))
                    .sendKeys(String.format(Locale.ENGLISH, "%.6f", actualPrice));
            Thread.sleep(1000);
            driver.findElement(By.xpath("//div[@class='btn-xedit']//button[@class='btn btn--green editable-submit']"))
                    .click();
        }
    }


    public boolean isPriceChanged() {

        actualPrice =  ME.getPrice();
        System.out.println(actualPrice);
        for (Seller seller : sellers) {
            if (ME.getPrice() != 0 && seller.getPrice() < ME.getPrice() && seller.getStock() > 700) {
                actualPrice =  seller.getPrice() - 0.00001d;
                    System.out.println(actualPrice);
                    return true;
                }
            }
        return false;
    }

    public boolean backToNormal() {
        for (Seller seller : sellers) {
            if (actualPrice < seller.getPrice() && seller.getPrice() - ME.getPrice() > 1) {
                actualPrice = (double) (seller.getPrice() - 1)/1_000;
                System.out.println(actualPrice);
                return true;
            }
        }
        return false;
    }
//    public boolean limit(){
//            for (Seller seller : sellers) {
//                if (seller.getPrice() < actualPrice&& actualPrice - seller.getPrice() < 2000) {
//                return false;
//       }
//    }
//            return true;
//    }
    public void start() throws IOException {
        String path="cmd /c start src\\main\\resources\\asdf.bat";
        Runtime rn=Runtime.getRuntime();
        rn.exec(path);
        org.apache.log4j.BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);
        System.setProperty("webdriver.chrome.driver", "D:\\Downloads\\Java libraries\\chromedriver_win32");
        options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.get(String.format("https://www.g2g.com/sell/manage?service=1&game=%s",listingPage));
    }

}

