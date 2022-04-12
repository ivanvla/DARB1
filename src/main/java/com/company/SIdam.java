package com.company;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;

class SIdam{
    double[] profile=new double[24];
    LocalDate lod;
    String url_SI_DAM="https://www.bsp-southpool.com/day-ahead-trading-results-si.html?file=files/documents/trading/MarketResultsAuction.xlsx&cid=1291";

    SIdam(LocalDate ld){
        if(ld.equals(LocalDate.now().plusDays(1)) && LocalTime.now().isBefore(LocalTime.of(12,42))) return;
        lod=ld;
        System.setProperty("http.proxySet", "true");
        System.setProperty("http.proxyHost", "10.145.203.17");
        System.setProperty("http.proxyPort", "8080");
        profile=read();
        XXdam.snimiUBazu("SI", profile, ld);
    }

    double[] read(){
        //String d1;
        String linija;
        String grFajl="temp\\tempSI.xlsx";
        boolean uspelo1=false;
        try{
            //System.out.println("Djes");
            Files.createDirectories(Paths.get("temp"));
            Main.skiniSaGet(url_SI_DAM,grFajl);
            uspelo1=true;
        }catch(IOException e){
            //System.out.println(LocalDateTime.now()+" ---DAM--- "+"No data for GR, for "+lod);
            if(!(e instanceof FileNotFoundException)){
                e.printStackTrace();
            }
        }

        if(uspelo1){
            //System.out.println("uspelo");
            Path skaka=Main.xlsTocsvConvert(new File(grFajl), lod.getMonthValue()-1).toPath();
            try(BufferedReader bufR=new BufferedReader(new FileReader(skaka.toString()))){
                while(!(linija=bufR.readLine()).contains(""+Main.toExcelInt(lod))) if(linija==null) break;
                //System.out.println("zdrao");
                if(linija.contains(""+Main.toExcelInt(lod))){
                    for(int ii=0; ii<24; ii++) {
                        try {
                            profile[ii]=Double.parseDouble(linija.split(",")[ii+3]);
                            //System.out.println("ssd");
                        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                            //System.out.println("catch");
                            profile[ii]=0;
                        }
                    }
                }

            }catch(IOException e){
                e.printStackTrace();
            }
        }
        //System.out.println("evo ga");
        return profile;
    }
}