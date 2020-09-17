package ua.karazina.donets.utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceUtils {

    public static String readFileFromResourceAsString(String resourceName) {
        InputStream inputStream = ResourceUtils.class.getClassLoader().getResourceAsStream(resourceName);
        try {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error with reading resource file", e);
        }
    }
}
