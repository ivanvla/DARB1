package com.company;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Level;

import static com.company.Main.vreme;

class BGdam{
    double[] profile=new double[24];

    BGdam(LocalDate ld) {
        if(ld.equals(LocalDate.now().plusDays(1)) && LocalTime.now().isBefore(LocalTime.of(12,42))) return;
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);                         // ovo sluzi za stopiranje warninga
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        try (final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED, "10.145.203.17", 8080)) {
            final HtmlPage page = webClient.getPage("http://umm.ibex.bg/en/market-data/dam/prices-and-volumes/");
            final HtmlTable table = (HtmlTable) page.getByXPath(("//table[@class='calculations-table']")).get(2);
            //String[][] tablica=new String[table.getRowCount()][table.getRow(0).getCells().size()];
            String[][] tablica=new String[table.getRowCount()][table.getRow(2).getCells().size()];
            int x=0;
            int y=0;
            for (final HtmlTableRow row : table.getRows()) {
                for (final HtmlTableCell cell : row.getCells()) {
                    tablica[x][y++]=(cell.asText().contains("\r") ? cell.asText().split("\r")[0].trim() : cell.asText());
                }
                y=0;
                x++;
            }

            int b=0;
            for(int j=0; j<tablica[0].length; j++) if(
                    tablica[0][j].contains("/") &&
                            tablica[0][j].split("/")[0].contains(""+(ld.getMonthValue()<10 ? "0"+ld.getMonthValue() : ld.getMonthValue())) &&
                            tablica[0][j].split("/")[1].contains(""+(ld.getDayOfMonth()<10 ? "0"+ld.getDayOfMonth() : ld.getDayOfMonth()))
            ) b=j;
            if(b>0){
                for(int i=0; i<24; i++) {
                    try {
                        profile[i] = Double.parseDouble(tablica[i + 1][b].trim());
                    } catch (NumberFormatException e) {
                        //System.out.println(e.getMessage());
                        profile[i]=0;
                    }
                }
            }

        } catch (IOException e) {
            System.out.println(vreme()+" --- DAM --- ERROR: Data for BG, for "+ld+" not read ("+e.getMessage()+")");
            return;
        }
        //for(int i=0; i<24; System.out.println(profile[i++]));
        XXdam.snimiUBazu("BG", profile, ld);
    }

}
