package ru.kwanza.jeda.context.jdbc;

import ru.kwanza.jeda.context.MapContextImpl;
import junit.framework.Assert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Тесты работы JDBCBlobContextController с терминатором заключается в запуске тестов базовой версии тестов контроллера
 * с пред и пост-обработкой.
 * <p/>
 * В тестах используется 2 контроллера: основной (из базовых тестов) и дополнительный (у всех установлены разные терминаторы и одна таблица БД)
 * Перед запуском каждого теста происходит добавление контекстов с использованием дополнительного контроллера.
 * После отработки теста, происходит проверка данных, добавленных с использованием дполнительного контроллера -- данные должны остаться неизмененными.
 *
 * @author Dmitry Zagorovsky
 */
public abstract class AbstractJDBCBlobContextControllerWithTermTest extends AbstractJDBCBlobContextControllerTest {

    protected JDBCBlobContextController ctxControllerWithTerm;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Модифицируем контроллер базового теста для работы с терминатором.
        ((JDBCBlobContextController) ctxController).setTableName(getContextTableName());
        ((JDBCBlobContextController) ctxController).setTerminator("anotherTerm");
        ((JDBCBlobContextController) ctxController).reInit();

        ctxControllerWithTerm = ctx.getBean("jdbcBlobContextControllerWithTerm", JDBCBlobContextController.class);
        //Добавляем контексты из другого контроллера с такими же идентификаторами как и у первого
        ctxControllerWithTerm.store(getContextList(null));
    }

    @Override
    public void tearDown() throws Exception {
        Map<String, MapContextImpl> actualMap = ctxControllerWithTerm.load(Arrays.asList(CONTEXT_IDS));
        // Проверяем, что контексты, добавленные вторым контроллером не "пострадали", пока работал первый
        Assert.assertEquals(getContextListAsMap(getContextList(1l)), actualMap);
        super.tearDown();
    }

    private List<MapContextImpl> getContextList(Long version) {
        List<MapContextImpl> testContextList = getTestContextList(version);
        for (MapContextImpl ctx : testContextList) {
            ctx.setTerminator(ctxControllerWithTerm.getTerminator());
            ctx.putAll(getTestMap("anotherTermContext", 5));
        }
        return testContextList;
    }

    @Override
    public String getDbUnitResourcePostfix() {
        return "WithTerm";
    }

    @Override
    public String getContextTableName() {
        return ctx.getBean("jdbcBlobContextControllerWithTerm", JDBCBlobContextController.class).getTableName();
    }

}
