package at.jku.dke.etutor.domain;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(FileEntity.class)
public abstract class FileEntity_ {

	public static volatile SingularAttribute<FileEntity, LocalDateTime> submitTime;
	public static volatile SingularAttribute<FileEntity, Long> size;
	public static volatile SingularAttribute<FileEntity, Student> student;
	public static volatile SingularAttribute<FileEntity, String> name;
	public static volatile SingularAttribute<FileEntity, Long> id;
	public static volatile SingularAttribute<FileEntity, String> contentType;
	public static volatile SingularAttribute<FileEntity, byte[]> content;

	public static final String SUBMIT_TIME = "submitTime";
	public static final String SIZE = "size";
	public static final String STUDENT = "student";
	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String CONTENT_TYPE = "contentType";
	public static final String CONTENT = "content";

}

