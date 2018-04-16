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
            List<Model> allModels = modelsService.getAllModels()
            List<Model> activeModels = modelsService.getActiveModels()
            Model actualModel = modelsService.getActualModel()

            List<Article> articles = articlesService.getWithEvents()

            Set<Model> foundModels = []
            Map<Model, List<Double>> modelTrainErrors = [:]
            Map<Model, List<Double>> modelTestErrors = [:]

            articles.collect({ article -> article.events })
                    .flatten()
                    .findAll({ ArticleEvent articleEvent -> articleEvent.type == ArticleEventType.LIKED || articleEvent.type == ArticleEventType.DISLIKED })
                    .forEach({ ArticleEvent articleEvent ->
                        Article article = articles.find({ it.id == articleEvent.articleId })
                        article.ranks.forEach({ Rank rank ->
                            double expected = rank.value
                            double actual = articleEvent.type == ArticleEventType.LIKED ? 1.0 : -1.0
                            double error = (expected - actual) ** 2

                            Model model = allModels.find({ rank.id == "${it.name}:${it.version}" })

                            if (model != null) {
                                foundModels.add(model)
                                if (articleEvent.timestamp > model.timestamp && !articleEvent.timestamp.startsWith(model.timestamp)) {
                                    modelTestErrors.put(model, modelTestErrors.getOrDefault(model, []) + error)
                                } else {
                                    modelTrainErrors.put(model, modelTrainErrors.getOrDefault(model, []) + error)
                                }
                            }
                        })
                    })

            List<List> models = foundModels.collect({ model ->
                String status = '-'
                if (model == actualModel) {
                    status = '*'
                } else if (activeModels.contains(model)) {
                    status = '+'
                }

                double trainError
                int trainSize

                if (modelTrainErrors.containsKey(model)) {
                    trainError = Math.sqrt(modelTrainErrors.get(model).sum() / modelTrainErrors.get(model).size())
                    trainSize = modelTrainErrors.get(model).size()
                } else {
                    trainError = 0.0
                    trainSize = 0
                }

                double testError
                int testSize

                if (modelTestErrors.containsKey(model)) {
                    testError = Math.sqrt(modelTestErrors.get(model).sum() / modelTestErrors.get(model).size())
                    testSize = modelTestErrors.get(model).size()
                } else {
                    testError = 0.0
                    testSize = 0
                }

                [ status, model.name, model.version, model.timestamp, model.description, trainError, trainSize, testError, testSize ]
            })
            .sort({ it[8] > 0 ? it[7] : Double.MAX_VALUE })

            return [[ 'status', 'name', 'version', 'timestamp', 'description', 'train error', 'train size', 'test error', 'test size' ]] + models
        } catch (e) {
            log.error "monitoring models failed: $e.message", e
            throw new RuntimeException("monitoring models failed: $e.message", e)
        }
    }
}
