package ru.kwanza.jeda.context.jdbc;

import ru.kwanza.autokey.api.IAutoKey;
import ru.kwanza.autokey.mock.MockAutoKeyImpl;
import ru.kwanza.jeda.context.AbstractBlobContextControllerTest;
import ru.kwanza.jeda.context.DBUnitUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Dmitry Zagorovsky
 */
public abstract class AbstractJDBCBlobContextControllerTest extends AbstractBlobContextControllerTest {

    protected DBUnitUtil dbUnitUtil;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Connection conn = ctx.getBean("dataSource", BasicDataSource.class).getConnection();
        dbUnitUtil = new DBUnitUtil(conn);

        ctxController = ctx.getBean("jdbcBlobContextController", JDBCBlobContextController.class);

        resetAutoKey();
        conn.prepareStatement("DELETE FROM " + getContextTableName()).execute();
    }

    public void testContextUpdateWithOptimistic() throws Exception {
        super.testContextUpdateWithOptimistic();
        dbUnitUtil.assertDBTable(getContextTableName(),
                "jdbc/testContextUpdateWithOptimistic" + getDbUnitResourcePostfix() + ".xml");
    }

    private void resetAutoKey() {
        IAutoKey autoKey = ctx.getBean("autokey.IAutoKey", IAutoKey.class);
        ((MockAutoKeyImpl) autoKey).resetSequences();
    }

    protected String getDbUnitResourcePostfix() {
        return "";
    }

    protected String getContextTableName() {
        return "blob_context";
    }

    protected void executeServiceQuery(String sql) throws SQLException {
        ApplicationContext srvCtx = new ClassPathXmlApplicationContext(getContextFileName(), this.getClass());
        Connection conn = srvCtx.getBean("dataSource", BasicDataSource.class).getConnection();
        conn.prepareStatement(sql).execute();
        ((ClassPathXmlApplicationContext) srvCtx).close();
    }

}
