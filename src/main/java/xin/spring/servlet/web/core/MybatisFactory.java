package xin.spring.servlet.web.core;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Objects;

public class MybatisFactory {

    private static MybatisFactory defaultMybatisFactory = new MybatisFactory();

    SqlSessionFactory sqlSessionFactory;
    public static final String resource = "mybatis-config.xml";

    public static MybatisFactory getDefaultMybatisFactory() {
        return defaultMybatisFactory;
    }

    public MybatisFactory() {
        this(resource);
    }

    public MybatisFactory(String path) {
        try(InputStream inputStream = Resources.getResourceAsStream(path)) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MybatisFactory(InputStream inp) {
        Objects.requireNonNull(inp);
        try(BufferedInputStream bis = new BufferedInputStream(inp)) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(bis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SqlSession openSession() {
        return sqlSessionFactory.openSession();
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

}
