package com.company;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PozivacDam{
    LocalDate ltm=null;
    PozivacDam(XXdam.Zemlja z){
        try (Connection con = DriverManager.getConnection(XXdam.dbAdresa); Statement stm = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);) {
            ResultSet rs=stm.executeQuery("SELECT "+z.name()+" FROM DAM_LW WHERE id=1");
            if(rs.first()) ltm=LocalDate.parse(rs.getString(1));
        }catch (SQLException e) {
            e.printStackTrace();
        }
        if(ltm!=null){
            LocalDate dokle= LocalDateTime.now().getHour()<10 ? LocalDate.now() : LocalDate.now().plusDays(1);
            if(ltm.equals(dokle)) return;
            List<LocalDate> list=new ArrayList<>();
            for(LocalDate ild=ltm.plusDays(1); ild.isBefore(dokle.plusDays(1)); ild=ild.plusDays(1)){
                list.add(ild);
            }
            for(LocalDate ldx: list) {
                if(z.equals(XXdam.Zemlja.HR)) {
                    //System.out.println("Checking HR DAM...");
                    //new HRdam(ldx);                       - PROVERITI NESTO ZEZAJU CENE SA SAJTA!!!
                    new EPEXdamNew(ldx, z.name());
                }
                if(z.equals(XXdam.Zemlja.SR)) {
                    //System.out.println("Checking SR DAM...");
                    new SRdam(ldx);
                }
                if(z.equals(XXdam.Zemlja.CZ) || z.equals(XXdam.Zemlja.SK) || z.equals(XXdam.Zemlja.HU) || z.equals(XXdam.Zemlja.RO) || z.equals(XXdam.Zemlja.GR)) {
                    //System.out.println("Checking HU DAM...");
                    new EPEXdamNew(ldx, z.name());
                }

                if(z.equals(XXdam.Zemlja.BG)) {
                    //System.out.println("Checking BG DAM...");
                    //new BGdam(ldx);
                    new EPEXdamNew(ldx, z.name());
                }
                if(z.equals(XXdam.Zemlja.AU)) {
                    //System.out.println("Checking AT DAM...");
                    new EPEXdamNew(ldx, "AT");
                }
                if(z.equals(XXdam.Zemlja.DE)) {
                    //System.out.println("Checking DE DAM...");
                    new EPEXdamNew(ldx, "DE");
                }
                if(z.equals(XXdam.Zemlja.SI)) {
                    //System.out.println("Checking SI DAM...");
                    new SIdam(ldx);
                }
                if(z.equals(XXdam.Zemlja.TR)) {
                    //System.out.println("Checking SI DAM...");
                    new TRdam(ldx);
                }
                if(z.equals(XXdam.Zemlja.PL)) {
                    //System.out.println("Checking SI DAM...");
                    new EPEXdamNew(ldx, "PL");
                }
                if(z.equals(XXdam.Zemlja.IT_NORD)) {
                    //System.out.println("Checking SI DAM...");
                    new ITdam(ldx);
                }
            }
        }
    }
}
