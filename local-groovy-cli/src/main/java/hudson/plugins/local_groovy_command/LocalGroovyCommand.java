package hudson.plugins.local_groovy_command;

import hudson.Extension;
import hudson.cli.CLICommand;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import org.codehaus.groovy.tools.shell.Groovysh;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class LocalGroovyCommand extends CLICommand {
    public String getShortDescription() {
        return "Just as a demonstration, run Groovysh now locally on the CLI JVM, not on the server";
    }

    public int main(final List<String> args, InputStream stdin, PrintStream stdout, PrintStream stderr) {
        try {
            Channel ch = Channel.current();
            ch.preloadJar(getClass().getClassLoader(),Groovysh.class, Class.forName("org.apache.xerces.jaxp.DocumentBuilderImpl"));
            return ch.call(new GroovyRunner(args.toArray(new String[args.size()])));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    protected int run() {
        throw new UnsupportedOperationException();
    }

    private static class GroovyRunner implements Callable<Integer,IOException> {
        private final String[] args;

        public GroovyRunner(String[] args) {
            this.args = args;
        }

        public Integer call() throws IOException {
            Groovysh shell = new Groovysh();
            return shell.run(args);
        }
    }
}

