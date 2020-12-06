package io.klunge.cassandra;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.klunge.view.Entity;
import io.klunge.view.EntityFunctionSpec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author Zeldal Özdemir
 */
@Slf4j
public class CassandraViewQuery<E extends Entity> extends BaseCassandraViewQuery {

    public CassandraViewQuery(String tableName, CassandraSession cassandraSession, ObjectMapper objectMapper, List<EntityFunctionSpec<E, ?>> commandSpecs) {
        super(tableName, cassandraSession, objectMapper, commandSpecs);
    }

}
