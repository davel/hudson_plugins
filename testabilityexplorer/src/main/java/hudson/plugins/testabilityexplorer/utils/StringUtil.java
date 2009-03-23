package hudson.plugins.testabilityexplorer.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Contains some common String utility methods.
 *
 * @author reik.schatz
 */
public class StringUtil {

    private StringUtil() {
    }

    /**
     * Given that the specified String has the format of a Java method signature, this method will
     * strip away all package information.
     *
     * @param signature String
     * @return signature without package information
     */
    public static String stripPackages(String signature) {
        String stripped = signature;
        if (StringUtils.isNotBlank(signature)) {
            String[] tokens = StringUtils.split(stripped, ", ()");
            for (String token : tokens) {
                if (token.length() > 0 && token.contains(".")) {
                    String tokenWithoutPackage = StringUtils.substringAfterLast(token, ".");
                    stripped = StringUtils.replace(stripped, token, tokenWithoutPackage);
                }
            }

        }
        return stripped;
    }

    /**
     * Given a string that is a Fully Qualified classname signature, this method will simply return
     * the package name.
     *
     * @param signature String
     * @return package name
     */
    public static String getPackage(String signature) {
        String packageName = signature;
        if (StringUtils.isNotBlank(packageName)) {
            packageName = packageName.trim();
            int lastDot = packageName.lastIndexOf('.');
            if (lastDot > 0) {
                packageName = packageName.substring(0, lastDot);
            }
        }
        return packageName;
    }
}
