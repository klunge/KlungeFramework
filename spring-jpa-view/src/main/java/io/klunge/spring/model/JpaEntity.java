package io.klunge.spring.model;

import io.klunge.common.EventKey;
import io.klunge.view.Entity;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
public abstract class JpaEntity implements Entity {

    @Id
    private String id;
    private int version;

    public JpaEntity() {
    }

    public JpaEntity(String id, int version) {
        this.id = id;
        this.version = version;
    }

    @Override
    public EventKey getEventKey() {
        return new EventKey(id, version);
    }

}
