package com.example.exposeit.Post.Repository;

import com.example.exposeit.Post.Entity.PostDocument;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PostElasticSearchRepository extends ElasticsearchRepository<PostDocument, UUID> {
    List<PostDocument> findByGeohashPrefixIn(List<String> geohashes, Sort sort);

    List<PostDocument> findByCategoriesIn(Set<String> categories, Sort sort);
}
