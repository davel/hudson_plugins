package org.jggug.hudson.plugins.gcrawler.util;

import static org.apache.commons.beanutils.PropertyUtils.getProperty;
import static org.apache.commons.lang.StringUtils.join;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class JobTemplate {

    private static final String TEMPLATE_FILE_PREFIX =
        "/" + JobTemplate.class.getPackage().getName().replaceAll("\\.", "/") + "/%s";

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("#\\{(.*?)\\}");

    private String template;

    private Map<String, Pattern> variableMap;

    public JobTemplate(String template) {
        this.template = template;
        variableMap = new HashMap<String, Pattern>();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String name = matcher.group(1);
            if (!variableMap.containsKey(name)) {
                variableMap.put(name, Pattern.compile(String.format("#\\{%s\\}", name)));
            }
        }
    }

    public String generate(Object source) {
        String result = template;
        for (Entry<String, Pattern> entry : variableMap.entrySet()) {
            try {
                Matcher m = entry.getValue().matcher(result);
                while (m.find()) {
                    result = m.replaceAll(toString(getProperty(source, entry.getKey())));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private String toString(Object o) {
        String result = null;
        if (o == null) {
            result = "";
        }
        else if (o instanceof String) {
            result = (String) o;
        }
        else if (o instanceof Collection) {
            result = join((Collection) o, ", ");
        }
        else {
            result = o.toString();
        }
        return result;
    }

    public static JobTemplate createTemplate(String fileName) {
        String path = String.format(TEMPLATE_FILE_PREFIX, fileName);
        InputStream in = JobTemplate.class.getResourceAsStream(path);
        if (in == null) {
            throw new RuntimeException(String.format("Template file (%s) is not found.", path));
        }
        try {
            return new JobTemplate(IOUtils.toString(in));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
