package com.company;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


class ResultReader implements Runnable{

    public static final int DELAY1=5000;    // Koliko ceka za proveru da li je stigao neki fajl
    List<Kapac> liKa;

    public boolean decodeCap(List<String> l, LocalDate localDate){
        liKa=new ArrayList<>();
        int i=0;
        String s;
        String[] ss;
        Kapac kp;
        while(i<l.size()){
            s=l.get(i);
            if(s.contains("-")){
                kp=new Kapac();
                kp.cena=new double[24];
                kp.kap=new double[24];
                ss=s.split(" && ")[0].trim().toUpperCase().split("-");
                if(ss.length==2) kp.setGranica(Zemlja.valueOf(ss[0].contains("50HZT")?"D5":ss[0]),Zemlja.valueOf(ss[1].contains("50HZT")?"D5":ss[1]));
                if(kp.granica.in.name().equalsIgnoreCase("UA") || kp.granica.out.name().equalsIgnoreCase("UA")) {i++; continue;};
                i++;
                while(!l.get(i).contains("MW")) i++;
                ss=l.get(i).split(" && ");
                for(int j=0;j<24;j++) {
                    try{
                        kp.kap[j]=Double.parseDouble(ss[j+1]);
                    }catch(NumberFormatException e){
                        kp.kap[j]=0;
                    }
                }
                i++;
                while(!l.get(i).contains("Price")) i++;
                ss=l.get(i).split(" && ");
                for(int j=0;j<24;j++) {
                    try{
                        kp.cena[j]=Double.parseDouble(ss[j+1]);
                    }catch(NumberFormatException e){
                        kp.cena[j]=0;
                    }
                }
                kp.ld=localDate;
                //Utils.p(kp);
                liKa.add(kp);
            }
            i++;
        }
        if(liKa.isEmpty()) return false;
        else return true;
    }


    public boolean writeToDB() throws SQLException {
        Connection conn = DriverManager.getConnection(Utils.DB_LOCATION);
        conn.setAutoCommit(false);
        //System.out.println(stmt.executeUpdate("UPDATE table_2019 SET SRRO_E=0 WHERE Hour='2019010101'"));
        String godina;
        String smer;
        String hour;
        String str;
        String tmp;
        boolean succ=false;
        PreparedStatement ps;
        for(Kapac k:liKa) {
            godina=""+k.ld.getYear();
            smer=k.granica.out.ime+k.granica.in.ime;
            str="UPDATE cap_"+godina+" SET "+smer+"_M=?, "+smer+"_E=? WHERE Hour=?";
            ps=conn.prepareStatement(str);
            for(int i=0;i<24;i++) {
                //tmp=i<9 ? "0"+(i+1) : ""+(i+1);
                tmp=""+(i+1);
                hour=k.ld.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+"_H"+tmp;
                ps.setDouble(1, k.kap[i]);
                ps.setDouble(2, k.cena[i]);
                ps.setString(3, hour);
                ps.executeUpdate();
            }
            succ=true;
            Utils.p(LocalDateTime.now()+" ---CAP--- Cap written to DB for "+k.granica.out.ime+"->"+k.granica.in.ime+", "+k.ld);
        }
        conn.commit();
        conn.close();



        return succ;


    }

