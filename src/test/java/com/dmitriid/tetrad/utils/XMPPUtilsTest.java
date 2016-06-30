package com.dmitriid.tetrad.utils;

import org.jivesoftware.smackx.XHTMLText;
import org.junit.Test;

import static org.junit.Assert.*;

public class XMPPUtilsTest {
  @Test
  public void toXMPPXHTML() throws Exception {
    XHTMLText xhtml = new XHTMLText(null, null);

    String textWithNL = "a\nb\nc";

    XMPPUtils.toXMPPXHTML(textWithNL, xhtml);

    assertEquals("<body>a<br/>b<br/>c</body>", xhtml.toString());
  }
}
