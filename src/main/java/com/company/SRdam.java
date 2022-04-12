package com.company;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import static com.company.Main.vreme;

class SRdam {
    String link;
    String[] sadrzaj;
    double[] profile=new double[24];

    SRdam(LocalDate ld){
        if(ld.equals(LocalDate.now().plusDays(1)) && LocalTime.now().isBefore(LocalTime.of(11,0))) return;
        link="http://seepex-spot.rs/sr/market-data/day-ahead-auction/" +
                ld.getYear() + "-" + (ld.getMonthValue()<10 ? "0"+ld.getMonthValue() : ld.getMonthValue()) + "-" + (ld.getDayOfMonth()<10 ? "0"+ld.getDayOfMonth() : ld.getDayOfMonth()) +
                "/RS";
        //System.out.println(link);
        try {
            sadrzaj=Main.ispisiHtmlUnit(link,false).replace(",",".").split("\n");
            Arrays.stream(sadrzaj).forEach(System.out::println);
        } catch (Exception e) {
            System.out.println(vreme()+" --- DAM --- ERROR: Data for SR, for "+ld+" not read ("+e.getMessage()+")");
            return;
        }
        for(int i=0; i<sadrzaj.length; i++){
            if(sadrzaj[i].contains("Sati")){
                while(!sadrzaj[i].contains("00 - 01")) i++;
                for(int x=1; x<=24; x++){
                    try {
                        profile[x-1]=Double.parseDouble(sadrzaj[i].split("\t")[8].trim());
                    } catch (NumberFormatException e) {
                        profile[x-1]=0;
                    }
                    i+=2;
                }
            }
        }
        //for(int i=0; i<24; System.out.println(profile[i++]));
        XXdam.snimiUBazu("SR", profile, ld);
    }

}
