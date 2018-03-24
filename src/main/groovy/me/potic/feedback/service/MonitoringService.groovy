package me.potic.feedback.service

import groovy.util.logging.Slf4j
import me.potic.feedback.domain.Article
import me.potic.feedback.domain.ArticleEvent
import me.potic.feedback.domain.ArticleEventType
import me.potic.feedback.domain.Rank
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
                        List<String> ranks = article.ranks.collect({ "${it.id}=${it.value}" })
                        [ articleEvent.timestamp, articleEvent.type, ranks, article.card.source, articleEvent.articleId, articleEvent.userId ]
                    })

            return [[ 'timestamp', 'type', 'rank', 'source', 'articleId', 'userId' ]] + latestArticles
        } catch (e) {
            log.error "monitoring latest $count articles failed: $e.message", e
            throw new RuntimeException("monitoring latest $count articles failed: $e.message", e)
        }
    }

    List<List> monitorRanks() {
        log.debug "monitoring ranks..."

        try {
            List<Article> articles = articlesService.getWithEvents()
            Map<String, List<Double>> ranks = [:]

            articles.collect({ article -> article.events })
                    .flatten()
                    .findAll({ ArticleEvent articleEvent -> articleEvent.type == ArticleEventType.LIKED || articleEvent.type == ArticleEventType.DISLIKED })
                    .forEach({ ArticleEvent articleEvent ->
                        Article article = articles.find({ it.id == articleEvent.articleId })
                        article.ranks.forEach({ Rank rank ->
                            double expected = rank.value
                            double actual = articleEvent.type == ArticleEventType.LIKED ? 1.0 : -1.0
                            double error = (expected - actual) ** 2

                            ranks.put(rank.id, ranks.getOrDefault(rank.id, []) + error)
                        })
                    })

            List<List> monitorRanks = ranks.collect({ rankId, errors ->
                double error = Math.sqrt(errors.sum() / errors.size())
                [ rankId, error, errors.size() ]
            })

            return [[ 'rank', 'error', 'count' ]] + monitorRanks
        } catch (e) {
            log.error "monitoring ranks failed: $e.message", e
            throw new RuntimeException("monitoring ranks failed: $e.message", e)
        }
    }
}
