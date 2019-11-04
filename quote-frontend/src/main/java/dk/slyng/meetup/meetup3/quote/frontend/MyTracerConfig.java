package dk.slyng.meetup.meetup3.quote.frontend;

import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.contrib.java.spring.jaeger.starter.TracerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyTracerConfig {

    @Bean
    public TracerBuilderCustomizer ScopeManagerJaegerTracerCustomizer() {
        return new ScopeManagerTracerBuilderCustomizer();
    }

    public class ScopeManagerTracerBuilderCustomizer implements TracerBuilderCustomizer {
        @Override
        public void customize(JaegerTracer.Builder builder) {
            builder.withScopeManager(new MDCScopeManager());
        }
    }

}
