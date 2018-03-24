package me.potic.feedback.controller

import groovy.util.logging.Slf4j
import me.potic.feedback.service.DatasetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletResponse

@RestController
@Slf4j
class DatasetController {

    @Autowired
    DatasetService datasetService

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
