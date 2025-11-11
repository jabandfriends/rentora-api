package com.rentora.api.service.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.rentora.api.mapper.TenantMapper;
import com.rentora.api.model.entity.ApartmentUser;
import com.rentora.api.model.entity.elastic.ApartmentUserDocument;
import com.rentora.api.repository.ApartmentUserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor()
public class ApartmentUserEsService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ApartmentUserRepository apartmentUserRepository;
    private final ElasticsearchClient elasticsearchClient;

    private final TenantMapper tenantMapper;


    // Sync all data on application startup
    @PostConstruct
    public void init() {
        createIndexWithThaiAnalyzer();
        syncAllToElasticsearch();
    }

    public void syncAllToElasticsearch() {
        List<ApartmentUser> users = apartmentUserRepository.findAllWithRelations();

        List<ApartmentUserDocument> documents = users.stream()
                .map(tenantMapper::toApartmentUserDocument)
                .toList();

        elasticsearchOperations.save(documents);
        elasticsearchOperations.indexOps(ApartmentUserDocument.class).refresh();
    }

    public void createIndexWithThaiAnalyzer() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(ApartmentUserDocument.class);

        if (!indexOps.exists()) {
            // Settings with Thai analyzer + edge_ngram for partial search
            Map<String, Object> settings = new HashMap<>();
            Map<String, Object> analysis = new HashMap<>();
            Map<String, Object> analyzer = new HashMap<>();
            Map<String, Object> filter = new HashMap<>();

            Map<String, Object> edgeNGram = new HashMap<>();
            edgeNGram.put("type", "edge_ngram");
            edgeNGram.put("min_gram", 2);
            edgeNGram.put("max_gram", 20);
            filter.put("thai_edge_ngram_filter", edgeNGram);

            Map<String, Object> thaiEdgeAnalyzer = new HashMap<>();
            thaiEdgeAnalyzer.put("type", "custom");
            thaiEdgeAnalyzer.put("tokenizer", "thai"); // built-in Thai tokenizer
            thaiEdgeAnalyzer.put("filter", Arrays.asList("lowercase", "thai_edge_ngram_filter"));
            analyzer.put("thai_edge_ngram", thaiEdgeAnalyzer);

            analysis.put("analyzer", analyzer);
            analysis.put("filter", filter);
            settings.put("analysis", analysis);

            indexOps.create(settings);

            indexOps.putMapping(indexOps.createMapping(ApartmentUserDocument.class));
        }
    }

    public void saveToEs(ApartmentUser user) throws IOException {
        ApartmentUserDocument doc = tenantMapper.toApartmentUserDocument(user);

        elasticsearchClient.index(i -> i
                .index("apartment_users")
                .id(doc.getApartmentUserId().toString())
                .document(doc)
        );
    }

    public void updateInEs(ApartmentUser user) throws IOException {
        ApartmentUserDocument doc = tenantMapper.toApartmentUserDocument(user);

        elasticsearchClient.update(u -> u
                        .index("apartment_users")
                        .id(doc.getApartmentUserId().toString())
                        .doc(doc)
                        .docAsUpsert(true),
                ApartmentUserDocument.class
        );
    }

    public Page<ApartmentUserDocument> searchUsers(String name, String apartmentId,Boolean isActive, Pageable pageable) {
        try {
            int from = (int) pageable.getOffset();
            int size = pageable.getPageSize();

            SearchResponse<ApartmentUserDocument> response = elasticsearchClient.search(s ->
                            s.index("apartment_users")
                                    .from(from)
                                    .size(size)
                                    .query(q -> {
                                        // If no filters, match all
                                        if ((name == null || name.isEmpty()) && (apartmentId == null || apartmentId.isEmpty())) {
                                            return q.matchAll(m -> m);
                                        }

                                        // Build bool query with filters
                                        return q.bool(b -> {
                                            // Name fuzzy + exact match
                                            if (name != null && !name.isEmpty()) {
                                                // Partial match using edge_ngram
                                                b.should(sh -> sh.match(match -> match
                                                        .field("fullName")
                                                        .query(name)
                                                ));

                                                // Fuzzy match for typos
                                                b.should(sh -> sh.match(match -> match
                                                        .field("fullName")
                                                        .query(name)
                                                        .fuzziness("AUTO")
                                                        .fuzzyTranspositions(true)
                                                ));

                                                b.minimumShouldMatch("1"); // at least one should match
                                            }

                                            // Apartment ID exact match
                                            if (apartmentId != null && !apartmentId.isEmpty()) {
                                                b.must(m -> m.term(t -> t.field("apartmentId.keyword").value(apartmentId)));
                                            }

                                            if(isActive != null){
                                                b.must(m -> m.term(t -> t.field("isActive").value(isActive)));
                                            }

                                            return b;
                                        });
                                    }),
                    ApartmentUserDocument.class
            );

            List<ApartmentUserDocument> users = response.hits().hits().stream()
                    .map(Hit::source)
                    .toList();

            long total = response.hits().total() != null ? response.hits().total().value() : users.size();

            return new PageImpl<>(users, pageable, total);

        } catch (Exception e) {
            return Page.empty();
        }
    }
}
