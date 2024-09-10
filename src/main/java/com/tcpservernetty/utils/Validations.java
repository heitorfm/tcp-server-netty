package com.tcpservernetty.utils;

import jdk.internal.vm.annotation.ForceInline;

public class Validations {

    @ForceInline
    public static <T> T notNullArg(T obj) {
        if (obj == null)
            throw new IllegalArgumentException();
        return obj;
    }

    @ForceInline
    public static <T> T notNullArg(T obj, String msg) {
        if (obj == null)
            throw new IllegalArgumentException(msg);
        return obj;
    }

}
