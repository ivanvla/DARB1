package com.company;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import static com.company.Main.vreme;

public class XXdam{
    static String dbAdresa=Utils.DAM_DB_LOCATION;
    static boolean jeStogodUpisano=false;

    enum Zemlja{
        HR, SR, HU, RO, GR, BG, SI, AU, DE, PL,CZ, SK, TR, IT_NORD /*IT_CSUD, IT_SUD*/
    }

    public static void snimiUBazu(String zemlja, double[] profile, LocalDate loda){
        double d=0;
        for(int i=0; i<24; i++) d+=Math.abs(profile[i]);
        if((d<1)) {
            System.out.println(vreme()+" ---DAM--- Data for "+zemlja+", for "+loda+" not available");
            return;
        }
        if(loda.isBefore(LocalDate.of(2019,12,31)) || loda.isAfter(LocalDate.of(2029,12,31))) return;

        int brPokusaja=0;
        while(true) {
            try (Connection con = DriverManager.getConnection(dbAdresa); Statement stm = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                con.setAutoCommit(false);
                for (int i = 0; i < 24; i++) {
                    String br=String.format("%.2f",profile[i]);
                    //System.out.println("UPDATE DAM SET " + zemlja + "=" + profile[i] + " WHERE hour=" + DateTimeFormatter.ofPattern("yyyy-MM-dd_").format(loda) + "H" + i + 1);
                    if(zemlja.equals(Zemlja.TR.name())){
                        LocalDateTime tldt=LocalDateTime.of(loda, LocalTime.of(0,0));
                        int tof=(int) Duration.between(tldt.atZone(ZoneId.of("Turkey")), tldt.atZone(ZoneId.of("Europe/Berlin"))).getSeconds()/3600;
                        ResultSet rs=stm.executeQuery("SELECT ID FROM DAM WHERE hour='"+ DateTimeFormatter.ofPattern("yyyy-MM-dd_").format(loda)+"H1'");
                        int poz;
                        if(rs.first()){
                            poz=rs.getInt(1);
                            int z=0;
                            for(int ii=poz-tof; ii<poz+24; ii++){
                                if(ii>=poz-tof+24) br=String.format("%.2f",profile[-24+z++]);
                                else br=String.format("%.2f",profile[z++]);
                                if(ii>0) {
                                    stm.executeUpdate("UPDATE DAM SET " + zemlja + "=" + br + " WHERE ID=" + ii);

                                }
                            }
                        }else return;
                        //Arrays.toString(profile);
                        con.commit();
                        zadnjiUpis(zemlja,loda);
                        return;
                    }
                    else stm.executeUpdate("UPDATE DAM SET " + zemlja + "=" + br + " WHERE hour='" + DateTimeFormatter.ofPattern("yyyy-MM-dd_").format(loda) + "H" + (i + 1)+"'");

                }
                con.commit();
                zadnjiUpis(zemlja, loda);
                break;
            } catch (SQLException e) {
                e.printStackTrace();
                //System.out.println(e.getMessage()+" Trying again...");
                brPokusaja++;
                if (brPokusaja>10) {
                    System.out.println("Unsuccessful attempt to write data for "+zemlja+", date "+loda);
                    break;
                }
                try {
                    Thread.sleep(10_000);
                } catch (InterruptedException ex) {
                    //e.printStackTrace();
                    System.out.println(ex.getMessage()+"    Trying again...");
                }
            }
        }

    }

    public static void zadnjiUpis(String zemlja, LocalDate ld){
        try (Connection con = DriverManager.getConnection(dbAdresa); Statement stm = con.createStatement();) {
            stm.executeUpdate("UPDATE DAM_LW SET "+(zemlja.equals("AT") ? "AU" : zemlja)+"='"+ld+"' WHERE id=1");
            System.out.println(vreme()+" ---DAM--- Prices "+zemlja+" "+ld+" written into DB");
            jeStogodUpisano=true;

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
