package com.company;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.company.Main.vreme;

@Deprecated
class PLdam{
    String link;
    String[] sadrzaj;
    double[] profile=new double[24];
    String eic1;
    String eic2;

    PLdam(LocalDate ld){
        if(ld.equals(LocalDate.now().plusDays(1)) && LocalTime.now().isBefore(LocalTime.of(12,42))) return;
        //link="https://www.epexspot.com/en/market-data?market_area="+z+"&trading_date="+dat(ld.minusDays(1))+"&delivery_date="+dat(ld)+"&underlying_year=&modality=Auction&sub_modality=DayAhead&product=60&data_mode=table&period=";
        eic1="10YPL-AREA-----S";
        eic2="10YPL-AREA-----S";

        link="https://transparency.entsoe.eu/transmission-domain/r2/dayAheadPrices/show?name=&defaultValue=true&viewType=TABLE&areaType=BZN&atch=false&dateTime.dateTime="+datt(ld)+"+00:00|CET|DAY&biddingZone.values=CTY|"+eic1+"!BZN|"+eic2+"&resolution.values=PT60M&dateTime.timezone=CET_CEST&dateTime.timezone_input=CET+(UTC+1)+/+CEST+(UTC+2)";
        //    https://transparency.entsoe.eu/transmission-domain/r2/dayAheadPrices/show?name=&defaultValue=true&viewType=TABLE&areaType=BZN&atch=false&dateTime.dateTime=29.01.2021  +00:00|CET|DAY&biddingZone.values=CTY|10YPL-AREA-----S!BZN|&dateTime.timezone=CET_CEST&dateTime.timezone_input=CET+(UTC+1)+/+CEST+(UTC+2)
        try {
            sadrzaj=Main.ispisiHtmlUnit(link,false).split("\n");
        } catch (Exception e) {
            //System.out.println(vreme()+" ---DAM--- ERROR with "+"PL"+" for "+ld+" ("+e.getMessage()+")" );
            e.printStackTrace();
            return;
        }
        int p=0;
        while(!sadrzaj[p].contains("MWh")) p++;
        while(!sadrzaj[p].contains(".")) p++;
        try{
            for(int i=0; i<24; i++){
                profile[i]=Double.parseDouble(sadrzaj[p].split("\t")[1].trim());
                p++;
            }
            XXdam.snimiUBazu("PL", profile, ld);
            //for(int i=0; i<24; i++) System.out.println(profile[i]);

        }catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
            System.out.println(vreme()+" ---DAM--- Data for "+"PL"+", for "+ld+" not available");
        }
    }

    static String dat(LocalDate lud){
        return lud.getYear() + "-" + (lud.getMonthValue()<10 ? "0"+lud.getMonthValue() : lud.getMonthValue()) + "-" + (lud.getDayOfMonth()<10 ? "0"+lud.getDayOfMonth() : lud.getDayOfMonth());
    }

    static String datt(LocalDate lud){
        return (lud.getDayOfMonth()<10 ? "0"+lud.getDayOfMonth() : lud.getDayOfMonth())+"."+(lud.getMonthValue()<10 ? "0"+lud.getMonthValue() : lud.getMonthValue())+"."+lud.getYear();
    }

}
