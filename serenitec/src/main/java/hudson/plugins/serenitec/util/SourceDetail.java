/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.3 $
 * @since $Date: 2008/07/16 16:01:23 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.util;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

/**
 * Renders a source file containing an annotation for the whole file or a
 * specific line number.
 *
 * @author Georges Bossert
 */
public class SourceDetail implements ModelObject
{

    /** Full path to the file before refactoring */
    private final String _fileBefore;
    /** Full path to the file after refactoring */
    private final String _fileAfter;
    /** Asbtract Build */
    private final AbstractBuild<?, ?> _owner;
    /** Source Code */
    private String _sourceCode;

    public SourceDetail(final AbstractBuild<?, ?> owner, String filepath)
    {
        this._fileAfter = filepath;
        this._fileBefore = getBeforeFile(filepath);
        this._owner = owner;
    }

    private String getBeforeFile(String filepath)
    {
        return filepath + ".bis";
    }

    public String getSourceCode()
    {
        execute();
        return _sourceCode;
    }

    /**
     * Execute the diff
     */
    private void execute()
    {
        try
        {
            String[] a = file2string(_fileBefore);
            String[] b = file2string(_fileAfter);
            Diff diff = new Diff(a, b);

            Diff.change next = diff.diff_2(false);
            next = diff.diff_2(false);

            StringBuilder stb = new StringBuilder();
            stb.append("<table class=\"diff\" cellpadding=\"0\" cellspacing=\"0\" >");
            stb.append("<tr>");
            stb.append("<td class=\"ligne_entete\">&nbsp;</td>");
            stb.append("<td class=\"original_entete\">Original File<br /><small>" + _fileBefore + "</small></td>");
            stb.append("<td class=\"ligne_entete\">&nbsp;</td>");
            stb.append("<td class=\"refactored_entete\">Refactored File<br /><small>" + _fileAfter + "</small></td>");

            stb.append("</tr>");


            /**
             * generation html source code for those two files
             */
            int max = a.length;
            if (max < b.length)
            {
                max = b.length;
            }
            String ligneAvant, ligneApres;
            int numeroLigneAvant = 0, numeroLigneApres = 0;
            while (numeroLigneAvant < a.length || numeroLigneApres < b.length)
            {
                stb.append("<tr>");
                if (numeroLigneAvant >= a.length || a[numeroLigneAvant] == null)
                {
                    ligneAvant = "";
                }
                else
                {
                    ligneAvant = a[numeroLigneAvant];
                }
                if (numeroLigneApres >= b.length || b[numeroLigneApres] == null)
                {
                    ligneApres = "";
                }
                else
                {
                    ligneApres = b[numeroLigneApres];
                }

                if (next != null && next.line0 == numeroLigneAvant)
                {
                    if (next.inserted > 0 && next.deleted == 0)
                    {
                        for (int i = 0; i < next.inserted; i++)
                        {

                            stb.append("<tr>");
                            stb.append("<td class=\"ligne_normal\">" + numeroLigneAvant + "</td>");
                            stb.append("<td class=\"original_added\">&nbsp;</td>");
                            stb.append("<td class=\"ligne_added\">" + numeroLigneApres + "</td>");
                            stb.append("<td class=\"refactored_added\">" + ligneApres + "</td>");

                            stb.append("</tr>");

                            numeroLigneApres++;
                            if (numeroLigneApres >= b.length || b[numeroLigneApres] == null)
                            {
                                ligneApres = "";
                            }
                            else
                            {
                                ligneApres = b[numeroLigneApres];
                            }
                        }
                    }
                    if (next.deleted > 0 && next.inserted == 0)
                    {
                        for (int i = 0; i < next.deleted; i++)
                        {

                            stb.append("<tr>");
                            stb.append("<td class=\"ligne_deleted\">" + numeroLigneAvant + "</td>");
                            stb.append("<td class=\"original_deleted\">" + ligneAvant + "</td>");
                            stb.append("<td class=\"ligne_normal\">" + numeroLigneApres + "</td>");
                            stb.append("<td class=\"refactored_deleted\">&nbsp;</td>");

                            stb.append("</tr>");

                            numeroLigneAvant++;
                            if (numeroLigneAvant >= a.length || a[numeroLigneAvant] == null)
                            {
                                ligneAvant = "";
                            }
                            else
                            {
                                ligneAvant = a[numeroLigneAvant];
                            }
                        }
                    }
                    if (next.deleted > 0 && next.inserted > 0)
                    {
                        int max_temp = 0;
                        int min_temp = 0;

                        max_temp = next.deleted;
                        min_temp = next.inserted;

                        if (max_temp < next.inserted)
                        {
                            max_temp = next.inserted;
                            min_temp = next.deleted;
                        }
                        for (int i = 1; i <= max_temp; i++)
                        {
                            if (i > min_temp)
                            {
                                if (next.deleted > next.inserted)
                                {
                                    stb.append("<tr>");
                                    stb.append("<td class=\"ligne_deleted\">" + numeroLigneAvant + "</td>");
                                    stb.append("<td class=\"original_deleted\">" + ligneAvant + "</td>");
                                    stb.append("<td class=\"ligne_normal\">" + numeroLigneApres + "</td>");
                                    stb.append("<td class=\"refactored_deleted\">&nbsp;</td>");
                                    stb.append("</tr>");
                                    numeroLigneAvant++;
                                    if (numeroLigneAvant >= a.length || a[numeroLigneAvant] == null)
                                    {
                                        ligneAvant = "";
                                    }
                                    else
                                    {
                                        ligneAvant = a[numeroLigneAvant];
                                    }
                                }
                                else
                                {
                                    stb.append("<tr>");
                                    stb.append("<td class=\"ligne_normal\">" + numeroLigneAvant + "</td>");
                                    stb.append("<td class=\"original_added\">&nbsp;</td>");
                                    stb.append("<td class=\"ligne_added\">" + numeroLigneApres + "</td>");
                                    stb.append("<td class=\"refactored_added\">" + ligneApres + "</td>");
                                    stb.append("</tr>");
                                    numeroLigneApres++;
                                    if (numeroLigneApres >= b.length || b[numeroLigneApres] == null)
                                    {
                                        ligneApres = "";
                                    }
                                    else
                                    {
                                        ligneApres = b[numeroLigneApres];
                                    }
                                }

                            }
                            else
                            {
                                stb.append("<tr>");
                                stb.append("<td class=\"ligne_modified\">" + numeroLigneAvant + "</td>");
                                stb.append("<td class=\"original_modified\">" + ligneAvant + "</td>");
                                stb.append("<td class=\"ligne_modified\">" + numeroLigneApres + "</td>");
                                stb.append("<td class=\"refactored_modified\">" + ligneApres + "</td>");
                                stb.append("</tr>");
                                numeroLigneAvant++;
                                numeroLigneApres++;
                                if (numeroLigneAvant >= a.length || a[numeroLigneAvant] == null)
                                {
                                    ligneAvant = "";
                                }
                                else
                                {
                                    ligneAvant = a[numeroLigneAvant];
                                }
                                if (numeroLigneApres >= b.length || b[numeroLigneApres] == null)
                                {
                                    ligneApres = "";
                                }
                                else
                                {
                                    ligneApres = b[numeroLigneApres];
                                }
                            }
                        }

                    }
                    next = next.link;
                }
                else
                {
                    stb.append("<tr>");
                    stb.append("<td class=\"ligne_normal\">" + numeroLigneAvant + "</td>");
                    stb.append("<td class=\"original_normal\">" + ligneAvant + "</td>");
                    stb.append("<td class=\"ligne_normal\">" + numeroLigneApres + "</td>");
                    stb.append("<td class=\"refactored_normal\">" + ligneApres + "</td>");
                    stb.append("</tr>");
                    numeroLigneAvant++;
                    numeroLigneApres++;
                }
            }
            stb.append("</table>");
            _sourceCode = stb.toString();
        }
        catch (IOException ex)
        {
            Logger.getLogger(SourceDetail.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static String[] file2string(String file) throws IOException
    {
        BufferedReader rdr = new BufferedReader(new FileReader(file));
        Vector s = new Vector();
        for (;;)
        {
            String line = rdr.readLine();
            if (line == null)
            {
                break;
            }
            s.addElement(line);
        }
        String[] a = new String[ s.size() ];
        s.copyInto(a);
        return a;
    }

    public AbstractBuild<?, ?> getOwner()
    {
        return _owner;
    }

    public String getDisplayName()
    {
        return "getDisplayName : SourceDetail";
    }
}

