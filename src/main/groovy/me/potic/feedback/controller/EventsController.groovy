package me.potic.feedback.controller

import groovy.util.logging.Slf4j
import me.potic.feedback.domain.ArticleEvent
import me.potic.feedback.service.DatasetService
import me.potic.feedback.service.FeedbackService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletResponse

@RestController
@Slf4j
class EventsController {

    @Autowired
    FeedbackService feedbackService

    @Autowired
    DatasetService datasetService

    @CrossOrigin
    @PostMapping(path = '/event')
    @ResponseBody ResponseEntity<Void> event(@RequestBody ArticleEvent articleEvent) {
        log.info "receive POST request for /event; body=${articleEvent}"

        try {
            feedbackService.store(articleEvent)
            return new ResponseEntity<>(HttpStatus.OK)
        } catch (e) {
            log.error "POST request for /event; body=${articleEvent} failed: $e.message", e
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @CrossOrigin
    @GetMapping(path = '/train')
    void getEventsTrainDataset(
            @RequestParam(value = 'count', required = false) Integer count,
            HttpServletResponse response
    ) {
        log.info "receive GET request for /train?count=${count}"

        try {
            response.outputStream.withPrintWriter { writer ->
                datasetService.getEventsTrainDataset(count).forEach({
                    writer.write(it)
                    writer.write('\n')
                })
            }
        } catch (e) {
            log.error "GET request for /train?count=${count} failed: $e.message", e
            throw new RuntimeException("GET request for /train?count=${count} failed: $e.message", e)
        }
    }
}
