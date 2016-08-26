package com.dmitriid.tetrad.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MiscUtilsTest {
    @Test
    public void intToRGB(){
        assertEquals("B7E112", MiscUtils.toRGB("tetrad"));
        assertEquals("7B0735", MiscUtils.toRGB("tetrad@c.j.r"));
        assertEquals("D03A92", MiscUtils.toRGB("fluor"));
        assertEquals("4A40B5", MiscUtils.toRGB("fluor@c.j.r"));
        assertEquals("050626", MiscUtils.toRGB("Dmitrii Dimandt@c.j.r"));
    }
}
