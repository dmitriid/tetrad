package com.dmitriid.tetrad.services.utils;

import com.dmitriid.tetrad.utils.JIDUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class JIDUtilsTest {
    @Test
    public void jid_to_slack_username() throws Exception {
        assertEquals("JID is shortened",
                     "dmitriid@c.j.r",
                     JIDUtils.jid_to_slack_username("dmitriid@conference.jabber.ru")
                    );
    }

}
