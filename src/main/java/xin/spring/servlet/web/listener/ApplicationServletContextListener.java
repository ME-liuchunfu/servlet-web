package xin.spring.servlet.web.listener;

import xin.spring.servlet.web.core.ExceptionAdviseHandler;
import xin.spring.servlet.web.core.MybatisFactory;
import xin.spring.servlet.web.core.ScanClass;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener(value = "applicationListener")
public class ApplicationServletContextListener implements ServletContextListener {

    private static Logger logger = Logger.getLogger(ApplicationServletContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ScanClass.getScanClass(this.getClass().getClassLoader(), true);
            ExceptionAdviseHandler.getExceptionAdviseHandler();
            MybatisFactory.getDefaultMybatisFactory();
        } catch (Exception e) {
            e.printStackTrace();
            logger.warning(ExceptionAdviseHandler.convertToStringBuilder(e).toString());
        }
        logger.info("applicationListener start success!!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("applicationListener close success!!");
    }
}
