package com.mtvi.plateng.hudson.svnimport;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import hudson.FilePath;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.mtvi.plateng.hudson.svnimport.SubversionImporter.ImportItem;

public class AppTest {
    @Test
    public void whenExpressionWorks() throws Exception {
        SubversionImporter importer = new SubversionImporter(
                "/trunk/target/foo-(\\d*).(\\d*).(\\d*)-(\\d*).swf", "http://subversion/dest",
                "/$1/$2.$3/$4/foo.swf");
        // List<String> filePaths =
        // Arrays.asList("/trunk/target/foo-1.2.3-4.swf");
        FilePath workspace = new FilePath(new File("src/test/resources/workspace1"));
        List<ImportItem> items = importer.getItemsToImport(workspace);
        assertThat(items.size(), equalTo(1));
        assertThat(items.get(0).svnDestination.toString(),
                equalTo("http://subversion/dest/1/2.3/4/foo.swf"));
    }

    @Test
    public void multipleFiles() throws Exception {
        SubversionImporter importer = new SubversionImporter(
                "/trunk/target/foo-(\\d*).(\\d*).(\\d*)-(\\d*).swf", "http://subversion/dest",
                "/$1/$2.$3/$4/foo.swf");
        // List<String> filePaths =
        // Arrays.asList("/trunk/target/foo-1.2.3-4.swf",
        // "/trunk/target/foo-1.2.3-5.swf");
        FilePath workspace = new FilePath(new File("src/test/resources/workspace2"));
        List<ImportItem> items = importer.getItemsToImport(workspace);
        assertThat(items.size(), equalTo(2));
        assertThat(items.get(0).svnDestination.toString(),
                equalTo("http://subversion/dest/1/2.3/4/foo.swf"));
        assertThat(items.get(1).svnDestination.toString(),
                equalTo("http://subversion/dest/1/2.3/5/foo.swf"));
    }
}
