package com.rentora.api.repository.elastic;

import com.rentora.api.model.entity.elastic.ApartmentUserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.UUID;

public interface ApartmentUserEsRepository extends ElasticsearchRepository<ApartmentUserDocument, UUID> {
}