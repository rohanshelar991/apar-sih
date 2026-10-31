package com.infobro.apaar;

import com.infobro.apaar.entity.AuditLog;
import com.infobro.apaar.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"})
@org.springframework.test.context.ActiveProfiles("h2")
class AuditLogRepositoryTest {
    @Autowired AuditLogRepository audits;
    @Test void omittedFiltersReturnNewestFirstAndSupportPagination() {
        AuditLog old = new AuditLog("Alice", "ADMIN", "CREATE", "Student", "1", null, null);
        old.setTimestamp(LocalDateTime.of(2026, 1, 1, 0, 0));
        audits.saveAndFlush(old);
        AuditLog recent = audits.saveAndFlush(new AuditLog("Bob", "ADMIN", "UPDATE", "Student", "1", null, null));
        var page = audits.findWithFilters(null, null, null, null, null, null, PageRequest.of(0, 1));
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(AuditLog::getId).containsExactly(recent.getId());
    }
    @Test void suppliedFiltersIncludeTimeBoundariesAndIgnoreActorCase() {
        LocalDateTime time = LocalDateTime.of(2026, 1, 1, 0, 0);
        AuditLog log = new AuditLog("Alice", "ADMIN", "CREATE", "Student", "1", null, null);
        log.setTimestamp(time); audits.saveAndFlush(log);
        assertThat(audits.findWithFilters("ALI", "ADMIN", "CREATE", "Student", time, time, PageRequest.of(0, 20)).getContent())
            .extracting(AuditLog::getId).containsExactly(log.getId());
        assertThat(audits.findWithFilters(null, null, null, null, time.plusSeconds(1), null, PageRequest.of(0, 20))).isEmpty();
    }
}
