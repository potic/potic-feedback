package me.potic.feedback.service

import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.feedback.domain.Article
import me.potic.feedback.domain.ArticleEvent
import me.potic.feedback.domain.ArticleEventType
import me.potic.feedback.domain.Rank
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

    List<Article> getWithEvents(Integer count = null) {
        log.debug "getting $count articles with events..."

        try {
            String params = ''
            if (count != null) {
                params = "(count: ${count})"
            }

            def response = articlesServiceRest.post {
                request.uri.path = '/graphql'
                request.contentType = 'application/json'
                request.body = [ query: """
                    {
                      withEvents${params} {
                        id
                        
                        userId
                        
                        fromPocket {
                            status
                            time_added
                            time_read
                            word_count
                            has_image
                            has_video
                        }
                        
                        card {
                            source
                            title
                        }
                        
                        events {
                            userId
                            articleId
                            type
                            timestamp
                        }
                        
                        ranks {
                            id
                            value
                        }
                      }
                    }
                """ ]
            }

            List errors = response.errors
            if (errors != null && !errors.empty) {
                throw new RuntimeException("Request failed: $errors")
            }

            return response.data.withEvents.collect({
                it['events'] = it['events'].collect({ event -> new ArticleEvent(userId: event['userId'], articleId: event['articleId'], type: ArticleEventType.valueOf(event['type']), timestamp: event['timestamp']) })
                it['ranks'] = it['ranks'].collect({ rank -> new Rank(id: rank['id'], value: Double.parseDouble(rank['value'].toString())) })
                new Article(it)
            })
        } catch (e) {
            log.error "getting $count articles with events failed: $e.message", e
            throw new RuntimeException("getting $count articles with events failed: $e.message", e)
        }
    }
}
