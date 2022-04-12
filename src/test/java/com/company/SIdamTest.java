package com.company;

import java.io.File;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class SIdamTest {

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        System.out.println("Testing in progress...");
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void read() {
        for(String s:Utils.excelToList(new File("Z:\\Trading\\DARB\\henex\\A_6033_20210419_HENEX_01.xlsx"), "Results"))
            System.out.println(s);

    }
}