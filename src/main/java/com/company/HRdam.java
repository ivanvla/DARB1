package com.company;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.company.Main.vreme;

class HRdam {
    String link;
    String[] sadrzaj;
    double[] profile=new double[24];

    HRdam(LocalDate ld){
        if(ld.equals(LocalDate.now().plusDays(1)) && LocalTime.now().isBefore(LocalTime.of(12,42))) return;
        link="https://www.cropex.hr/hr/trgovanja/dan-unaprijed-trziste/rezultati-dan-unaprijed-trzista.html";
        try {
            sadrzaj=Main.ispisiHtmlUnit(link, true).replace(",", ".").split("\n");
        } catch (Exception e) {
            System.out.println(vreme()+" --- DAM --- ERROR: Data for HR, for "+ld+" not read ("+e.getMessage()+")");
            return;
        }
        String datumce=""+ld.getYear()+ (ld.getMonthValue()<10 ? "0"+ld.getMonthValue() : ld.getMonthValue()) + (ld.getDayOfMonth()<10 ? "0"+ld.getDayOfMonth() : ld.getDayOfMonth());
        int j=0;
        outer: for(int i=1; i<=24; i++){
            while(!sadrzaj[j].contains(datumce+"-"+i)) {
                j++;
                if(j>=sadrzaj.length) break outer;
            }
            //System.out.println("sdsdsd");
            while(!sadrzaj[j].contains(".")) j++;
            try {
                profile[i-1]=Double.parseDouble(sadrzaj[j].trim());
            } catch (NumberFormatException e) {
                profile[i-1]=0;
            }
        }
        //for(int i=0; i<24; System.out.println(profile[i++]));
        XXdam.snimiUBazu("HR", profile, ld);
    }
}
