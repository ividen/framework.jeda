package ru.kwanza.jeda.context.jdbc;

import ru.kwanza.jeda.context.TestObject;
import junit.framework.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Zagorovsky
 */
public abstract class AbstractObjectContextControllerWithTermTest extends AbstractObjectContextControllerTest {

    protected JDBCObjectContextController<TestObject> ctxControllerWithTerm;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        //noinspection unchecked
        ctxControllerWithTerm = ctx.getBean("jdbcObjectContextControllerWithTerm", JDBCObjectContextController.class);
        //Добавляем контексты из другого контроллера с такими же идентификаторами как и у первого
        ctxControllerWithTerm.store(getContextList(null));

        // Модифицируем контроллер базового теста для работы с терминатором.
        ctxController.setTableName(getContextTableName());
        ctxController.setTerminator("anotherTerm");
        ctxController.reInit();
    }

    @Override
    public void tearDown() throws Exception {
        Map<Long, TestObject> actualMap = ctxControllerWithTerm.load(Arrays.asList(CONTEXT_IDS));
        // Проверяем, что контексты, добавленные вторым контроллером не "пострадали", пока работал первый
        Assert.assertEquals(getContextListAsMap(getContextList(1l)), actualMap);
        super.tearDown();
    }

    private List<TestObject> getContextList(Long version) {
        List<TestObject> testContextList = getTestContextList(version);
        for (TestObject ctx : testContextList) {
            ctx.setData1("anotherTermData");
        }
        return testContextList;
    }

    @Override
    public String getContextTableName() {
        return ctx.getBean("jdbcObjectContextControllerWithTerm", JDBCObjectContextController.class).getTableName();
    }

}
