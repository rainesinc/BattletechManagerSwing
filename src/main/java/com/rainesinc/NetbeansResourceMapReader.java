package com.rainesinc;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class NetbeansResourceMapReader {

    private Properties properties;
    public NetbeansResourceMapReader(String resourceName) throws IOException {
        properties = new Properties();
        properties.load(NetbeansResourceMapReader.class.getClassLoader()
                .getResourceAsStream(resourceName));
    }
    public String getString(String key) throws IOException {
        return this.properties.getProperty(key);
    }

    public int getInteger(String key) throws IOException {
        String stringValue = this.getString(key);
        return Integer.parseInt(stringValue);
    }

    public Icon getIcon(String key) throws IOException {
        String stringValue = this.getString(key);
        return new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource(stringValue)));
    }

    public Font getFont(String key) throws IOException {
        String stringValue = this.getString(key);
        return new Font("Verdana", Font.BOLD, 12);
    }
}
