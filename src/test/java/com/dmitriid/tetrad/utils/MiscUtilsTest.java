package com.dmitriid.tetrad.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MiscUtilsTest {
    @Test
    public void intToRGB(){
        assertEquals("B7E112", MiscUtils.intToRGB("tetrad".hashCode()));
        assertEquals("7B0735", MiscUtils.intToRGB("tetrad@c.j.r".hashCode()));
        assertEquals("D03A92", MiscUtils.intToRGB("fluor".hashCode()));
        assertEquals("4A40B5", MiscUtils.intToRGB("fluor@c.j.r".hashCode()));
        assertEquals("050626", MiscUtils.intToRGB("Dmitrii Dimandt@c.j.r".hashCode()));
    }
}
