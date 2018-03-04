package me.potic.feedback.service

import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.feedback.domain.ArticleEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@Slf4j
class ArticlesService {

    HttpBuilder articlesServiceRest

    @Autowired
    HttpBuilder articlesServiceRest(@Value('${services.articles.url}') String articlesServiceUrl) {
        articlesServiceRest = HttpBuilder.configure {
            request.uri = articlesServiceUrl
        }
    }

    void storeEventWithArticle(ArticleEvent articleEvent) {
        log.debug "storing event ${articleEvent} with its article..."

        articlesServiceRest.post {
            request.uri.path = "/article/${articleEvent.articleId}/event"
            request.body = articleEvent
            request.contentType = 'application/json'
        }
    }
}
