package delta.codecharacter.server.auth.jwt

import delta.codecharacter.server.user.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtRequestFilter : OncePerRequestFilter() {
    @Autowired private lateinit var userService: UserService
    @Autowired private lateinit var authUtil: JwtService

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authorizationHeader = request.getHeader("Authorization")
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }
        val jwt = authorizationHeader.substring(7)

        if (SecurityContextHolder.getContext().authentication == null) {
            try {
                val email = authUtil.getEmailFromToken(jwt)
                val userDetails = userService.loadUserByUsername(email)
                authUtil.validateToken(jwt, userDetails)
                val usernamePasswordAuthenticationToken =
                    UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                usernamePasswordAuthenticationToken.details =
                    WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
            } catch (e: Exception) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                return
            }
        }
        filterChain.doFilter(request, response)
    }
}
