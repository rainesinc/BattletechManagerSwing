package com.rainesinc;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class NetbeansResourceMapReader {

    private final Properties properties;
    public NetbeansResourceMapReader(String resourceName) throws IOException {
        properties = new Properties();
        properties.load(NetbeansResourceMapReader.class.getClassLoader()
                .getResourceAsStream(resourceName));
    }
    public String getString(String key) {
        return this.properties.getProperty(key);
    }

    public int getInteger(String key) {
        String stringValue = this.getString(key);
        return Integer.parseInt(stringValue);
    }

    public Icon getIcon(String key)  {
        String stringValue = this.getString(key);
        return new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource(stringValue)));
    }

    public Font getFont(String key) {
        String stringValue = this.getString(key);
        return new Font("Verdana", Font.BOLD, 10);
    }

    public Color getColor(String key) {
        String stringValue = this.getString(key);
        String[] stringArray = stringValue.split(",");
        int r = Integer.parseInt(stringArray[0].trim());
        int g = Integer.parseInt(stringArray[1].trim());
        int b = Integer.parseInt(stringArray[2].trim());
        return new Color(r, g, b);
    }
}
