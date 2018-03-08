package me.potic.feedback.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
class PocketArticle {

    String status

    Long time_added
    Long time_read

    String word_count

    String has_image
    String has_video
}
