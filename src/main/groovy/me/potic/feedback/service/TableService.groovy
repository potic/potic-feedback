package me.potic.feedback.service

import groovy.util.logging.Slf4j
import org.springframework.web.bind.annotation.RestController

@RestController
@Slf4j
class TableService {

    List<String> table(List<List> data) {
        Map<Integer, Integer> columnWidth = [:]
        data.forEach({ List dataLine ->
            dataLine.withIndex().collect({ cell, index -> columnWidth.put(index, Math.max(columnWidth.getOrDefault(index, 0), cell.toString().length())) })
        })

        data.collect({ List dataLine ->
            dataLine.withIndex().collect({ cell, index -> cell.toString().padLeft(columnWidth.get(index) + 3)}).join(' ')
        })
    }
}
