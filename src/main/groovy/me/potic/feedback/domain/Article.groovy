package me.potic.feedback.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder

@Builder
@EqualsAndHashCode(includes = 'id')
@ToString(includeNames = true)
class Article {

    String id

    String userId

    PocketArticle fromPocket

    Card card

    List<ArticleEvent> events

    List<Rank> ranks
}
