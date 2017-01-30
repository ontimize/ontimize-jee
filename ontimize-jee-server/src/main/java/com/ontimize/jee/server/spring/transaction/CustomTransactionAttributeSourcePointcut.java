package com.ontimize.jee.server.spring.transaction;

import java.lang.reflect.Method;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

public class CustomTransactionAttributeSourcePointcut extends StaticMethodMatcherPointcut {

    private TransactionAttributeSource transactionAttributeSource;

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        ClassFilter classFilter2 = this.getClassFilter();
        if ((classFilter2 != null) && classFilter2.matches(targetClass)) {
            return ((this.transactionAttributeSource == null) || (this.transactionAttributeSource.getTransactionAttribute(method, targetClass) != null));
        } else {
            return false;
        }
    }

    public void setTransactionAttributeSource(TransactionAttributeSource transactionAttributeSource) {
        this.transactionAttributeSource = transactionAttributeSource;
    }

}
