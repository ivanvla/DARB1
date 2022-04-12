package com.company;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GRdamNewTest {

    @Test
    void read() {
        new GRdamNew(LocalDate.now().plusDays(1));
    }
}