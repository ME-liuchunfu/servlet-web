package xin.spring.servlet.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import xin.spring.servlet.web.annotation.GetMapping;
import xin.spring.servlet.web.annotation.PostMapping;
import xin.spring.servlet.web.annotation.RequestMapping;
import xin.spring.servlet.web.annotation.ResponseBody;
import xin.spring.servlet.web.utils.IdWorker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RequestMapping(value = "/solr")
public class SolrServlet {

    @GetMapping(value = "/get")
    public void get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write(JSON.toJSONString(request.getParameterMap()));
        writer.flush();
    }

    @ResponseBody
    @GetMapping(value = "/getObj")
    public Object getObj(HttpServletRequest request, HttpServletResponse response, Obj obj) throws IOException {
        return obj;
    }

    HttpSolrClient solrServer = new HttpSolrClient.Builder("http://localhost:8983/solr/meta_forum").build();
    @ResponseBody
    @PostMapping(value = "update")
    public Object update(Forum forum) throws IOException, SolrServerException {

        long start = TimeUnit.NANOSECONDS.toNanos(System.nanoTime());
        //1.创建连接
        //3.将文档写入索引库中
        UpdateResponse response = solrServer.addBean(forum);
        //4.提交
        solrServer.commit();
//        solrServer.close();
        long end = TimeUnit.NANOSECONDS.toNanos(System.nanoTime());
        printTime(start, end);
        return response.getResponse().toString();
    }

    @ResponseBody
    @PostMapping(value = "add")
    public Object add(Forum forum) throws IOException, SolrServerException {

        long start = TimeUnit.NANOSECONDS.toNanos(System.nanoTime());
        //1.创建连接
//        HttpSolrClient solrServer = new HttpSolrClient.Builder("http://localhost:8983/solr/meta_forum").build();
        //2.创建一个文档对象
        if (Objects.isNull(forum.getId())) {
            forum.setId(String.valueOf(IdWorker.defaultInstance().nextId()));
        }
        //3.将文档写入索引库中
        UpdateResponse response = solrServer.addBean(forum);
        //4.提交
        solrServer.commit();
//        solrServer.close();
        long end = TimeUnit.NANOSECONDS.toNanos(System.nanoTime());
        printTime(start, end);
        return response.getResponse().toString();
    }

    @ResponseBody
    @PostMapping(value = "addMap")
    public Object addMap(Map<String, Object> map) throws IOException, SolrServerException {

        long start = TimeUnit.NANOSECONDS.toNanos(System.nanoTime());
        //1.创建连接
//        HttpSolrClient solrServer = new HttpSolrClient.Builder("http://localhost:8983/solr/meta_forum").build();
        //2.创建一个文档对象
        SolrInputDocument inputDocument = new SolrInputDocument();
        Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
        inputDocument.addField("id", IdWorker.defaultInstance().nextId());
        while (iterator.hasNext()) {
            //向文档中添加域以及对应的值(注意：所有的域必须在schema.xml中定义过,前两篇导入时已定义)
            Map.Entry<String, Object> next = iterator.next();
            inputDocument.addField(next.getKey(), next.getValue());
        }
        //3.将文档写入索引库中
        solrServer.add(inputDocument);
        //4.提交
        solrServer.commit();
//        solrServer.close();
        long end = TimeUnit.NANOSECONDS.toNanos(System.nanoTime());
        printTime(start, end);
        return inputDocument;
    }

    @ResponseBody
    @GetMapping("/query")
    public Object query(Map<String, String> q) throws IOException, SolrServerException {
        System.out.println(q);
        long start = TimeUnit.NANOSECONDS.toNanos(System.nanoTime());
        //1.创建连接
//        HttpSolrClient solrServer = new HttpSolrClient.Builder("http://localhost:8983/solr/meta_forum").build();
        SolrQuery solrQuery = new SolrQuery(q.get("q"));
        solrQuery.setHighlight(true);
        solrQuery.addHighlightField("name");
        solrQuery.addHighlightField("title");
        solrQuery.addHighlightField("content");
        solrQuery.setHighlightSimplePre("<span style='color:red;'>");
        solrQuery.setHighlightSimplePost("</span>");
        QueryResponse query = solrServer.query(solrQuery);

        SolrDocumentList response = query.getResults();
        System.out.println(JSON.toJSONString(response));
        System.out.println("response.getMaxScore():" + response.getMaxScore());
        System.out.println("response.getNumFound():" + response.getNumFound());
        System.out.println("response.getNumFoundExact():" + response.getNumFoundExact());
        System.out.println("response.getStart():" + response.getStart());
        System.out.println("response.toString():" + response.toString());

//        for (SolrDocument document : response) {
//            document.get()
//        }
        Map<String, Map<String, List<String>>> highlighting = query.getHighlighting();
//        solrServer.close();
        long end = TimeUnit.NANOSECONDS.toNanos(System.nanoTime());
        printTime(start, end);
        JSONObject object = new JSONObject();
        object.put("highlighting", highlighting);
        object.put("response", response);
        return object;
    }

    public void printTime(long start, long end) {
        long i = end - start;
        long sub = (long) 1e6;
        System.out.println("耗时：" + (i / sub) + "." + (i % sub) + " 毫秒");
    }
}
