package xin.spring.servlet.web;

import org.apache.solr.client.solrj.beans.Field;

import java.util.Date;

public class Forum {

    @Field
    private String id;
    @Field
    private String name;
    @Field
    private String content;
    @Field
    private String toForum;
    @Field
    private Long time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToForum() {
        return toForum;
    }

    public void setToForum(String toForum) {
        this.toForum = toForum;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
