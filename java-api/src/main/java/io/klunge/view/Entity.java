package io.klunge.view;

import io.klunge.common.EventKey;

import java.io.Serializable;

/**
 * @author Zeldal Özdemir
 */
public interface Entity extends Serializable {

    EventKey getEventKey();

    String getId();

    void setId(String id);

    int getVersion();

    void setVersion(int version);
}
