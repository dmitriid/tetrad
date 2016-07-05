/*******************************************************************************
 * Copyright (c) 2016 Dmitrii "Mamut" Dimandt <dmitrii@dmitriid.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/

package com.dmitriid.tetrad;

import com.dmitriid.tetrad.interfaces.IManagedService;
import com.dmitriid.tetrad.services.ServiceConfiguration;
import com.dmitriid.tetrad.utils.CommandLine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Tetrad {

    private final IManagedService service;
    private final ServiceConfiguration configuration;

    private Tetrad(IManagedService service, ServiceConfiguration configuration) {
        this.service = service;
        this.configuration = configuration;
    }

    public static void main(String[] args) {

        Logger logger = LoggerFactory.getLogger(Tetrad.class.getCanonicalName());
        logger.info("Startup with args " + Arrays.toString(args));

        CommandLine line = new CommandLine(args);
        String configFile = line.optionValue("config");

        ServiceConfiguration configuration = new ServiceConfiguration(configFile);
        String handler = configuration.getConfiguration().at("/handler").asText();

        IManagedService service = TetradObjectFactory.getService(handler);

        new Tetrad(service, configuration).run();
    }

    private void run() {
        Logger logger = LoggerFactory.getLogger(service.getClass().getCanonicalName());
        logger.debug("Tetrad::run");

        try {
            service.init(configuration);
            service.start();
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                service.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
