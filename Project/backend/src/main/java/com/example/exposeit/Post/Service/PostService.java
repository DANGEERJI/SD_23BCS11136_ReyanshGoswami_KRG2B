package com.example.exposeit.Post.Service;

import ch.hsr.geohash.GeoHash;
import com.example.exposeit.Post.Entity.Post;
import com.example.exposeit.Post.Entity.PostDocument;
import com.example.exposeit.Post.Repository.PostElasticSearchRepository;
import com.example.exposeit.Post.Repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostElasticSearchRepository esRepository;

    @Transactional
    public void createPost(Post postRequest){
        Post savedPost = postRepository.saveAndFlush(postRequest);

        String exactGeohash = GeoHash.withCharacterPrecision(
                savedPost.getLatitude(),
                savedPost.getLongitude(),
                7
        ).toBase32();

        Set<String> stringCategories = savedPost.getCategories().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        PostDocument esDoc = PostDocument
                .builder()
                .id(savedPost.getId())
                .title(savedPost.getTitle())
                .description(savedPost.getDescription())
                .authorName(savedPost.getAuthor().getUsername())
                .geohash(exactGeohash)
                .geohashPrefix(exactGeohash.substring(0, 4))
                .categories(stringCategories)
                .mediaFiles(savedPost.getMediaFiles())
                .likes(savedPost.getLikes())
                .createdAtEpoch(savedPost.getCreatedAt().toEpochMilli())
                .build();

        esRepository.save(esDoc);
    }

    public List<PostDocument> getGlobalTrending(){
        PageRequest top50 = PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "likes"));
        return esRepository.findAll(top50).getContent();
    }

    public List<PostDocument> getNearbyRecent(double lat, double lon){
        List<String> searchPrefixes = getNineBoxGeohashes(lat, lon);
        Sort recentSort = Sort.by(Sort.Direction.DESC, "createdAtEpoch");
        System.out.println(esRepository.findByGeohashPrefixIn(searchPrefixes, recentSort));

        return esRepository.findByGeohashPrefixIn(searchPrefixes, recentSort);
    }

    public List<PostDocument> getPostByCategory(Set<String> categories){
        Sort recentSort = Sort.by(Sort.Direction.DESC, "createdAtEpoch");

        return esRepository.findByCategoriesIn(categories, recentSort);
    }

    private List<String> getNineBoxGeohashes(double lat, double lon){
        GeoHash centreBox = GeoHash.withCharacterPrecision(lat, lon, 4);

        GeoHash[] surroundingBoxes = centreBox.getAdjacent();

        List<String> prefixes = new ArrayList<>();
        prefixes.add(centreBox.toBase32());

        for(GeoHash box: surroundingBoxes)
            prefixes.add(box.toBase32());

        return prefixes;
    }
}
