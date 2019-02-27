package marquez.service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DataSourceConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.db.DataSourceDao;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.models.DataSource;

import java.util.List;


@Slf4j
public class DataSourceService {
    private final DataSourceDao dataSourceDao;

    public DataSourceService(@NonNull final DataSourceDao dataSourceDao) {
        this.dataSourceDao = dataSourceDao;
    }

    public DataSource create(@NonNull DataSourceConnectionUrl connectionUrl,
                             @NonNull DatasourceName name) throws UnexpectedException {
        return null;

    }

    public List<DataSource> list() {

        return null;
    }


}
