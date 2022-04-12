package com.company;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.http.annotation.Obsolete;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.json.JsonOutput;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.company.Main.vreme;



//https://eportal.cezdata.cz/mtprovider/genfile?dt=po&tp=je&from=1.11.2019&to=30.11.2019&lang=en




public class Main implements Runnable{

    static void ts(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static File xlsTocsvConvert(File inputFile, int sit) {
        // For storing data into CSV files

        String fin=inputFile.toString();
        fin=fin.substring(0, fin.length()-3).concat("csv");
        File outputFile=new File(fin);

        StringBuffer data = new StringBuffer();
        data.append(Boolean.FALSE);
        try {
            FileOutputStream fos = new FileOutputStream(outputFile);

            // Get the workbook object for XLSX file

            //HSSFWorkbook wBook = new HSSFWorkbook(new FileInputStream(inputFile));

            // Get first sheet from the workbook
            //HSSFSheet sheet = wBook.getSheetAt(sit);

            Workbook wBook;
            if(inputFile.toString().endsWith(".xls")) wBook=new HSSFWorkbook(new FileInputStream(inputFile));
            else wBook = new XSSFWorkbook(new FileInputStream(inputFile));
            wBook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            Sheet sheet = wBook.getSheetAt(sit);


            Row row;
            Cell cell;

            // Iterate through each rows from first sheet
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                row = rowIterator.next();

                // For each row, iterate through each columns
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {

                    cell = cellIterator.next();
                    CellType ctp=cell.getCellType();

                    switch (ctp) {
                        case BOOLEAN:
                            data.append(cell.getBooleanCellValue() + ",");

                            break;
                        case NUMERIC:
                            data.append(cell.getNumericCellValue() + ",");

                            break;
                        case STRING:
                            data.append(cell.getStringCellValue().replace(",",".") + ",");
                            break;
                        case FORMULA:
                            try{
                                data.append(cell.getNumericCellValue() + ",");
                            }catch(Exception e){
                                try{
                                    data.append(cell.getStringCellValue() + ",");
                                }catch(IllegalStateException ee){
                                    ee.printStackTrace();
                                    data.append("-99.99,");
                                }
                            }

                            break;

                        case BLANK:
                            data.append("" + ",");
                            break;
                        default:
                            data.append(cell + ",");

                    }
                }
                data.append("\n");
            }

            fos.write(data.toString().getBytes());
            fos.close();


        } catch (Exception ioe) {
            ioe.printStackTrace();

        }
        return outputFile;
    }

    static void skiniSaGet(String urlS, String outFileName) throws IOException {
        ReadableByteChannel readableChannelForHttpResponseBody = null;
        FileChannel fileChannelForDownloadedFile = null;

        try {
            // Define server endpoint
            URL robotsUrl = new URL(urlS);
            Proxy p=new Proxy(Proxy.Type.HTTP,new InetSocketAddress(Utils.proxy, Utils.port));
            HttpURLConnection urlConnection = (HttpURLConnection) robotsUrl.openConnection(p);

            // Get a readable channel from url connection
            readableChannelForHttpResponseBody = Channels.newChannel(urlConnection.getInputStream());

            // Create the file channel to save file
            FileOutputStream fosForDownloadedFile = new FileOutputStream(outFileName);
            fileChannelForDownloadedFile = fosForDownloadedFile.getChannel();

            // Save the body of the HTTP response to local file
            fileChannelForDownloadedFile.transferFrom(readableChannelForHttpResponseBody, 0, Long.MAX_VALUE);

        } catch (IOException ioException) {
            //System.out.println("IOException occurred while contacting server.");
            //ioException.printStackTrace();
            throw ioException;
        } finally {
            if (readableChannelForHttpResponseBody != null) {
                try {
                    readableChannelForHttpResponseBody.close();
                } catch (IOException ioe) {
                    System.out.println("Error while closing response body channel");
                }
            }
            if (fileChannelForDownloadedFile != null) {
                try {
                    fileChannelForDownloadedFile.close();
                } catch (IOException ioe) {
                    System.out.println("Error while closing file channel for downloaded file");
                }
            }
        }
    }

    static void anchory(String linak) throws IOException{
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        ProxyConfig proxyConfig = new ProxyConfig(Utils.proxy, Utils.port, "http");
        webClient.getOptions().setProxyConfig(proxyConfig);
        HtmlAnchor anc=null;
        HtmlPage page=webClient.getPage(linak);
        List<HtmlAnchor> list=page.getAnchors();
        for(HtmlAnchor h: list){
            System.out.println(h.asXml());
        }
    }

