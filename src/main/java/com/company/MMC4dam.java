package com.company;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.company.Main.vreme;

@Deprecated
class MMC4dam{
    String link;
    String[] sadrzaj;
    double[][] profile=new double[4][24];

    MMC4dam(LocalDate ld){
        if(ld.equals(LocalDate.now().plusDays(1)) && LocalTime.now().isBefore(LocalTime.of(11,40))) return;
        link="https://www.ote-cr.cz/cs/kratkodobe-trhy/elektrina/market-coupling?date="+ld.getYear() + "-" + (ld.getMonthValue()<10 ? "0"+ld.getMonthValue() : ld.getMonthValue()) + "-" + (ld.getDayOfMonth()<10 ? "0"+ld.getDayOfMonth() : ld.getDayOfMonth());
        try {
            sadrzaj=Main.ispisiHtmlUnit(link,false).replace(",",".").split("\n");
        } catch (Exception e) {
            System.out.println(vreme()+" --- DAM --- ERROR: Data for 4MMC, for "+ld+" not read ("+e.getMessage()+")");
            return;
        }
        int p=0;
        while(!sadrzaj[p].contains("Hodina")) p++;
        while(!sadrzaj[p].contains("1")) p++;
        try {
            for(int i=0; i<24; i++){
                for(int j=0; j<4; j++){
                    profile[j][i]=Double.parseDouble(sadrzaj[p].split("\t")[j+1].trim());
                }
                p++;
            }
            XXdam.snimiUBazu("CZ", profile[0], ld);
            XXdam.snimiUBazu("SK", profile[1], ld);
            XXdam.snimiUBazu("HU", profile[2], ld);
            XXdam.snimiUBazu("RO", profile[3], ld);

        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println(vreme()+" ---DAM--- Data for 4MMC, for "+ld+" not available");
        }
    }
}
