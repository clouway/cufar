package com.clouway.cufar.flag;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
public class RequiredAnnotationException extends RuntimeException {

  public RequiredAnnotationException() {
  }

  public RequiredAnnotationException(Class targetClass, Class annotation) {
    super(String.format("Class %s is not annotated with @%s", targetClass.getSimpleName(), annotation.getSimpleName()));
  }
}
