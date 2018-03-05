package me.potic.feedback.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
class ArticleEvent {

    String userId

    String articleId

    ArticleEventType type

    String timestamp
}
