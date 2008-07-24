/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.4 $
 * @since $Date: 2008/07/16 15:11:09 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.parseur;

public class ReportPointeur
{

    private boolean isfixed;
    private String  filename;
    private int     linenumber;
    private String  fullpath;
    private static int nbrePointeur;
    private final int key;

    public ReportPointeur()
    {
        key = nbrePointeur;
        nbrePointeur++;
    }
    public boolean equals(final ReportPointeur _o)
    {

        boolean etat = false;
        if (fullpath.equals(_o.getFullpath())
                && linenumber == _o.getLinenumber())
        {
            etat = true;
        }
        return etat;
    }

    public String getFilename()
    {
        return filename;
    }
    public String getFullpath()
    {
        return fullpath;
    }

    public int getKey()
    {
        return key;
    }

    public int getLinenumber()
    {
        return linenumber;
    }

    public boolean isIsfixed()
    {
        return isfixed;
    }
    public void setFilename(final String filename)
    {
        this.filename = filename;
    }
    public void setFullpath(final String fullpath)
    {
        this.fullpath = fullpath;
    }
    public void setIsfixed(final boolean isfixed)
    {
        this.isfixed = isfixed;
    }
    public void setLinenumber(final int linenumber)
    {
        this.linenumber = linenumber;
    }

}
