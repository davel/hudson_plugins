/* Copyright (c) 2007, http://www.codeviation.org project 
 * This program is made available under the terms of the MIT License. 
 */

package hudson.plugins.codeviation;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor.FormException;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapper.Environment;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.FormValidation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.codeviation.model.PersistenceManager;
import org.codeviation.model.Repository;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
/**
 *
 * @author pzajac
 */
public class PAntWrapper extends BuildWrapper {
    @Deprecated private transient StaplerRequest req;
    public  String startDate;
    public  String endDate;
    public int daysStep;
    public String antOpts = ""; 
    /** reppsitoryName=CVSROOT
     */ 
    public String repositoryMapping;
    static  SimpleDateFormat f1 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    static  SimpleDateFormat f2 = new SimpleDateFormat("yyyy/MM/dd");
    static  SimpleDateFormat f3 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    static Logger log = Logger.getLogger(PAntWrapper.class.getName());

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl(); 
    
    public PAntWrapper() {
    }
    
  
    String[] getRepMapArray() {
        if (repositoryMapping != null) {
            String strings[] = repositoryMapping.split("=");
            if (strings.length == 2) {
               strings[0] = strings[0].trim();
               strings[1] = strings[1].trim();
               if (strings[0].length() > 0 && strings[1].length() > 0) {
                   return strings;
               }
            }
        }
        return null;
        
    }
    /** @return pant's repository for the project
     */
    public Repository getRepository() {
        System.setProperty(PersistenceManager.PANT_CACHE_FOLDER,PAntWrapper.DESCRIPTOR.getpantCacheFolder().getAbsolutePath());
        PersistenceManager pm = PersistenceManager.getDefault();
        String rn = getRepositoryName();
        if (rn == null || pm == null) {
            return  null;
        }
        return pm.getRepository(rn);
    }
    private String getRepositoryName() {
        String strs[] = getRepMapArray();
        return (strs != null) ?  strs[0] : null;
    }
    private String getRelCvsPath() {
        String strs[] = getRepMapArray();
        return (strs != null) ?  strs[1] : null;
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException {
        if (!checkDate(getStartDate())) {
            throw new IOException("Codeviation: invalid Starting date format - " + getStartDate());
        }
        if (!checkDate(getEndDate())) {
            throw new IOException("Codeviation: invalid Ending date format - " + getEndDate());
        }
        
        File pantCache = DESCRIPTOR.getpantCacheFolder();
        if (pantCache == null) {
            throw new IOException("Codeviation: Pant cache folder doesn't exists. Set up it hudson global settings.");
        }
        String pantLibs = DESCRIPTOR.getPantLibs();
        if (pantLibs == null || pantLibs.length() == 0) {
            throw new IOException("Codeviation: pant libs folder is empty or is invalid. Set up it hudson global settings.");
        }
        if (getRepMapArray() == null) {
            throw new IOException("Codeviation: Repository mapping is not configured");
        }
        initRepository(build);
        return new EnvironmentImpl(build);
    }
       private void initRepository(AbstractBuild<?,?> build) throws IOException {
            FilePath path = build.getWorkspace();
            final String relCvs = getRelCvsPath();
            String absPath = null;
            try {
                 absPath = path.act(new FileCallable<String>(){ 
                    public String  invoke(File f, VirtualChannel channel) throws IOException {
                        
                      File newFile = (".".equals(relCvs) ? f :new File(f,relCvs));
                      newFile.mkdirs();
                      if (!newFile.isDirectory()) {
                        throw new IOException ("CVS checkout path doesn't exist:" + newFile);
                      }
                      return newFile.getAbsolutePath();
                    }
                }); 
            } catch(InterruptedException ie) {
                throw new IOException(ie.getMessage());
            }
            String repName = getRepositoryName();
            // repository definition in repositories.lst
            // XXX two lines should be rewritten in patched ant with one line name = folder
            String twoLines = repName + "\n" + absPath + "\n";
            File f = DESCRIPTOR.getpantCacheFolder();
            File repositories = new File (f,"repositories.lst");
            if (repositories.exists()) {
                byte buff[] = new byte[(int)repositories.length()];
                FileInputStream fis = new FileInputStream(repositories);
                try {
                    fis.read(buff);
                } finally {
                    fis.close();
                }
                String strBuff = new String(buff);
                // XXX Windows separator :(
                if (strBuff.indexOf(twoLines) != -1) {
                    return;
                }
            }
            Writer fos = new FileWriter(repositories,true);
            try {
                fos.write(twoLines );
            } finally {
                fos.close();
            }
            
        }
 
    public String getStartDate() {
        return startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    
    public void setStartDate(String date) {
        this.startDate = date;
    }
    public void setEndDate(String date) {
        this.endDate = date;
    }
    
    public void setDaysStep(String steps) {
        if (steps == null) {
            daysStep = 0;
        } else {
            try {
                daysStep = Integer.parseInt(steps);
            } catch(NumberFormatException pe) {}
        }
    }
    public static Date parseDate(String str) {
         try {
            return f1.parse(str);
         } catch(ParseException pe) {
             try {
                return f2.parse(str);
             } catch(ParseException pe2) {
                try {
                    return f3.parse(str);
                 } catch(ParseException pe3) {}
             }
        }
        return null;
    }
    private static boolean checkDate(String str) {
        if (str != null && str.trim().length() > 0) {
            return parseDate(str) != null; 
        } 
        return false;
    }
    
    public static class DescriptorImpl extends BuildWrapperDescriptor {
    
       public File pantLibsFolder;
       public  File pantCacheFolder;
        DescriptorImpl() {
            super(PAntWrapper.class);
            load();
        }

        public String getDisplayName() {
            return "Codeviation metrics";
        }

        public File getpantCacheFolder() {
            File ret = null;
            if (pantLibsFolder != null && pantCacheFolder.isDirectory()) {
                ret = pantCacheFolder;
            }
            return ret;
        }
        // @return classpath 
        public String getPantLibs() {
            StringBuilder builder = new StringBuilder();
            if (pantLibsFolder != null && pantLibsFolder.isDirectory()) {
                for (File f :pantLibsFolder.listFiles()) {
                    String path = f.getAbsolutePath();
                    if (path.endsWith(".jar")) {
                        if (builder.length() > 0) {
                            builder.append(":");
                        }
                        builder.append(path);
                    }
                }
            }
            return builder.toString();
        }

        @Override
        public boolean isApplicable(AbstractProject<?,?> item) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindParameters(this,"codeviation.");
            save();
            return true;
        }

        @Override
        public String getHelpFile() {
            return "/plugin/codeviation/help-projectConfig.html";
        }
        
        // XXX this validator didn't work, maybe problem with Wrappers in hudson
        // In wrapper class is warning don't use this experimental class :(
        public FormValidation doCheckA(@QueryParameter String value, @QueryParameter String name) {
            if ("startDate".equals(name) || "endDate".equals(name)) {
                if (checkDate(value)) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error("Invalid date format (yyy/mm/dd hh:MM)");
                }
            } else {
                if (value != null) {
                    try {
                        Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        return FormValidation.error("Not number");
                    }
                }
                return FormValidation.ok();
            }
        }

        @Override
        public PAntWrapper newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            PAntWrapper wrapper = new PAntWrapper();
            req.bindParameters(wrapper,"codeviation.");
  //          System.out.println("startDate: " + req.getParameter("codeviation.startDate"));
//            wrapper.setStartDate(req.getParameter("codeviation.startDate"));
//            wrapper.setEndDate(req.getParameter("codeviation.endDate"));
//            wrapper.setDaysStep(req.getParameter("codeviation.daysStep"));
//            save();
            return wrapper; 
        }
        
    }
    class EnvironmentImpl extends Environment {
        AbstractBuild<?,?> build;
        EnvironmentImpl(AbstractBuild<?,?> build) {
            this.build = build;
        }
        @Override
        public void buildEnvVars(Map<String, String> env) {
            StringBuilder allAntOpts = new StringBuilder();
            allAntOpts.append("-Dpant.cache.folder=\"" + DESCRIPTOR.getpantCacheFolder().getAbsolutePath() + "\" ");
            allAntOpts.append("-Dbuild.compiler=org.codeviation.javac.MeasuringJavac ");
            String buildDate = getBuildDate();
            log.info("build.Date = " + buildDate);
            if (buildDate != null) {
                allAntOpts.append("-Dpant.cvs.tag=\"" + buildDate +"\" ");
            }
            if (antOpts != null) {
               
                String value = antOpts.replace('\n', ' ');
                value = value.replace((char)0x0d,' ');
                value = value.replaceAll("\\s"," ");
                allAntOpts.append(value );
                  //ANT_OPTS="-Xmx512m -Dbuild.compiler=org.codeviation.javac.MeasuringJavac\
                  //-Dpant.cache.folder=/space/pant/pantcache \
                  //-Dpant.log.file=$WORKSPACE/pant.log\
                  //-Dscrambler2=I-implicitly-accept-any-and-all-license-terms-required-by-any-files-scrambled-using-org.netbeans.nbbuild.Scramble-by-using-this-option-and-will-not-release-this-key-to-anyone-except-Sun-employees-and-other-explicitly-authorized-parties\
                  //-Dnetbeans.no.pre.unscramble=true "
            }
           env.put("ANT_OPTS",allAntOpts.toString());
           // classpath

           env.put("CLASSPATH",DESCRIPTOR.getPantLibs());
            //PANT_LIB=$HOME/pant
                //export CLASSPATH=$CLASSPATH:$PANT_LIB/issuezillaquery.jar:\
                //$PANT_LIB/org-netbeans-modules-java-source.jar:\
                //$PANT_LIB/pant.jar:\
                //$PANT_LIB/javac-api.jar:\
                //$PANT_LIB/javac-impl.jar:\
                //$PANT_LIB/mtj.jar:\
                //$PANT_LIB/org-openide-util.jar
                //./pantloop.pl
     
            env.put("PANT_CVS_DATE",buildDate );
            env.put("PANT_CVS_TAG", buildDate );
          
        }
        public String getBuildDate() {
            Date buildDate = new Date();
            Date startDate = parseDate(getStartDate());
            Date endDate = parseDate(getEndDate());
            if (startDate != null && endDate!= null && startDate.compareTo(endDate) < 0) {
                buildDate = startDate;
            }
            return f1.format(buildDate);
        }
        @Override
        public boolean tearDown(AbstractBuild build, BuildListener arg1) throws IOException {
            Date date = getNextBuildDate();    
            if (date != null) {
                setStartDate(f1.format(date));
                build.getProject().save();
                build.getProject().scheduleBuild(new CodeviationCause());
            }
            return true;
        }        
    }
    private Date getNextBuildDate() {
        Date startDate = parseDate(getStartDate());
        Date endDate = parseDate(getEndDate());
        Date retDate = null;
        if (startDate != null && endDate != null && daysStep > 0) {
            // increase date + daysStep days
            startDate.setTime(startDate.getTime() + daysStep*3600L*1000*24);
            if (startDate.compareTo(endDate) < 0) {
                retDate = startDate;
            }
        }
        return retDate;
    }
}
