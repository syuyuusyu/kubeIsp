package ind.syu.kubeIsp.service

import ind.syu.kubeIsp.config.AuthorFilter
import ind.syu.kubeIsp.entity.User
import ind.syu.kubeIsp.repository.UserRepository
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.crypto.SecretKey

@Service
class AuthorService(
        val userRepository: UserRepository,
        val secretKey: SecretKey
) {
    companion object {
        val sessionIdThread: ThreadLocal<Int> = ThreadLocal()
        val sessionNameThread: ThreadLocal<String> = ThreadLocal()
        val logger = LoggerFactory.getLogger(AuthorService::class.java)
    }


    @Value("\${kube-isp.tokenExpireMin}")
    var tokenExpireMin: Int = 0

    @Transactional
    fun login(id: String, pwd: String): Optional<User> {
        var fields = arrayOf("userName", "phone", "email", "idNumber")
        var user: User? = null
        for (field in fields) {
            var userOption = userRepository.findByField(field, id)
            if (userOption.isPresent) {
                user = userOption.get()
                user?.roles.size
                break
            }
        }
        if (user != null && user.vaildPwd(pwd)) {
            return Optional.of(user)
        }
        return Optional.empty()
    }

    fun createToken(user: User): String {
        val calendar = Calendar.getInstance(Locale.SIMPLIFIED_CHINESE)
        calendar.add(Calendar.MINUTE, tokenExpireMin)
        return Jwts.builder()
                .setAudience(user.userName)
                .setSubject(user.id.toString()).setExpiration(calendar.time).signWith(secretKey).compact()
    }

    fun getUserByToken(token: String): Optional<User> {
        try {
            var id = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body.subject
            return userRepository.findById(id.toInt())
        } catch (e: JwtException) {
            return Optional.empty()
        }
    }

    fun verifyToken(token: String): Boolean {
        try {
            logger.info(token)
            var body = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).body
            var id = body.subject
            var name = body.audience
            sessionIdThread.set(id.toInt())
            sessionNameThread.set(name)
            return true
        } catch (e: JwtException) {
            return false
        }
    }

    fun currentUser(): User {
        var id = sessionIdThread.get()
        return userRepository.findById(id).get()
    }
}