package me.bartosz1.monitoring;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@AutoConfigureMockMvc
@SpringBootTest
public class MonitoringTests {

    @RegisterExtension
    static GreenMailExtension greenMailExtension;

    static {
        final ServerSetup SERVER_SETUP = ServerSetup.SMTP;
        SERVER_SETUP.setServerStartupTimeout(10000);
        greenMailExtension = new GreenMailExtension(SERVER_SETUP).withConfiguration(GreenMailConfiguration.aConfig().withUser("bmonitord-test", "Test1234")).withPerMethodLifecycle(false);
    }

    @Test
    public void contextLoads() {
    }

}
