package com.kloia.eventapis.view;

import com.kloia.eventapis.common.EventKey;

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
