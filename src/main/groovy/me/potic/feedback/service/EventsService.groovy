package me.potic.feedback.service

import groovy.util.logging.Slf4j
import me.potic.feedback.domain.ArticleEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.LocalDateTime

@Service
@Slf4j
class EventsService {

    @Autowired
    ArticlesService articlesService

    void store(ArticleEvent articleEvent) {
        log.debug "storing event ${articleEvent}..."

        try {
            if (articleEvent.timestamp == null) {
                articleEvent.timestamp = LocalDateTime.now().toString()
            }

            if (articleEvent.articleId != null && articleEvent.type != null) {
                articlesService.storeEventWithArticle(articleEvent)
            } else {
                log.warn "skipped invalid event ${articleEvent}"
            }

        } catch (e) {
            log.error "storing event ${articleEvent} failed: $e.message", e
            throw new RuntimeException("storing event ${articleEvent} failed: $e.message", e)
        }
    }

    List<List> getLastEvents(Integer count) {
        log.info "getting last $count events..."

        try {
            List<List> lastEvents = articlesService.getWithEvents(null)
                    .collect({ article -> article.events })
                    .flatten()
                    .sort({ ArticleEvent articleEvent -> articleEvent.timestamp })
                    .reverse()
                    .take(count)
                    .collect({ ArticleEvent articleEvent ->
                        [ articleEvent.timestamp, articleEvent.type, articleEvent.articleId, articleEvent.userId ]
                    })

            return [[ 'timestamp', 'type', 'articleId', 'userId' ]] + lastEvents
        } catch (e) {
            log.error "getting last $count events failed: $e.message", e
            throw new RuntimeException("getting last $count events failed: $e.message", e)
        }
    }
}