    public void read() {
        Comparator<Path> comp= Utils::cmpr;
        Predicate<Path> pred= x->!Files.isDirectory(x) && (x.toString().endsWith(".xls") || x.toString().endsWith(".xlsm") || x.toString().endsWith(".xlsx"));
        Consumer<Path> con= y->{
            LocalDate loda=null;
            try{
                loda= LocalDateTime.ofInstant(Files.getLastModifiedTime(y).toInstant(), ZoneId.systemDefault()).toLocalDate().plusDays(1);
            }catch(IOException e){
                Utils.log(e);
            }
            if(pred.test(y)) {
                List<String> lu=Utils.excelToList(y.toFile(), "OTL");
                if (lu==null) {
                    try {
                        if(Utils.excelToList(y.toFile(), 0).get(0).split(" && ")[0].equalsIgnoreCase("Capacity bidding")) Files.move(y, y.getParent().getParent().resolve("input_atc").resolve(y.getFileName()), REPLACE_EXISTING);
                        else Files.delete(y);
                        //throw new IOException();
                    } catch (IOException e) {
                        Utils.log(e);
                    } catch (IndexOutOfBoundsException e){
                        if(y.getFileName().toString().contains("HENEX")) {
                            try {
                                Files.move(y, y.getParent().getParent().resolve("henex").resolve(y.getFileName()), REPLACE_EXISTING);
                            } catch (IOException ex) {
                                Utils.log(e);
                            }
                        } else {
                            try {
                                Files.delete(y);
                            } catch (IOException ex) {
                                Utils.log(e);
                            }
                        }
                    }
                    return;
                }
                if (decodeCap(lu,loda)) {
                    try {
                        //if (writeToDB()) Files.delete(y);
                        if (writeToDB()) Files.move(y, y.getParent().getParent().resolve("input_atc").resolve(y.getFileName()), REPLACE_EXISTING);
                    } catch (IOException | SQLException e) {
                        Utils.log(e);
                    }
                } else {
                    Utils.p("ERROR - File " + y + " cannot be processed - " + LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
                    try {
                        Files.delete(y);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                try {
                    Files.delete(y);
                } catch (IOException e) {
                    Utils.log(e);
                }
            }
        };

        try {
            Files.list(Utils.CAP_LOCATION).sorted(comp).forEach(con);
        } catch (IOException e) {
            Utils.log(e);
        }
    }

    public void run() {
        read();
    }
     /*   while(true){
            read();
            Utils.p("Waiting...");
            try {
                Thread.sleep(DELAY1);
            } catch (InterruptedException e) {
                Utils.log(e);
            }
        }
    }*/

}

enum Zemlja{
    RS("SR"),SR("SR"),HR("HR"),MK("MK"),GR("GR"),HU("HU"),RO("RO"),BG("BG"),CG("ME"),ME("ME"),AL("AL"),BH("BH"),SI("SI"),AT("AT"),CZ("CZ"),SK("SK"),D5("FH"),DE("TE"),PL("PL"),TR("TR"),UA("UA"),IT("IT");
    String ime;
    Zemlja(String s){
        ime=s;
    }
}

class Granica{
    Zemlja out;
    Zemlja in;
    Granica(Zemlja out,Zemlja in){
        this.out=out;
        this.in=in;
    }
}

class Kapac{

    Granica granica;
    LocalDate ld;
    double[] kap;
    double[] cena;


    Kapac(Granica granica, LocalDate ld, double[] kap, double[] cena) {
        this.granica = granica;
        this.ld = ld;
        this.kap = kap;
        this.cena = cena;
    }
    Kapac(){
        this(null,null,null,null);
    }

    public Kapac setGranica(Zemlja out, Zemlja in){
        if(granica!=null) {
            granica.out = out;
            granica.in = in;
        }else granica=new Granica(out,in);
        return this;
    }

    public Kapac setDate(LocalDate l){
        ld=l;
        return this;
    }

    @Override
    public String toString(){
        if(granica==null || ld==null) return "Kapacitet sa nedefinisanom granicom ili datumom";
        else return "Kapacitet "+granica.out.name()+"-"+granica.in.name()+" za dan "+ld.format(DateTimeFormatter.ISO_DATE)+"\n"+
                "MWh \t"+ Arrays.toString(kap)+"\n"+
                "Price \t"+Arrays.toString(cena);
    }

    public boolean hasBasic(){
        if(
                granica!=null &&
                        kap!=null &&
                        cena!=null
        ) return true;
        return false;
    }

    void setOutIn(String s1, String s2){
        try {
            granica.out=Zemlja.valueOf(s1);
            granica.in=Zemlja.valueOf(s2);
        } catch (IllegalArgumentException e) {
            Utils.log(e);
        }
    }
}
