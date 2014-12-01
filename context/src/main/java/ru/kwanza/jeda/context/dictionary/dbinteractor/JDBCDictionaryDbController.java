package ru.kwanza.jeda.context.dictionary.dbinteractor;

import ru.kwanza.autokey.api.IAutoKey;
import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.dbtool.core.KeyValue;
import ru.kwanza.jeda.context.MapContextImpl;
import ru.kwanza.jeda.context.dictionary.ContextDictionaryController;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JDBCDictionaryDbController implements DictionaryDbController {

    private static final String SEQUENCE_NAME = MapContextImpl.class.getName();

    private static final String INSERT_SQL = "INSERT INTO %s (%s, %s) VALUES (?,?)";
    private static final String SELECT_SQL = "SELECT %s FROM %s WHERE %s = ?";
    private static final String SELECT_ALL_SQL = "SELECT %s, %s FROM %s";

    private static final DictionaryRowMapper DICT_ROW_MAPPER = new DictionaryRowMapper();

    private DBTool dbTool;
    private IAutoKey autoKey;

    @Transactional(value = TransactionalType.REQUIRES_NEW)
    public Long storeNewProperty(String propertyName, ContextDictionaryController dictCtrl) {
        Long id = autoKey.getNextValue(SEQUENCE_NAME);
        try {
            dbTool.getJdbcTemplate().update(getInsertSql(dictCtrl), propertyName, id);
        } catch (DuplicateKeyException e) {
            id = readIdFromDb(propertyName, dictCtrl);
        }
        return id;
    }

    @Transactional(value = TransactionalType.REQUIRES_NEW)
    public Long readIdFromDb(String propertyName, ContextDictionaryController dictCtrl) {
        List<Long> list =
                dbTool.getJdbcTemplate().queryForList(getSelectByNameSql(dictCtrl), new Object[]{propertyName}, Long.class);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Transactional(value = TransactionalType.REQUIRES_NEW)
    public Map<String, Long> readAllDictionary(ContextDictionaryController dictCtrl) {
        return dbTool.selectMap(getSelectAllSql(dictCtrl), DICT_ROW_MAPPER);
    }

    @Transactional(value = TransactionalType.REQUIRES_NEW)
    public String readNameFromDb(Long propertyId, ContextDictionaryController dictCtrl) {
        return dbTool.getJdbcTemplate().queryForObject(getSelectByIdSql(dictCtrl),
                new Object[]{propertyId}, String.class);
    }

    protected String getInsertSql(ContextDictionaryController dictCtrl) {
        return String.format(INSERT_SQL, dictCtrl.getDictionaryTableName(),
                dictCtrl.getDictionaryPropertyColumnName(), dictCtrl.getDictionaryIdColumnName());
    }

    protected String getSelectByNameSql(ContextDictionaryController dictCtrl) {
        return String.format(SELECT_SQL, dictCtrl.getDictionaryIdColumnName(),
                dictCtrl.getDictionaryTableName(), dictCtrl.getDictionaryPropertyColumnName());
    }

    protected String getSelectByIdSql(ContextDictionaryController dictCtrl) {
        return String.format(SELECT_SQL, dictCtrl.getDictionaryPropertyColumnName(),
                dictCtrl.getDictionaryTableName(), dictCtrl.getDictionaryIdColumnName());
    }

    protected String getSelectAllSql(ContextDictionaryController dictCtrl) {
        return String.format(SELECT_ALL_SQL, dictCtrl.getDictionaryPropertyColumnName(),
                dictCtrl.getDictionaryIdColumnName(), dictCtrl.getDictionaryTableName());
    }

    public void setDbTool(DBTool dbTool) {
        this.dbTool = dbTool;
    }

    public void setAutoKey(IAutoKey autoKey) {
        this.autoKey = autoKey;
    }

    private static class DictionaryRowMapper implements RowMapper<KeyValue<String, Long>> {
        public KeyValue<String, Long> mapRow(ResultSet rs, int rowNum) throws SQLException {
            String name = rs.getString(1);
            Long id = rs.getLong(2);
            return new KeyValue<String, Long>(name, id);
        }
    }

}
