package org.jggug.hudson.plugins.gcrawler.util;

import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.jggug.hudson.plugins.gcrawler.GBuildWrapper;

public class HudsonPluginUtils {

    private static final Field F_BUILD_WRAPPERS;

    static {
        try {
            F_BUILD_WRAPPERS = Project.class.getDeclaredField("buildWrappers");
            F_BUILD_WRAPPERS.setAccessible(true);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Publisher createEmotionalHudsonPublisher() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return (Publisher) Class.forName("hudson.plugins.emotional_hudson.EmotionalHudsonPublisher").newInstance();
    }

    @SuppressWarnings("unchecked")
    public static Publisher createTwitterPublisher() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Class type = Class.forName("hudson.plugins.twitter.TwitterPublisher");
        Constructor c = type.getConstructor(String.class, String.class, Boolean.class, Boolean.class);
        return (Publisher) c.newInstance(null, null, false, true);
    }

    public static boolean isActive(String shortName) {
        return Hudson.getInstance().getPlugin(shortName) != null;
    }

    @SuppressWarnings("unchecked")
    public static void addGBuildWrapper(FreeStyleProject job) {
        try {
            ((DescribableList) F_BUILD_WRAPPERS.get(job)).add(new GBuildWrapper());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
