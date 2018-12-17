package marquez.db.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jdbi.v3.sqlobject.customizer.SqlStatementCustomizingAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@SqlStatementCustomizingAnnotation(BindDbTableInfoRowFactory.class)
public @interface BindDbTableInfoRow {}
