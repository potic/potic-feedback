package me.potic.feedback.service

import groovy.util.logging.Slf4j
import me.potic.feedback.domain.Article
import me.potic.feedback.domain.ArticleEvent
import me.potic.feedback.domain.ArticleEventType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class MonitoringService {

    @Autowired
    ArticlesService articlesService

    List<List> monitorLatestEvents(int count) {
        log.debug "monitoring latest $count events..."

        try {
            List<List> latestEvents = articlesService.getWithEvents()
                    .collect({ article -> article.events })
                    .flatten()
                    .sort({ ArticleEvent articleEvent -> articleEvent.timestamp })
                    .reverse()
                    .take(count)
                    .collect({ ArticleEvent articleEvent ->
                        [ articleEvent.timestamp, articleEvent.type, articleEvent.articleId, articleEvent.userId ]
                    })

            return [[ 'timestamp', 'type', 'articleId', 'userId' ]] + latestEvents
        } catch (e) {
            log.error "monitoring latest $count events failed: $e.message", e
            throw new RuntimeException("monitoring latest $count events failed: $e.message", e)
        }
    }

    List<List> monitorLatestArticles(int count) {
        log.debug "monitoring latest $count articles..."

        try {
            List<Article> articles = articlesService.getWithEvents()

            List<List> latestArticles = articles
                    .collect({ article -> article.events })
                    .flatten()
                    .findAll({ ArticleEvent articleEvent -> articleEvent.type == ArticleEventType.LIKED || articleEvent.type == ArticleEventType.DISLIKED })
                    .sort({ ArticleEvent articleEvent -> articleEvent.timestamp })
                    .reverse()
                    .take(count)
                    .collect({ ArticleEvent articleEvent ->
                        Article article = articles.find({ it.id == articleEvent.articleId })
                        [ articleEvent.timestamp, articleEvent.type, article.ranks, article.card.source, articleEvent.articleId, articleEvent.userId ]
                    })

            return [[ 'timestamp', 'type', 'rank', 'source', 'articleId', 'userId' ]] + latestArticles
        } catch (e) {
            log.error "monitoring latest $count articles failed: $e.message", e
            throw new RuntimeException("monitoring latest $count articles failed: $e.message", e)
        }
    }
}
