package com.company;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class ITdam{
    double[][] profile=new double[3][24];

    ITdam(LocalDate ld){
        if(ld.equals(LocalDate.now().plusDays(1)) && LocalTime.now().isBefore(LocalTime.of(13,0))) return;
        //double[][] profile=new double[3][24];
        String date=""+(ld.getDayOfMonth()<10 ? "0"+ld.getDayOfMonth() : ld.getDayOfMonth())+"/"+(ld.getMonthValue()<10 ? "0"+ld.getMonthValue() : ld.getMonthValue())+"/"+ld.getYear();
        String day=ld.getDayOfMonth()<10 ? "0"+ld.getDayOfMonth() : ""+ld.getDayOfMonth();
        String month=ld.getMonthValue()<10 ? "0"+ld.getMonthValue() : ""+ld.getMonthValue();
        String year=""+ld.getYear();
        String userName=System.getProperty("user.name");
        String dwldDir="C:\\Users\\"+userName+"\\Downloads";
        System.setProperty("webdriver.chrome.driver","chromedriver.exe");    //    za CHROME
        WebDriver driver=new ChromeDriver();
        driver.get("https://www.mercatoelettrico.org/En/Download/DownloadDati.aspx?val=MGP_Prezzi");
        if(driver.getPageSource().contains("accept")){
            driver.findElement(By.id("ContentPlaceHolder1_CBAccetto1")).click();
            driver.findElement(By.id("ContentPlaceHolder1_CBAccetto2")).click();
            Main.ts(1000);
            while (true) {
                try {
                    driver.findElement(By.id("ContentPlaceHolder1_Button1")).click();
                    break;
                } catch (Exception e) {
                    Utils.p(e.getMessage());
                    Main.ts(500);
                }
            }
            driver.findElement(By.id("ContentPlaceHolder1_tbDataStart")).sendKeys(date);
            driver.findElement(By.id("ContentPlaceHolder1_tbDataStop")).sendKeys(date);
            driver.findElement(By.id("ContentPlaceHolder1_btnScarica")).click();
            int brc=0;
            while(true){
                if(Files.exists(Paths.get(dwldDir+"\\MGP_Prezzi"+year+month+day+year+month+day+".zip"))) {
                    driver.close();
                    profile=zipper(dwldDir+"\\MGP_Prezzi"+year+month+day+year+month+day+".zip");
                    XXdam.snimiUBazu("IT_NORD", profile[0], ld);
                    XXdam.snimiUBazu("IT_CSUD", profile[1], ld);
                    XXdam.snimiUBazu("IT_SUD", profile[2], ld);
                    break;
                }
                Main.ts(500);
                brc++;
                if(brc>10){
                    try {
                        driver.switchTo().alert().accept();
                    } catch (Exception e) {

                    }
                    driver.close();
                    break;
                }
            }


        }
    }

    static double[][] zipper(String fajl){
        String ispad;
        ZipFile zipFile=null;
        double[][] prfl=new double[3][24];
        try {
            zipFile = new ZipFile(fajl);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Enumeration<? extends ZipEntry> entries=null;
        if (zipFile!=null) entries = zipFile.entries();

        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            try {
                InputStream stream = zipFile.getInputStream(entry);
                BufferedReader br=new BufferedReader(new InputStreamReader(stream));
                String s;
                int r=0;
                while((s=br.readLine())!=null){
                    if(s.contains("<Ora>")){
                        r=Integer.parseInt(izmedju(s));
                        while(!(s=br.readLine()).contains("Prezzi")){
                            if(s.contains("SLOV")) prfl[0][r-1]=Double.parseDouble(izmedju(s).replace(",","."));
                            if(s.contains("MONT")) prfl[1][r-1]=Double.parseDouble(izmedju(s).replace(",","."));
                            if(s.contains("GREC")) prfl[2][r-1]=Double.parseDouble(izmedju(s).replace(",","."));
                        }

                    }
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return prfl;

    }

    static String izmedju(String se){
        return se.split(">")[1].split("<")[0].trim();
    }
}
