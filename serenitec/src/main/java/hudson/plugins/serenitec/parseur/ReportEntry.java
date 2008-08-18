/**
 * Hudson Serenitec plugin
 *
 * @author Georges Bossert <gbossert@gmail.com>
 * @version $Revision: 1.6 $
 * @since $Date: 2008/07/24 09:44:14 ${date}
 * @copyright Université de Rennes 1
 */
package hudson.plugins.serenitec.parseur;


import java.util.ArrayList;

public class ReportEntry implements Comparable<ReportEntry>
{

    String                       name;
    int                          severity;
    ArrayList<ReportPointeur>    pointeurs;
    ArrayList<ReportDescription> descriptions;

    public ReportEntry() {

        pointeurs = new ArrayList<ReportPointeur>();
        descriptions = new ArrayList<ReportDescription>();
    }

    public int compareTo(final ReportEntry _o) {

        int resultat = 0;
        if (getNumberOfPointeurs() < _o.getNumberOfPointeurs()) {
            resultat = -1;
        } else if (getNumberOfPointeurs() > _o.getNumberOfPointeurs()) {
            resultat = 1;
        }
        return resultat;
    }
    public boolean equals(final ReportEntry _o) {

        boolean etat = false;
        if (name.equals(_o.getName())) {
            etat = true;
            for (final ReportPointeur pointeur : pointeurs) {
                if (!_o.getPointeurs().contains(pointeur)) {
                    etat = false;
                }
            }
        }
        return etat;
    }

    public ArrayList<ReportDescription> getDescriptions() {

        return descriptions;
    }

    public String getName() {

        return name;
    }
    public ArrayList<ReportPointeur> getPointeurs() {

        return pointeurs;
    }
    public int getNumberOfPointeurs() {

        return pointeurs.size();
    }
    public int getSeverity() {

        return severity;
    }

    public void setDescriptions(final ArrayList<ReportDescription> descriptions) {

        this.descriptions = descriptions;
    }
    public void setName(final String name) {

        this.name = name;
    }

    public void setPointeurs(final ArrayList<ReportPointeur> pointeurs) {

        this.pointeurs = pointeurs;
    }

    public void setSeverity(final int severity) {

        this.severity = severity;
    }
    public boolean isActive() {

        return this.pointeurs.size() > 0;
    }
    /**
     * Return the patern having this ID key
     * 
     * @param key
     * @return ReportPointeur
     */
    public ReportPointeur getPattern(final int key) {

        ReportPointeur resultat = null;
        for (ReportPointeur pattern : getPointeurs()) {
            if (pattern.getKey() == key) {
                resultat = pattern;
            }
        }
        return resultat;
    }
    /**
     * Return if all the patterns are fixed
     */
    public boolean isFixed() {

        boolean resultat = true;
        for (ReportPointeur pattern : getPointeurs()) {
            if (!pattern.isIsfixed()) {
                resultat = false;
            }
        }
        return resultat;
    }

}
