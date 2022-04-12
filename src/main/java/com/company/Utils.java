package com.company;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Utils{
    public static String DB_LOCATION="jdbc:ucanaccess://\\\\alpotldbs1p\\scheduling\\sc_otl10\\SCHEDULING\\Trading\\DARB\\CAP2020-2030.accdb";
    public static String DAM_DB_LOCATION="jdbc:ucanaccess://\\\\alpotldbs1p\\scheduling\\sc_otl10\\SCHEDULING\\Trading\\DARB\\DAM2020-2030.accdb";
    public static String HENEX_LOCATION="\\\\alpotldbs1p\\scheduling\\sc_otl10\\SCHEDULING\\Trading\\DARB\\henex";
    //public static String DB_LOCATION="jdbc:ucanaccess://CAP2020-2030.accdb";
    //public static String DAM_DB_LOCATION="jdbc:ucanaccess://DAM2020-2030.accdb";
    public static String REMIT_RAW_DB_LOCATION="jdbc:ucanaccess://remit.accdb";
    public static String REMIT_MAIN_DB_LOCATION="jdbc:derby:ivanche";
    public static Path CAP_LOCATION= Paths.get("\\\\alpotldbs1p/scheduling/sc_otl10/SCHEDULING/Trading/DARB/input");
    public static String CSV_DAM_2020_location="\\\\alpotldbs1p/scheduling/sc_otl10/SCHEDULING/Trading/DARB/DAM2022.csv";
    public static String CSV_CAP_2020_location="\\\\alpotldbs1p/scheduling/sc_otl10/SCHEDULING/Trading/DARB/CAP2020.csv";
    public static String proxy="10.145.203.17";
    public static int port=8080;
    static {

    }

    static String ispisiStranu(String urlS) throws IOException {
        String s="";
        URL url = new URL(urlS);
        Proxy p=new Proxy(Proxy.Type.HTTP,new InetSocketAddress(Utils.proxy, Utils.port));
        //p=Proxy.NO_PROXY;
        HttpsURLConnection con = (HttpsURLConnection)url.openConnection(p);
        InputStream is =con.getInputStream();
        BufferedReader br=new BufferedReader(new InputStreamReader(is));
        String line;
        while((line=br.readLine())!=null){
            s=s+line+"\n";
        }
        return s;
    }

    static List<String> excelToList(File inputFile, String s){
        String spr=" && ";
        List<String> list=new ArrayList<>();

        try (FileInputStream fis=new FileInputStream(inputFile)){
            Workbook wBook;
            if(inputFile.toString().endsWith(".xls")) wBook=new HSSFWorkbook(fis);
            else wBook = new XSSFWorkbook(fis);
            try {
                wBook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            } catch (Exception e) {
                System.out.println(e.getMessage()+", proceeding without evaluation - file: "+inputFile);;
            }
            Sheet sheet = wBook.getSheet(s);
            if (sheet==null) return null;
            else{
                Row row;
                Cell cell;
                StringBuilder red=new StringBuilder("");
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext()) {
                    row = rowIterator.next();
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        cell = cellIterator.next();
                        CellType ctp=cell.getCellType();
                        switch (ctp) {
                            case BOOLEAN:
                                red.append(cell.getBooleanCellValue() + spr);
                                break;
                            case NUMERIC:
                                red.append(cell.getNumericCellValue() + spr);
                                break;
                            case STRING:
                                red.append(cell.getStringCellValue().replace(",",".") + spr);
                                break;
                            case FORMULA:
                                try{
                                    red.append(cell.getNumericCellValue() + spr);
                                }catch(Exception e){
                                    try{
                                        red.append(cell.getStringCellValue() + spr);
                                    }catch(IllegalStateException ee){
                                        red.append("XXX"+spr);
                                        Utils.log(e);
                                    }
                                }
                                break;
                            case BLANK:
                                red.append("" + spr);
                                break;
                            default:
                                red.append(cell + spr);
                        } // end switch
                    } // end inner
                    list.add(red.toString());
                    red= new StringBuilder("");
                } // end outer

            }
        }
        catch(IOException e){
            Utils.log(e);
        }
        //Utils.p(list);
        return list;
    } // End excelToList

    static List<String> excelToList(File inputFile, int s){
        String spr=" && ";
        List<String> list=new ArrayList<>();

        try (FileInputStream fis=new FileInputStream(inputFile)){
            Workbook wBook;
            if(inputFile.toString().endsWith(".xls")) wBook=new HSSFWorkbook(fis);
            else wBook = new XSSFWorkbook(fis);
            //wBook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            try {
                wBook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            } catch (Exception e) {
                System.out.println(e.getMessage()+", proceeding without evaluation - file: "+inputFile);;
            }
            Sheet sheet = wBook.getSheetAt(s);

            //Sheet sheet=wBook.getSheetAt();
            if (sheet==null) return null;
            else{
                Row row;
                Cell cell;
                StringBuilder red=new StringBuilder("");
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext()) {
                    row = rowIterator.next();
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        cell = cellIterator.next();
                        CellType ctp=cell.getCellType();
                        switch (ctp) {
                            case BOOLEAN:
                                red.append(cell.getBooleanCellValue() + spr);
                                break;
                            case NUMERIC:
                                red.append(cell.getNumericCellValue() + spr);
                                break;
                            case STRING:
                                red.append(cell.getStringCellValue().replace(",",".") + spr);
                                break;
                            case FORMULA:
                                try{
                                    red.append(cell.getNumericCellValue() + spr);
                                }catch(Exception e){
                                    try{
                                        red.append(cell.getStringCellValue() + spr);
                                    }catch(IllegalStateException ee){
                                        red.append("XXX"+spr);
                                        Utils.log(e);
                                    }
                                }
                                break;
                            case BLANK:
                                red.append("" + spr);
                                break;
                            default:
                                red.append(cell + spr);
                        } // end switch
                    } // end inner
                    list.add(red.toString());
                    red= new StringBuilder("");
                } // end outer

            }
        }
        catch(IOException e){
            Utils.log(e);
        }
        //Utils.p(list);
        return list;
    } // End excelToList

    static void log(Exception e){
        e.printStackTrace();
    }

    static void p(Object o){
        System.out.println(o);
    }

    static int cmpr(Path p1, Path p2){
        try{
            return Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
        }catch (IOException e){
            e.printStackTrace();
        }
        return 0;
    }


} // End Utils
