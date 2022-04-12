package com.company;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.LocalDate;
import java.time.LocalTime;

class TRdam{
    double[] profile=new double[24];

    TRdam(LocalDate ld){
        if(ld.equals(LocalDate.now().plusDays(1)) && LocalTime.now().isBefore(LocalTime.of(13,0))) return;
        System.setProperty("webdriver.chrome.driver","chromedriver.exe");    //    za CHROME
        //System.setProperty("webdriver.gecko.driver","C:\\Users\\vlajic_i\\IdeaProjects\\untitled\\selenium-java-3.141.59\\geckodriver.exe");  //  za FIREFOX
        String datum=""+(ld.getDayOfMonth()<10 ? "0"+ld.getDayOfMonth() : ld.getDayOfMonth())+"."+(ld.getMonthValue()<10 ? "0"+ld.getMonthValue() : ld.getMonthValue())+"."+ld.getYear();
        WebDriver driver=new ChromeDriver();
        driver.get("https://seffaflik.epias.com.tr/transparency/piyasalar/gop/ptf.xhtml");
        Main.ts(1000);
        //driver.findElement(By.xpath("//span/input")).click();
        //driver.findElement(By.xpath("//span/input")).click();
        //driver.findElement(By.xpath("//form/ul/li[2]/a")).click();
        driver.findElement(By.xpath("//span/input")).sendKeys("");
        driver.findElement(By.xpath("//span/input")).sendKeys(datum);
        Main.ts(1000);
        //driver.findElement(By.id("j_idt195:date2_input")).clear();
        //driver.findElement(By.xpath("j_idt195:date2_input")).click();
        driver.findElement(By.xpath("//div[2]/div[2]/span/input")).sendKeys("");
        driver.findElement(By.xpath("//div[2]/div[2]/span/input")).sendKeys(datum);
        Main.ts(1000);
        driver.findElement(By.xpath("//button/span")).click();
        String[] s=driver.getPageSource().replace("><",">\n<").replaceAll(",",".").split("\n");
        int i=0;
        String sat=(i<10 ? "0"+i : ""+i)+":00";
        for(int x=0; x<s.length; x++){
            if(s[x].contains(sat)){
                try {
                    profile[i]=Double.parseDouble(s[x+3].split(">")[1].split("<")[0].trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }
                i++;
                sat=(i<10 ? "0"+i : ""+i)+":00";
            }
        }
        driver.close();
        XXdam.snimiUBazu("TR", profile, ld);
    }
}
