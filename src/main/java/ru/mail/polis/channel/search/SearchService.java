package ru.mail.polis.channel.search;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Streams;
import ru.mail.polis.channel.Message;

/**
 * Allows full-text search over channel messages.
 */
public class SearchService implements Closeable {

    private static final String FIELD_TEXT = "text";
    private static final String FIELD_AUTHOR = "author_id";

    private final RestHighLevelClient client;
    private final String indexName;

    public SearchService(@NotNull String indexName,
                         @NotNull String host, int port) {
        this.indexName = indexName;
        this.client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(host, port)));
    }

    @NotNull
    public SearchResult search(@NotNull final SearchQuery query,
                               @NotNull final Paging paging) throws IOException {
        final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (query.getText() != null) {
            boolQuery.must()
                    .add(QueryBuilders
                            .termQuery(FIELD_TEXT, query.getText()));
        }
        if (query.getAuthorId() != null) {
            boolQuery.must()
                    .add(QueryBuilders
                            .termQuery(FIELD_AUTHOR, query.getAuthorId()));
        }
        sourceBuilder.query(boolQuery);
        sourceBuilder.from(paging.getOffset());
        sourceBuilder.size(paging.getCount());

        final SearchRequest searchRequest =
                new SearchRequest()
                        .indices(indexName)
                        .source(sourceBuilder);

        SearchResponse searchResponse =
                client.search(searchRequest, RequestOptions.DEFAULT);

        final List<Long> ids =
                Streams.stream(searchResponse.getHits())
                        .map(hit -> Long.parseLong(hit.getId()))
                        .collect(Collectors.toList());
        return new SearchResult(
                ids,
                searchResponse.getHits().getTotalHits().value);

    }

    public void index(@NotNull final Message message) throws IOException {
        final XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.field(FIELD_AUTHOR, message.getAuthorId());
            builder.field(FIELD_TEXT, message.getText());
        }
        builder.endObject();

        final IndexRequest indexRequest =
                new IndexRequest(indexName)
                        .id(String.valueOf(message.getId()))
                        .source(builder);

        client.index(indexRequest, RequestOptions.DEFAULT);
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
