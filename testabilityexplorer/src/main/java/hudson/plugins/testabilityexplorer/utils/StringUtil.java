package hudson.plugins.testabilityexplorer.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Contains some common String utility methods.
 *
 * @author reik.schatz
 */
public class StringUtil
{
    private StringUtil() { }

    /**
     * Given that the specified String has the format of a Java method signature, this method
     * will strip away all package information.
     *
     * @param signature String
     * @return signature without package information
     */
    public static String stripPackages(String signature)
    {
        String stripped = signature;
        if (!StringUtils.isBlank(signature))
        {
            String[] tokens = StringUtils.split(stripped, ", ()");
            for (String token : tokens)
            {
                if (token.length() > 0 && token.contains("."))
                {
                    String tokenWithoutPackage = StringUtils.substringAfterLast(token, ".");
                    stripped = StringUtils.replace(stripped, token, tokenWithoutPackage);
                }
            }

        }
        return stripped;
    }
}
