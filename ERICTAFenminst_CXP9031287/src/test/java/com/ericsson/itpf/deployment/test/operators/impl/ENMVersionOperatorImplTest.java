package com.ericsson.itpf.deployment.test.operators.impl;

import com.ericsson.itpf.deployment.test.operators.utils.OutputUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;

import static org.testng.Assert.assertTrue;

public class ENMVersionOperatorImplTest {

    private static final Logger logger = LoggerFactory.getLogger(ENMVersionOperatorImplTest.class);

    @Test
    public void testParseOutput() throws Exception {

        ENMVersionOperatorImpl operator = new ENMVersionOperatorImpl();

        String output =
                "2015-09-02 10:30:24 INFO  display_enm_product_header    : ENM product versions :\n" +
                        "2015-09-02 10:30:25 INFO  display_release_info          : LITP Release info     : LITP 15.12 CSA 113 110 R2AF31\n";

        List<String> lines = OutputUtils.parseOutputToLines(output);

        assertTrue(operator.matchLine(lines, "ENM product versions", null), "Not matched line");
        assertTrue(operator.matchLine(lines, "LITP Release info", ".*LITP.*CSA.*"), "Not matched line");

    }

}