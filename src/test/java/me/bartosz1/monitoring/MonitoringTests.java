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
    static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetup.SMTP).withConfiguration(GreenMailConfiguration.aConfig().withUser("bmonitord-test", "Test1234")).withPerMethodLifecycle(false);

    @Test
    public void contextLoads() {
    }

}
