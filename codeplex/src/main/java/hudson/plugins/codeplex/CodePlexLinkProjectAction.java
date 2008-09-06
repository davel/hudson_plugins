package hudson.plugins.codeplex;

import hudson.model.Action;
import hudson.model.ProminentProjectAction;

/***
 * Project action that adds a link on the Hudson project page to the project
 * on the codeplex.com website.
 * 
 * @author Erik Ramfelt
 */
public class CodePlexLinkProjectAction implements ProminentProjectAction {

    private static final long serialVersionUID = 1L;

    private final CodePlexProjectProperty property;

    public CodePlexLinkProjectAction(CodePlexProjectProperty codePlexProjectProperty) {
        this.property = codePlexProjectProperty;
    }

    public String getDisplayName() {
        return "CodePlex Home";
    }

    public String getIconFileName() {
        return "/plugin/codeplex/icons/codeplex-110x110.png";
    }

    public String getUrlName() {
        return property.getProjectUrlString();
    }
}
