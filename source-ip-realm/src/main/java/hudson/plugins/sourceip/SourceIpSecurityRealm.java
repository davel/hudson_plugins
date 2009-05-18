package hudson.plugins.sourceip;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.SecurityRealm;
import hudson.security.GroupDetails;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationCredentialsNotFoundException;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.kohsuke.stapler.DataBoundConstructor;
import org.springframework.dao.DataAccessException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@link SecurityRealm} that
 *
 * @author Kohsuke Kawaguchi
 */
public class SourceIpSecurityRealm extends SecurityRealm {
    @DataBoundConstructor
    public SourceIpSecurityRealm() {
    }


    /**
     * The incoming request is "authenticated" automatically as an user that represents the remote IP address.
     * There's no real authentication involved here (in the sense that no one sends a password), so we
     * create {@link Authentication#isAuthenticated() an pre-authenticated Authentication object} right
     * from the get-go.
     */
    @Override
    public Filter createFilter(FilterConfig filterConfig) {
        return new Filter() {
            public void init(FilterConfig filterConfig) throws ServletException {
            }

            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                String addr = request.getRemoteAddr();

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(addr,addr, buildGroupList(addr)));
                chain.doFilter(request,response);
            }

            public void destroy() {
            }
        };
    }

    /**
     * Approximation of the IP address.
     */
    private static final Pattern IP = Pattern.compile("[0-9]+(\\.[0-9]+)*");

    /**
     * Accepts a dot-separated numbers and count the number of tokens.
     */
    private int decomposeIP(String s) {
        try {
            // quickly check if it looks roughly right.
            // this simplifies the rest of the check
            if(!IP.matcher(s).matches())    return 0;

            String[] tokens = s.split("\\.");
            for (String t : tokens) {
                int i = Integer.parseInt(t);
                if(i<0 || i>255)
                    return 0;
            }
            return tokens.length;
        } catch (NumberFormatException e) {
            // happens if the number is too big
            return 0;
        }
    }

    /**
     * Builds up the group list from the IP address.
     */
    private GrantedAuthority[] buildGroupList(String addr) {
        // add subnet as groups
        List<GrantedAuthority> groups = new ArrayList<GrantedAuthority>();
        for(String a=addr;;) {
            int idx=a.lastIndexOf('.');
            if(idx<0)   break;
            a=a.substring(0,idx);
            groups.add(new GrantedAuthorityImpl(a));
        }
        return groups.toArray(new GrantedAuthority[groups.size()]);
    }

    @Override
    public GroupDetails loadGroupByGroupname(final String groupname) throws UsernameNotFoundException, DataAccessException {
        int cnt = decomposeIP(groupname);
        if(0<cnt && cnt<4)
            return new GroupDetails() {
                public String getName() {
                    return groupname;
                }
            };
        throw new UsernameNotFoundException(groupname+" is not a valid partial IP address");
    }

    public SecurityComponents createSecurityComponents() {
        return new SecurityComponents(
            new AuthenticationManager() {
                public Authentication authenticate(Authentication a) throws AuthenticationException {
                    // the filter above creates a fully authenticated Authentication object,
                    // so no need to do anything further
                    if(!a.isAuthenticated())
                        throw new AuthenticationCredentialsNotFoundException("Not authenticated");
                    return a;
                }
            },
            new UserDetailsService() {
                public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
                    if(decomposeIP(username)!=4)
                        throw new UsernameNotFoundException(username+" is not a valid IP address");
                    return new User(username,username,true,true,true,true,buildGroupList(username));
                }
            }
        );
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SecurityRealm> {
        public String getDisplayName() {
            return "By the IP address of the user";
        }
    }
}
