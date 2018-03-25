package me.potic.feedback.service

import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.feedback.domain.Model
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@Slf4j
class ModelsService {

    HttpBuilder modelsServiceRest

    @Autowired
    HttpBuilder modelsServiceRest(@Value('${services.models.url}') String modelsServiceUrl) {
        modelsServiceRest = HttpBuilder.configure {
            request.uri = modelsServiceUrl
        }
    }

    Model getActualModel() {
        log.debug "requesting actual model..."

        try {
            def response = modelsServiceRest.get {
                request.uri.path = '/actual'
                request.contentType = 'application/json'
            }

            return new Model(response)
        } catch (e) {
            log.error "requesting actual model failed: $e.message", e
            throw new RuntimeException("requesting actual model failed", e)
        }
    }

    List<Model> getActiveModels() {
        log.debug "requesting active models..."

        try {
            def response = modelsServiceRest.get {
                request.uri.path = '/active'
                request.contentType = 'application/json'
            }

            return response.collect({ new Model(it) })
        } catch (e) {
            log.error "requesting active models failed: $e.message", e
            throw new RuntimeException("requesting active models failed", e)
        }
    }
}
