package hudson.plugins.pxe.ubuntu;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Constant that represents ubuntu releases.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Release implements Comparable<Release> {
    public static final List<Release> RELEASES = init();

    public final String nickName;
    public final String number;

    private Release(String nickName, String number) {
        this.nickName = nickName;
        this.number = number;
    }

    public int compareTo(Release that) {
        return this.number.compareTo(that.number);
    }

    @Override
    public String toString() {
        return number;
    }

    public String getDisplayName() {
        return String.format("%s (%s)",number,nickName);
    }

    /**
     * Finds the {@link Release} via a nick name or a number.
     */
    public static Release get(String nickNameOrNumber) {
        for (Release r : RELEASES)
            if(r.nickName.equals(nickNameOrNumber) || r.number.equals(nickNameOrNumber))
                return r;
        return null;
    }

    private static List<Release> init() {
        try {
            Properties props = new Properties();
            props.load(Release.class.getClassLoader().getResourceAsStream("ubuntu-releases.properties"));

            List<Release> r = new ArrayList<Release>();
            for (Entry<Object, Object> e : props.entrySet()) {
                r.add(new Release(e.getKey().toString(),e.getValue().toString()));
            }
            Collections.sort(r);

            return r;
        } catch (IOException e) {
            throw new Error("Failed to load ubuntu releases",e);
        }
    }

    /**
     * On disk, just use the release number.
     */
    public static final class ConverterImpl implements Converter {
        public ConverterImpl() {
        }

        public boolean canConvert(Class type) {
            return type==Release.class;
        }

        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            Release src = (Release) source;
            writer.setValue(src.number);
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            return Release.get(reader.getValue());
        }
    }
}
