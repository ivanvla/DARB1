package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

class GRdamNew{
    double[] profile=new double[24];
    LocalDate lod;

    GRdamNew(LocalDate ld){
        if(ld.equals(LocalDate.now().plusDays(1)) && LocalTime.now().isBefore(LocalTime.of(12,45))) return;
        lod=ld;
        profile=read();
        XXdam.snimiUBazu("GR", profile, ld);
    }

    double[] read(){
        try {
            Files.list(Paths.get(Utils.HENEX_LOCATION)).forEach(x -> {
                //LocalDate lg=LocalDate.parse(x.getFileName().toString().split("_")[2].substring(0,8),DateTimeFormatter.ofPattern("yyyyMMdd"));
                LocalDate lg= null;
                try {
                    lg = LocalDateTime.ofInstant(Files.getLastModifiedTime(x).toInstant(), ZoneId.of("Europe/Paris")).toLocalDate().plusDays(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(lod.equals(lg)){
                    boolean b=false;
                    int i=0;
                    for(String s : Objects.requireNonNull(Utils.excelToList(x.toFile(), "Results"))){
                        if(s.contains("Price")) {
                            b=true;
                            continue;
                        }
                        if(b){
                            profile[i++] = Double.parseDouble(s.split(" && ")[1].trim().replace(",","."));
                            if(i==24) break;
                        }

                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profile;
    }
}
