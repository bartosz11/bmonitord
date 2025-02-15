package one.bartosz.bmonitord.common.converters;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.Duration;

@WritingConverter
public class DurationToIntervalConverter implements Converter<Duration, String> {
    @Override
    public String convert(Duration source) {
        return source.toString().replace("PT", "").toLowerCase(); // Converts to "1h30m"
    }
}
