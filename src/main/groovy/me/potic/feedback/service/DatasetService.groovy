package me.potic.feedback.service

import groovy.util.logging.Slf4j
import me.potic.feedback.domain.Article
import me.potic.feedback.domain.ArticleEventType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class DatasetService {

    @Autowired
    ArticlesService articlesService

    List<String> getEventsTrainDataset(Integer count) {
        log.info "getting events train dataset of size $count..."

        try {
            List<String> result = [ 'id,user_id,read,read_time,read_duration,has_image,has_video,word_count,source,showed_count,skipped_count,liked_count,disliked_count' ]

            articlesService.getWithEvents(count).collect { Article article ->

                int showed_count = article.events.count { event -> event.type == ArticleEventType.SHOWED }
                int skipped_count = article.events.count { event -> event.type == ArticleEventType.SKIPPED }
                int liked_count = article.events.count { event -> event.type == ArticleEventType.LIKED }
                int disliked_count = article.events.count { event -> event.type == ArticleEventType.DISLIKED }

                long read_duration = Math.max(0, article.fromPocket.time_read - article.fromPocket.time_added)

                return  "${article.id}," +
                        "${article.userId}," +
                        "${article.fromPocket.status}," +
                        "${article.fromPocket.time_read}," +
                        "${read_duration}," +
                        "${article.fromPocket.has_image}," +
                        "${article.fromPocket.has_video}," +
                        "${article.fromPocket.word_count != null ? Long.parseLong(article.fromPocket.word_count) : null}," +
                        "${article.card.source}," +
                        "${showed_count}," +
                        "${skipped_count}," +
                        "${liked_count}," +
                        "${disliked_count}"
            }.forEach({ result.add(it) })

            return result
        } catch (e) {
            log.error "getting events train dataset of size $count failed: $e.message", e
            throw new RuntimeException("getting events train dataset of size $count failed: $e.message", e)
        }
    }
}
