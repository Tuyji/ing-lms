package com.ing.loan.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark methods that require authorization.
 * The AuthorizationAspect will intercept these methods and check if the
 * logged-in user is authorized to perform the action.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorizeAction {}
