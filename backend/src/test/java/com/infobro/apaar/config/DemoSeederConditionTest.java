package com.infobro.apaar.config;

import com.infobro.apaar.service.DataSeederService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

class DemoSeederConditionTest {
    @Test void productionAndDisabledDemoNeverConstructSeeder() {
        for (String[] profiles : new String[][]{{"postgres"}, {"postgres", "h2"}, {"h2"}, {"dev"}}) {
            try (var context = new AnnotationConfigApplicationContext()) {
                context.getEnvironment().setActiveProfiles(profiles);
                context.getEnvironment().getSystemProperties().put("apaar.seed-demo-data",
                    profiles[0].equals("postgres") ? "true" : "false");
                try {
                    context.register(DataSeederService.class);
                    context.refresh();
                    assertThat(context.getBeansOfType(DataSeederService.class)).isEmpty();
                } finally {
                    context.getEnvironment().getSystemProperties().remove("apaar.seed-demo-data");
                }
            }
        }
    }
}
