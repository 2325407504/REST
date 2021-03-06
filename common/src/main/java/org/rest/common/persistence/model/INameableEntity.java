package org.rest.common.persistence.model;

public interface INameableEntity extends IEntity {

    String getName();

    void setName(final String nameToSet);

}
