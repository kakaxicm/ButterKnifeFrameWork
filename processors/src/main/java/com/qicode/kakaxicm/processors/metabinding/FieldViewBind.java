package com.qicode.kakaxicm.processors.metabinding;

import com.qicode.kakaxicm.processors.VerifyUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public final class FieldViewBind {
  private final String name;
  private final TypeName type;
  private final boolean required;

  public FieldViewBind(String name, TypeName type, boolean required) {
    this.name = name;
    this.type = type;
    this.required = required;
  }

  public String getName() {
    return name;
  }

  public TypeName getType() {
    return type;
  }

  public ClassName getRawType() {
    if (type instanceof ParameterizedTypeName) {
      return ((ParameterizedTypeName) type).rawType;
    }
    return (ClassName) type;
  }

  public String getDescription() {
    return "field '" + name + "'";
  }

  public boolean isRequired() {
    return required;
  }

  public boolean requiresCast() {
    return !VerifyUtils.VIEW_TYPE.equals(type.toString());
  }

}
