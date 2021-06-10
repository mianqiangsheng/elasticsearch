package com.lizhen.elasticsearch.controller;

import io.swagger.annotations.Api;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchPhrasePrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/es")
@Api(value = "/es", tags = "操作Elasticsearch")
public class ElasticSearchController {

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @RequestMapping(value = "/getIndex", method = RequestMethod.GET)
    public ResponseEntity<String> getIndex(){
        try {
            SearchRequest searchRequest = new SearchRequest().indices("content");
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            MatchPhrasePrefixQueryBuilder mppqb = QueryBuilders.matchPhrasePrefixQuery("name", "lizhen");
            sourceBuilder.query(mppqb);
            SearchResponse sr = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            String result = sr.toString();
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/putIndex", method = RequestMethod.PUT)
    public ResponseEntity<String> putIndex(){
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", "20210406");
            map.put("name", "lihuizhang");
            map.put("age", 54);
            try {
                IndexRequest indexRequest = new IndexRequest("content").id(map.get("id").toString()).source(map);
                IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
                System.out.println(indexResponse.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResponseEntity.status(HttpStatus.OK).body("成功");
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