    static void ispisiStranu(String urlS) throws IOException{

        URL url = new URL(urlS);
        Proxy p=new Proxy(Proxy.Type.HTTP,new InetSocketAddress(Utils.proxy, Utils.port));
        //p=Proxy.NO_PROXY;
        HttpsURLConnection con = (HttpsURLConnection)url.openConnection(p);
        try {
            Thread.sleep(1);
        } catch (InterruptedException interruptedException) {
        }

        InputStream is =con.getInputStream();
        BufferedReader br=new BufferedReader(new InputStreamReader(is));
        String line;
        while((line=br.readLine())!=null){
            System.out.println(line);
        }
    }

    static String ispisiHtmlUnit(String s, boolean izbor){
        
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);                         // ovo sluzi za stopiranje warninga
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");        //
        String obsah=null;

        try (final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED,Utils.proxy,Utils.port)) {
            //try (final WebClient webClient = new WebClient()) {
            //webClient.getOptions().setThrowExceptionOnScriptError(false);
            //webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

            //final HtmlPage page = webClient.getPage("https://transparency.entsoe.eu/outage-domain/r2/unavailabilityOfProductionAndGenerationUnits/show?name=&defaultValue=false&viewType=TABLE&areaType=CTA&atch=false&dateTime.dateTime=23.01.2019+00:00|UTC|DAY&dateTime.endDateTime=25.01.2019+00:00|UTC|DAY&area.values=CTY|10YCS-SERBIATSOV!CTA|10YCS-SERBIATSOV&assetType.values=PU&assetType.values=GU&outageType.values=A54&outageType.values=A53&outageStatus.values=A05&masterDataFilterName=&masterDataFilterCode=&dv-datatable_length=10");
            final HtmlPage page = webClient.getPage(s);
            //final HtmlPage page = webClient.getPage("http://artis.argon.corp.ch:8080/stp/#/myDeals");

            final String pageAsXml;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                //Logger.getLogger(JavaApplication2.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(izbor==true) pageAsXml=page.asXml();
            else pageAsXml=page.asText();

            try {
                //final String pageAsXml = page.asXml();
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                //Logger.getLogger(JavaApplication2.class.getName()).log(Level.SEVERE, null, ex);
            }
            //System.out.println(pageAsXml);

            //final String pageAsText = page.asText();
            obsah=pageAsXml;

        } catch(IOException e){
            e.printStackTrace();
        }

        return obsah;
        //return "1";
    }

    static void napraviCSV2020_2030() throws IOException{
        BufferedWriter bw=new BufferedWriter(new FileWriter("exp.csv"));
        for(LocalDateTime l=LocalDateTime.of(2020,1,1,0,0); l.isBefore(LocalDateTime.of(2031,1,1,0,0)); l=l.plusHours(1)){
            bw.write(DateTimeFormatter.ofPattern("yyyy-MM-dd_").format(l)+"H"+(l.getHour()+1));
            bw.newLine();

        }
        bw.flush();
    }

    static int toExcelInt(LocalDate dalo){
        return (int) ChronoUnit.DAYS.between(LocalDate.of(1900,1,1), dalo.plusDays(2));
    }

    static String vreme(){
        DateTimeFormatter df=DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        return LocalDateTime.now().format(df);
    }

    static void odradiCSV(){
        while(true) {
            try (Connection con = DriverManager.getConnection(Utils.DAM_DB_LOCATION); Statement stm = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); BufferedWriter br = new BufferedWriter(new FileWriter(new File(Utils.CSV_DAM_2020_location)))) {
                //con.setAutoCommit(false);
                ResultSet rs=stm.executeQuery("SELECT * from DAM WHERE hour LIKE '%2022%'");
                String temp="";
                while(rs.next()){
                    try{
                        for(int i=1; i<100; i++){
                            temp=rs.getString(i);
                            br.write((temp==null ? "" : temp) +",");
                            //System.out.print(rs.getString(i)+"\t");
                        }
                    }catch(SQLException e){
                        br.newLine();
                        //System.out.println();
                    }
                }
                return;
            } catch (IOException | SQLException e) {
                System.out.println("Greska pri kreiranju DAM2022.csv fajla - " + e.getMessage());
                ts(5000);
            }
        }
    }

    static void odradiCSV_CAP(){
        System.out.println("Poco");
        while(true) {
            try (Connection con = DriverManager.getConnection(Utils.DB_LOCATION); Statement stm = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); BufferedWriter br = new BufferedWriter(new FileWriter(new File(Utils.CSV_CAP_2020_location)))) {
                //con.setAutoCommit(false);
                ResultSet rs=stm.executeQuery("SELECT * from cap_2020");
                String temp="";
                while(rs.next()){
                    try{
                        for(int i=1; i<200; i++){
                            temp=rs.getString(i);
                            br.write((temp==null ? "" : temp) +",");
                            //System.out.print(rs.getString(i)+"\t");
                        }
                    }catch(SQLException e){
                        br.newLine();
                        //System.out.println();
                    }
                }
                System.out.println("Zavrsio");
                return;
            } catch (IOException | SQLException e) {
                //System.out.println("Greska pri kreiranju CAP2020.csv fajla - " + e.getMessage());
                ts(5000);
            }
        }
    }

    public void run(){
        for(XXdam.Zemlja z: XXdam.Zemlja.values()){
            new PozivacDam(z);
        }
        if(XXdam.jeStogodUpisano) odradiCSV();
    }

    static void skiniSaGet(String urlS) throws IOException {
        ReadableByteChannel readableChannelForHttpResponseBody = null;
        FileChannel fileChannelForDownloadedFile = null;

        try {
            // Define server endpoint
            URL robotsUrl = new URL(urlS);
            Proxy p=new Proxy(Proxy.Type.HTTP,new InetSocketAddress(Utils.proxy, Utils.port));
            HttpURLConnection urlConnection = (HttpURLConnection) robotsUrl.openConnection(p);

            // Get a readable channel from url connection
            readableChannelForHttpResponseBody = Channels.newChannel(urlConnection.getInputStream());

            // Create the file channel to save file
            FileOutputStream fosForDownloadedFile = new FileOutputStream("robots.zip");
            fileChannelForDownloadedFile = fosForDownloadedFile.getChannel();

            // Save the body of the HTTP response to local file
            fileChannelForDownloadedFile.transferFrom(readableChannelForHttpResponseBody, 0, Long.MAX_VALUE);

        } catch (IOException ioException) {
            //System.out.println("IOException occurred while contacting server.");
            //ioException.printStackTrace();
            throw ioException;
        } finally {
            if (readableChannelForHttpResponseBody != null) {
                try {
                    readableChannelForHttpResponseBody.close();
                } catch (IOException ioe) {
                    System.out.println("Error while closing response body channel");
                }
            }
            if (fileChannelForDownloadedFile != null) {
                try {
                    fileChannelForDownloadedFile.close();
                } catch (IOException ioe) {
                    System.out.println("Error while closing file channel for downloaded file");
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        //Utils.p("aaa");

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            Class.forName("org.openqa.selenium.WebDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        while(true){
            try {
                new Main().run();
                //new ResultReader().run();
                //odradiCSV_CAP();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))+" Waiting...");
                try {
                    int ct;
                    LocalTime lt=LocalTime.now();
                    if(lt.isBefore(LocalTime.of(8,0,0)) || lt.isAfter(LocalTime.of(20,0,0))) ct=300_000;
                    else ct=60_000;
                    Thread.sleep(ct);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //ispisiPdf("https://eportal.cezdata.cz/mtprovider/genfile?dt=po&tp=je&from=1.11.2019&to=30.11.2019&lang=en");
        //anchory("https://www.cez.cz/en/power-plants-and-environment/remit.html");
        //anchory("https://www.insideinformation.hu/en/PubPages/newslistmain.aspx");
        //System.out.println(ispisiHtmlUnit("https://tge.pl/electricity-dam-tge-base?date_start=2020-01-06&iframe=1",true));
        //ispisiStranu("https://tge.pl/electricity-dam-tge-base?date_start=2020-01-06&iframe=1");

        //ispisiHtmlUnit("http://94.102.226.251",true);

        //System.out.println(new SRdam(LocalDate.of(2020,1,1)));
        /*while(true) {
            new EPEXdam(LocalDate.of(2020, 1, 7), "DE");
            new EPEXdam(LocalDate.of(2020, 1, 7), "AT");
        }

         */



        /*
        ScheduledExecutorService executor= Executors.newScheduledThreadPool(6);
        //ScheduledExecutorService executor= Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(new Main(), 0, 60, TimeUnit.SECONDS);
        executor.scheduleWithFixedDelay(new ResultReader(), 0, 10, TimeUnit.SECONDS);

        Scanner in=new Scanner(System.in);
        String prekid=in.nextLine();
        if(prekid.equalsIgnoreCase("stop")) {
            executor.shutdown();
            while(true){
                if(executor.isTerminated()) System.exit(0);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

         */




        //new ITdam(LocalDate.of(2020,2,1));




        //xlsTocsvConvert(new File("si.xlsx"),0);

        //System.out.println("00 - 01 \t€/MWh\t33.14\t37.05\t35.32\t29.30\t25.11\t37.52\t33.02".split("\t")[8]);

    }
}
