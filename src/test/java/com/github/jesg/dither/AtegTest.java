package com.github.jesg.dither;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class AtegTest {

    @Test
    public void canRunAteg() {
        List<Object[]> result = Dither.ateg(
                new Object[][] {
                    new Object[] { 1, 2 },
                    new Object[] { 3, 4 }
                });
        assertTrue(result.size() == 4);
    }

    @Test
    public void canRunAteg2() {
        List<Object[]> result = Dither.ateg(3,
                new Object[][] {
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2 },
                    new Object[] { 1, 2, 3 },
                    new Object[] { 1, 2, 3 },
                    new Object[] { 1, 2, 3, 4 },
                    new Object[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
                    new Object[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }
                });
        assertTrue(result.size() > 100);
    }
}
