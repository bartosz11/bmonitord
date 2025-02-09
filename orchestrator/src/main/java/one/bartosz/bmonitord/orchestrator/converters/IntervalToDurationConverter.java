package one.bartosz.bmonitord.orchestrator.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.Duration;

@ReadingConverter
public class IntervalToDurationConverter implements Converter<String, Duration> {
    @Override
    public Duration convert(String source) {
        return Duration.parse("PT" + source.replace(" ", "").toUpperCase());
    }
}
