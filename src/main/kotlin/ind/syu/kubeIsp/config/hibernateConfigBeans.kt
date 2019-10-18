package ind.syu.kubeIsp.config

import ind.syu.kubeIsp.repository.ColumnConfRepository
import ind.syu.kubeIsp.repository.DictionaryRepository
import ind.syu.kubeIsp.repository.MonyToMonyRepository
import ind.syu.kubeIsp.repository.TableConfRepository
import ind.syu.kubeIsp.service.AuthorService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.data.domain.AuditorAware
import java.util.*
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.util.StringUtils
import javax.persistence.EntityManager
import javax.sql.DataSource


class AuditorAwareImpl : AuditorAware<String> {

    companion object{
        private val log:Logger= LoggerFactory.getLogger(AuditorAwareImpl::class.java)
    }

    override fun getCurrentAuditor(): Optional<String> {
        var name = AuthorService.sessionNameThread.get()
        log.info("current session name is {}",name)
        if(StringUtils.isEmpty(name)){
            return Optional.empty()
        }
        return Optional.of(name)
    }

}

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
class PersistenceConfig {

    @Bean
    fun auditorProvider(): AuditorAware<String> {
        return AuditorAwareImpl()
    }
}

