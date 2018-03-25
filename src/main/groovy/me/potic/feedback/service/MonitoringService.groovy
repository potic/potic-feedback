package me.potic.feedback.service

import groovy.util.logging.Slf4j
import me.potic.feedback.domain.Article
import me.potic.feedback.domain.ArticleEvent
import me.potic.feedback.domain.ArticleEventType
import me.potic.feedback.domain.Model
import me.potic.feedback.domain.Rank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class MonitoringService {

    @Autowired
    ArticlesService articlesService

    @Autowired
    ModelsService modelsService

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
                        List<String> ranks = article.ranks.collect({ "${it.id}=${String.format(Locale.US, '%.4f', it.value)}" })
                        [ articleEvent.timestamp, articleEvent.type, ranks, article.card.source, articleEvent.articleId, articleEvent.userId ]
                    })

            return [[ 'timestamp', 'type', 'rank', 'source', 'articleId', 'userId' ]] + latestArticles
        } catch (e) {
            log.error "monitoring latest $count articles failed: $e.message", e
            throw new RuntimeException("monitoring latest $count articles failed: $e.message", e)
        }
    }

    List<List> monitorModels() {
        log.debug "monitoring models..."

        try {
            List<Model> activeModels = modelsService.getActiveModels()
            Model actualModel = modelsService.getActualModel()

            List<Article> articles = articlesService.getWithEvents()

            Map<Model, List<Double>> modelErrors = [:]
            Map<Model, List<Double>> modelAfterReleaseErrors = [:]

            articles.collect({ article -> article.events })
                    .flatten()
                    .findAll({ ArticleEvent articleEvent -> articleEvent.type == ArticleEventType.LIKED || articleEvent.type == ArticleEventType.DISLIKED })
                    .forEach({ ArticleEvent articleEvent ->
                        Article article = articles.find({ it.id == articleEvent.articleId })
                        article.ranks.forEach({ Rank rank ->
                            double expected = rank.value
                            double actual = articleEvent.type == ArticleEventType.LIKED ? 1.0 : -1.0
                            double error = (expected - actual) ** 2

                            Model model = activeModels.find({ rank.id == "${it.name}:${it.version}" })

                            if (model != null) {
                                modelErrors.put(model, modelErrors.getOrDefault(model, []) + error)
                                if (articleEvent.timestamp >= model.timestamp) {
                                    modelAfterReleaseErrors.put(model, modelAfterReleaseErrors.getOrDefault(model, []) + error)
                                }
                            }
                        })
                    })

            List<List> models = modelErrors.collect({ model, errors ->
                String isActual = model == actualModel ? '*' : ' '

                double error = Math.sqrt(errors.sum() / errors.size())
                int size = errors.size()

                double errorAfterRelease
                int sizeAfterRelease

                if (modelAfterReleaseErrors.containsKey(model)) {
                    errorAfterRelease = Math.sqrt(modelAfterReleaseErrors.get(model).sum() / modelAfterReleaseErrors.get(model).size())
                    sizeAfterRelease = modelAfterReleaseErrors.get(model).size()
                } else {
                    errorAfterRelease = 0.0
                    sizeAfterRelease = 0
                }

                [ isActual, model.name, model.version, model.timestamp, model.description, error, size, errorAfterRelease, sizeAfterRelease ]
            })

            return [[ 'actual', 'name', 'version', 'timestamp', 'description', 'total error', 'total dataset size', 'after release error', 'after release dataset size' ]] + models
        } catch (e) {
            log.error "monitoring models failed: $e.message", e
            throw new RuntimeException("monitoring models failed: $e.message", e)
        }
    }
}
