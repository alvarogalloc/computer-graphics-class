package edu.up.cg.integrations.exiftool;

import edu.up.cg.integrations.common.CommandResult;
import edu.up.cg.integrations.common.CommandRunner;
import edu.up.cg.integrations.metadata.GeoPoint;
import edu.up.cg.integrations.metadata.MediaMetadata;
import edu.up.cg.integrations.metadata.MediaType;
import edu.up.cg.integrations.metadata.MetadataService;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class ExifToolCliService implements MetadataService {
    private static final DateTimeFormatter EXIF_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    private final CommandRunner commandRunner;

    public ExifToolCliService() {
        this(new CommandRunner());
    }

    public ExifToolCliService(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    @Override
    public MediaMetadata readMetadata(Path mediaPath) {
        List<String> command = List.of(
            "exiftool",
            "-s3",
            "-n",
            "-DateTimeOriginal",
            "-CreateDate",
            "-ModifyDate",
            "-GPSLatitude",
            "-GPSLongitude",
            "-MIMEType",
            mediaPath.toAbsolutePath().toString()
        );

        CommandResult result = commandRunner.run(command);
        if (!result.isSuccess()) {
            throw new IllegalStateException("Unable to read metadata: " + result.getOutput());
        }

        String[] lines = result.getOutput().split("\\R", -1);
        Optional<LocalDateTime> capturedAt = firstDate(lines, 0, 1, 2);
        Optional<GeoPoint> location = parseLocation(lines, 3, 4);
        String mime = readLine(lines, 5);

        return new MediaMetadata(mediaPath, MediaType.fromMimeType(mime), capturedAt, location);
    }

    private Optional<LocalDateTime> firstDate(String[] lines, int... indexes) {
        for (int index : indexes) {
            String value = readLine(lines, index);
            if (!value.isBlank()) {
                try {
                    return Optional.of(LocalDateTime.parse(value, EXIF_DATE_FORMAT));
                } catch (DateTimeParseException ignored) {
                    // Move to next candidate date field.
                }
            }
        }
        return Optional.empty();
    }

    private Optional<GeoPoint> parseLocation(String[] lines, int latIndex, int lonIndex) {
        String lat = readLine(lines, latIndex);
        String lon = readLine(lines, lonIndex);

        if (lat.isBlank() || lon.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String readLine(String[] lines, int index) {
        if (index < 0 || index >= lines.length) {
            return "";
        }
        return lines[index].trim();
    }
}
