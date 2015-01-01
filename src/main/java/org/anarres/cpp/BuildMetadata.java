package org.anarres.cpp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;

/**
 * Returns information about the build.
 *
 * @author shevek
 */
public class BuildMetadata {

    public static final String RESOURCE = "/META-INF/jcpp.properties";
    private static BuildMetadata INSTANCE;

    /** @throws RuntimeException if the properties file cannot be found on the classpath. */
    @Nonnull
    public static synchronized BuildMetadata getInstance() {
        try {
            if (INSTANCE == null)
                INSTANCE = new BuildMetadata();
            return INSTANCE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Properties properties = new Properties();

    private BuildMetadata() throws IOException {
        URL url = BuildMetadata.class.getResource(RESOURCE);
        InputStream in = url.openStream();
        try {
            properties.load(in);
        } finally {
            in.close();
        }
    }

    @Nonnull
    public Map<? extends String, ? extends String> asMap() {
        Map<String, String> out = new HashMap<String, String>();
        for (Map.Entry<Object, Object> e : properties.entrySet())
            out.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
        return out;
    }

    @Nonnull
    public com.github.zafarkhaja.semver.Version getVersion() {
        return com.github.zafarkhaja.semver.Version.valueOf(properties.getProperty("Implementation-Version"));
    }

    @Nonnull
    public Date getBuildDate() throws ParseException {
        // Build-Date=2015-01-01_10:09:09
        String text = properties.getProperty("Build-Date");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        return format.parse(text);
    }

    public String getChangeId() {
        return properties.getProperty("Change");
    }
}
