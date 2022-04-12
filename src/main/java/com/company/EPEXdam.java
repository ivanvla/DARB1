package com.company;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.company.Main.vreme;

@Deprecated
class EPEXdam{
    String link;
    String[] sadrzaj;
    double[] profile=new double[24];
    String eic1;
    String eic2;

    EPEXdam(LocalDate ld, String z){
        if(ld.equals(LocalDate.now().plusDays(1)) && LocalTime.now().isBefore(LocalTime.of(12,42))) return;
        //link="https://www.epexspot.com/en/market-data?market_area="+z+"&trading_date="+dat(ld.minusDays(1))+"&delivery_date="+dat(ld)+"&underlying_year=&modality=Auction&sub_modality=DayAhead&product=60&data_mode=table&period=";
        //link="https://transparency.entsoe.eu/transmission-domain/r2/dayAheadPrices/show?name=&defaultValue=true&viewType=TABLE&areaType=BZN&atch=false&dateTime.dateTime=16.04.2021  +00:00|CET|DAY&biddingZone.values=CTY|10Y1001A1001A83F!BZN|10Y1001A1001A82H&resolution.values=PT60M&dateTime.timezone=CET_CEST&dateTime.timezone_input=CET+(UTC+1)+/+CEST+(UTC+2)
        //link="https://transparency.entsoe.eu/transmission-domain/r2/dayAheadPrices/show?name=&defaultValue=true&viewType=TABLE&areaType=BZN&atch=false&dateTime.dateTime="+datt(ld)+"+00:00|CET|DAY&biddingZone.values=CTY|"+eic1+"!BZN|"+eic2+"                                        &dateTime.timezone=CET_CEST&dateTime.timezone_input=CET+(UTC+1)+/+CEST+(UTC+2)";
        if(z.equals("CZ")) {
            eic1="10YCZ-CEPS-----N";
            eic2="10YCZ-CEPS-----N";
        }
        else if(z.equals("SK")) {
            eic1="10YSK-SEPS-----K";
            eic2="10YSK-SEPS-----K";
        }
        else if(z.equals("HU")) {
            eic1="10YHU-MAVIR----U";
            eic2="10YHU-MAVIR----U";
        }
        else if(z.equals("RO")) {
            eic1="10YRO-TEL------P";
            eic2="10YRO-TEL------P";
        }
        else if(z.equals("GR")) {
            eic1="10YGR-HTSO-----Y";
            eic2="10YGR-HTSO-----Y";
        }
        else if(z.equals("AT")) {
            eic1="10YAT-APG------L";
            eic2="10YAT-APG------L";
        }
        else {
            eic1="10Y1001A1001A83F";
            eic2="10Y1001A1001A82H";
        }
        link="https://transparency.entsoe.eu/transmission-domain/r2/dayAheadPrices/show?name=&defaultValue=true&viewType=TABLE&areaType=BZN&atch=false&dateTime.dateTime="+datt(ld)+"+00:00|CET|DAY&biddingZone.values=CTY|"+eic1+"!BZN|"+eic2+"&resolution.values=PT60M&dateTime.timezone=CET_CEST&dateTime.timezone_input=CET+(UTC+1)+/+CEST+(UTC+2)";
        try {
            sadrzaj=Main.ispisiHtmlUnit(link,false).split("\n");
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println(vreme()+" ---DAM--- ERROR with "+z+" for "+ld+" ("+e.getMessage()+")" );
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
            XXdam.snimiUBazu(z, profile, ld);
            //for(int i=0; i<24; i++) System.out.println(profile[i]);

        }catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
            System.out.println(vreme()+" ---DAM--- Data for "+z+", for "+ld+" not available");
        }
    }

    static String dat(LocalDate lud){
        return lud.getYear() + "-" + (lud.getMonthValue()<10 ? "0"+lud.getMonthValue() : lud.getMonthValue()) + "-" + (lud.getDayOfMonth()<10 ? "0"+lud.getDayOfMonth() : lud.getDayOfMonth());
    }

    static String datt(LocalDate lud){
        return (lud.getDayOfMonth()<10 ? "0"+lud.getDayOfMonth() : lud.getDayOfMonth())+"."+(lud.getMonthValue()<10 ? "0"+lud.getMonthValue() : lud.getMonthValue())+"."+lud.getYear();
    }

}
