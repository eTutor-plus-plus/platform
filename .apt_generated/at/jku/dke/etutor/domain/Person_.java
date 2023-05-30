package at.jku.dke.etutor.domain;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Person.class)
public abstract class Person_ {

	public static volatile SingularAttribute<Person, Long> id;
	public static volatile SingularAttribute<Person, User> user;

	public static final String ID = "id";
	public static final String USER = "user";

}

