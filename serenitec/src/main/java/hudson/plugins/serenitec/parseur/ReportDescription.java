/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.3 $
 * @since $Date: 2008/07/16 15:11:10 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.parseur;

public class ReportDescription
{
    private String Description;
    private String Language;
    private String Helpreference;
    
    public ReportDescription()
    {
        Description = "";
        Language = "";
    }
    public String getDescription()
    {
        return Description;
    }

    public void setDescription(String description)
    {
        Description = description;
    }

    public String getLanguage()
    {
        return Language;
    }

    public void setLanguage(String language)
    {
        Language = language;
    }

    public String getHelpreference()
    {
        return Helpreference;
    }

    public void setHelpreference(String helpreference)
    {
        Helpreference = helpreference;
    }
}
