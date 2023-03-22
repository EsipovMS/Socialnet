package socialnet.security.jwt;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import socialnet.security.UserService;
import socialnet.security.SocialiteUserDetails;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
@Component
public class JWTRequestFilter extends OncePerRequestFilter {
    private final UserService socialNetUserDetailsService;
    private final JWTUtil jwtUtil;

    public JWTRequestFilter(UserService socialNetUserDetailsService, JWTUtil jwtUtil) {
        this.socialNetUserDetailsService = socialNetUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        String username = null;
        Cookie[] cookies = httpServletRequest.getCookies();
        if(cookies!=null){
            for (Cookie cookie: cookies){
                if(cookie.getName().equals("token")){
                    token=cookie.getValue();
                    username = jwtUtil.extractUsername(token);
                }
                if(username!=null&& SecurityContextHolder.getContext().getAuthentication()==null){
                    SocialiteUserDetails userDetails =(SocialiteUserDetails) socialNetUserDetailsService.loadUserByUsername(username);
                    if(jwtUtil.validateToken(token,userDetails)){
                        UsernamePasswordAuthenticationToken authenticationToken =new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    }
                }
            }
        }
        filterChain.doFilter(httpServletRequest,httpServletResponse);
    }
}
